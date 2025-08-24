package co.hotwax.mdm

import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils
import org.moqui.context.ExecutionContextFactory
import org.moqui.context.ToolFactory
import org.moqui.entity.EntityCondition
import org.moqui.entity.EntityList
import org.moqui.impl.context.ExecutionContextFactoryImpl
import org.moqui.impl.context.ExecutionContextImpl
import org.moqui.util.SystemBinding
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
class DataManagerToolFactory implements ToolFactory<ScheduledDataManagerRunner> {
    protected final static Logger logger = LoggerFactory.getLogger(DataManagerToolFactory.class)
    final static String TOOL_NAME = "MDM"
    private ScheduledDataManagerRunner dataManagerRunner
    protected ExecutionContextFactoryImpl ecfi = null

    /** Default empty constructor */
    DataManagerToolFactory() { }

    @Override
    String getName() { return TOOL_NAME }

    @Override
    void init(ExecutionContextFactory ecf) {
        this.ecfi = (ExecutionContextFactoryImpl) ecf
        long mdmRunnerRate = (SystemBinding.getPropOrEnv("scheduled.mdm.check.time") ?: "60") as long
        if (mdmRunnerRate > 0L) {
            dataManagerRunner = new ScheduledDataManagerRunner(this.ecfi)
            this.ecfi.scheduleAtFixedRate(dataManagerRunner, 120L, mdmRunnerRate)
            logger.info("Scheduled DataManager Runner every {}s after {}s delay", mdmRunnerRate, 120)
            // TODO: Replace this manual instance ID configuration with a Moqui‐native solution, right now no native solution exists
            // For now, read a unique server identifier from configuration (unique.instance.id)
            // so that ScheduledDataManagerRunner can distinguish between cluster nodes when reloading jobs.
            reScheduleCrashedImport()
        } else {
            logger.warn("Not starting Scheduled DataManager Runner (config:${mdmRunnerRate})")
            dataManagerRunner = null;
        }
    }
    private void reScheduleCrashedImport() {
        EntityList crashedImports = ecfi.entity.find("co.hotwax.datamanager.DataManagerLog")
                .condition("statusId", EntityCondition.IN,  ["DmlsRunning", "DmlsQueued"])
                .condition("runByInstanceId", SystemBinding.getPropOrEnv("unique.instance.id"))
                .disableAuthz()
                .list()
        if (crashedImports) {
            crashedImports.each { crashedImport ->
                ecfi.transactionFacade.runRequireNew(600, "ReSchedule crashed job", true, false) {
                    ExecutionContextImpl ec = ecfi.getEci();
                    ec.artifactExecutionFacade.disableAuthz()
                    ec.userFacade.loginAnonymousIfNoUser()
                    ec.serviceFacade.sync().name("update#co.hotwax.datamanager.DataManagerLog")
                        .parameters(["logId": crashedImport.getString("logId"), "statusId": "DmlsCrashed", "cancelDateTime": ec.userFacade.nowTimestamp])
                        .disableAuthz().ignorePreviousError(true)
                        .requireNewTransaction(true).call();
                    def dataManagerLog = crashedImport.cloneValue();
                    dataManagerLog.remove("logId")
                    dataManagerLog.remove("startDateTime")
                    dataManagerLog.remove("runThread")
                    dataManagerLog.remove("runByInstanceId")
                    dataManagerLog.put("parentLogId", crashedImport.getString("logId"))
                    dataManagerLog.put("statusId", "DmlsPending")
                    dataManagerLog.put("createdDate", ec.userFacade.nowTimestamp)
                    dataManagerLog.setSequencedIdPrimary()
                    dataManagerLog.create()

                    EntityList importParameters = ec.entity.find("co.hotwax.datamanager.DataManagerParameter")
                            .condition("logId", crashedImport.getString("logId")).disableAuthz()
                            .list()
                    if (importParameters) {
                        importParameters.each {
                            def importParameter = it.cloneValue();
                            importParameter.put("logId", dataManagerLog.getString("logId"));
                            importParameter.create()
                        }
                    }
                    EntityList dmContents = ec.entity.find("co.hotwax.datamanager.DataManagerContent").disableAuthz()
                            .condition("logId", crashedImport.getString("logId"))
                            .condition("logContentTypeEnumId", "DmcntImported")
                            .list()
                    if (dmContents) {
                        dmContents.each {
                            def dmContent = it.cloneValue();
                            dmContent.put("logId", dataManagerLog.getString("logId"));
                            dmContent.setSequencedIdPrimary();
                            dmContent.create();
                        }
                    }
                    if (this.ecfi != null ) {
                        FileUtils.deleteDirectory(new File(ecfi.getRuntimePath(), "tmp/DM_${crashedImport.getString("logId")}"))
                    }
                    ec.destroy()
                }
            }
            logger.info("Re-scheduled ${crashedImports.size()} crashed imports")
        } else {
            logger.info("No crashed imports found for re-scheduling")
        }
    }

    @Override
    ScheduledDataManagerRunner getInstance(Object... parameters) {
        return dataManagerRunner;
    }
}
