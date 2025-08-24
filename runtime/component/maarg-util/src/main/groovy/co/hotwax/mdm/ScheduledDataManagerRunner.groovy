package co.hotwax.mdm

import co.hotwax.datamanager.MaargDataLoaderImpl
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.moqui.BaseException
import org.moqui.entity.EntityCondition
import org.moqui.entity.EntityList
import org.moqui.entity.EntityValue
import org.moqui.impl.context.ExecutionContextFactoryImpl
import org.moqui.impl.context.ExecutionContextImpl
import org.moqui.util.CollectionUtilities
import org.moqui.util.SystemBinding
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * ScheduledDataManagerRunner
 *
 * <p>This runner is responsible for processing DataManagerLog (DML) records for file imports.
 * It uses thread pools to manage parallel and sequential executions depending on configuration.
 *
 * <h3>Memory Leak Handling</h3>
 * <ul>
 *   <li>Each worker thread explicitly creates and destroys its own ExecutionContextImpl (ECI).
 *       This prevents thread-local variables (like artifact execution stacks and message caches)
 *       from accumulating, which otherwise could lead to large memory retention.</li>
 *   <li>For chunked file processing, a new ECI is created for each chunk and destroyed immediately
 *       after processing, ensuring any cached artifacts or messages are cleared quickly.</li>
 *   <li>Scheduler-level ECI is destroyed in the `finally` block of the `run()` method to ensure
 *       no global context references remain.</li>
 * </ul>
 *
 * <h3>Thread Pools</h3>
 * <ul>
 *   <li>Priority queue: 3 threads</li>
 *   <li>Normal queue: 3 threads</li>
 * </ul>
 */
@CompileStatic
class ScheduledDataManagerRunner implements Runnable {
    protected final static Logger logger = LoggerFactory.getLogger(ScheduledDataManagerRunner.class)

    // Thread group for easier debugging and grouping of worker threads
    private final ThreadGroup dmGroup = new ThreadGroup("DataManagerGroup")
    private final int purgeDays = SystemBinding.getPropOrEnv("datamanager.purge.days") != null ? SystemBinding.getPropOrEnv("datamanager.purge.days").toInteger() : 30
    private final String instanceId = SystemBinding.getPropOrEnv("unique.instance.id")
    private long lastExecuteTime = 0

    private final ThreadFactory dmFactory = { Runnable r ->
        Thread t = new Thread(dmGroup, r)
        t.name = "DM-Worker-${t.id}"
        t.daemon = false
        t.uncaughtExceptionHandler = { Thread th, Throwable ex ->
            logger.error("Uncaught exception in thread ${th.name}", ex)
        } as Thread.UncaughtExceptionHandler
        return t
    } as ThreadFactory

    public ThreadGroup getDmThreadGroup() {
        return this.dmGroup
    }
    long getLastExecuteTime() { lastExecuteTime }
    private final ExecutionContextFactoryImpl ecfi

    // Different pools for priority-based execution
    //TODO: We can make the thread pool size configurable
    private final ThreadPoolExecutor priorityPool =
            new ThreadPoolExecutor(3, 3, 0L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(10), dmFactory, new ThreadPoolExecutor.CallerRunsPolicy())
    private final ThreadPoolExecutor normalPool =
            new ThreadPoolExecutor(3, 3, 0L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(10), dmFactory, new ThreadPoolExecutor.CallerRunsPolicy())
    //TODO: Remove lowPool, it's good to keep only 2 thread pool
    /*private final ThreadPoolExecutor lowPool =
            new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(10), dmFactory, new ThreadPoolExecutor.CallerRunsPolicy())*/

    private final AtomicInteger priorityActive = new AtomicInteger(0)
    private final AtomicInteger normalActive   = new AtomicInteger(0)
    private final AtomicInteger lowActive      = new AtomicInteger(0)

    ScheduledDataManagerRunner(ExecutionContextFactoryImpl ecfi) {
        this.ecfi = ecfi
    }

    /**
     * Entry point for scheduled job execution.
     * Creates an ExecutionContextImpl for scheduler context, fetches pending DataManagerLogs
     * and dispatches them to thread pools based on configuration.
     *
     * <p>Memory leak prevention: `eci.destroy()` is called in `finally` to ensure no thread-local
     * data from the scheduler run remains.</p>
     */
    @Override
    void run() {
        ExecutionContextImpl eci = ecfi.getEci()
        try {
            eci.userFacade.loginAnonymousIfNoUser()
            EntityList configList = eci.entityFacade.find("co.hotwax.datamanager.DataManagerConfigAndLog")
                    .condition("logTypeEnumId","DmltImport")
                    .condition("statusId", "DmlsPending")
                    .selectField("configId")
                    .orderBy("priority desc")
                    .distinct(true)
                    .disableAuthz()
                    .list()

            if (logger.isDebugEnabled()) {
                logger.debug("Processing {} config records for data manager import", configList?.size() ?: 0)
            }
            if (configList.isEmpty() && normalActive.get() == 0) {
                purgeDataManagerLogs()
            }
            configList.each { config ->
                String configId   = config.getString("configId")
                String queueType  = getQueueType(eci, configId)
                String execMode   = getExecutionMode(eci, configId)

                if (!jobSlotsAvailable(queueType)) {
                    logger.debug("No slot available for queue ${queueType}, configId ${configId}")
                    return
                }

                if ("DMC_QUEUE".equalsIgnoreCase(execMode)) {
                    // Sequential execution mode for config
                    processConfigSequentially(eci, configId, queueType)
                } else {
                    // Parallel pool processing mode
                    processConfigWithPools(eci, configId, queueType)
                }
            }
            lastExecuteTime = ZonedDateTime.now().toInstant().toEpochMilli()
        } catch (Exception e) {
            logger.error("Error in ScheduledDataManagerRunner", e)
        } finally {
            eci.destroy() // <-- CRITICAL TO PREVENT LEAK
        }
    }

    /**
     * Processes logs for a configuration sequentially.
     * Waits for each DML to finish before processing the next.
     * If configuration is marked multi-threaded, chunk processing is triggered.
     */
    private void processConfigSequentially(ExecutionContextImpl eci, String configId, String queueType) {
        EntityList pendingLogs = eci.entityFacade.find("co.hotwax.datamanager.DataManagerLog")
                .condition("logTypeEnumId","DmltImport")
                .condition("statusId", "DmlsPending")
                .condition("configId", configId)
                .orderBy("createdDate asc")
                .disableAuthz()
                .list()

        pendingLogs.each { log ->
            updateDataManagerLog(eci, log.getString("logId"), CollectionUtilities.toHashMap("statusId","DmlsQueued"))
        }

        // IMPORTANT: orchestrate on ecfi.workerPool, not on the same PRIORITY/NORMAL pool
        ecfi.workerPool.submit({
            increment(queueType) // track running sequencer
            ExecutionContextImpl ec = ecfi.getEci()
            try {
                for (EntityValue log : pendingLogs) {
                    EntityValue locked = ec.entityFacade.find("co.hotwax.datamanager.DataManagerLog")
                            .condition("logId", log.logId).disableAuthz().one()
                    if (locked?.statusId != "DmlsQueued") continue

                    if (isMultiThreadedConfig(ec, configId)) {
                        // processFileInChunks already orchestrates on workerPool and submits chunk work to pools
                        Future<Void> f = processFileInChunks(locked, queueType)
                        f.get()
                    } else {
                        // submit to PRIORITY/NORMAL pool and wait here (workerPool thread)
                        Future<Void> f = submitTask(locked, queueType)
                        f.get()
                    }
                }
            } finally {
                try { ec.destroy() } catch (Throwable ignore) {}
                decrement(queueType)
            }
            return null
        } as Callable<Void>)
    }

    /**
     * Processes logs using thread pools.
     * <p>Unlike sequential processing, this submits logs to thread pools directly without waiting.
     * This is used for configs where parallel processing is allowed.</p>
     */
    private void processConfigWithPools(ExecutionContextImpl eci, String configId, String queueType) {
        EntityList pendingLogs = eci.entityFacade.find("co.hotwax.datamanager.DataManagerLog")
                .condition("logTypeEnumId","DmltImport")
                .condition("statusId", "DmlsPending")
                .condition("configId", configId)
                .orderBy("createdDate asc")
                .disableAuthz()
                .list()

        pendingLogs.each { log ->
            if (!jobSlotsAvailable(queueType)) {
                logger.debug("No slot available for queue ${queueType}, logId ${log.logId}")
                return
            }
            EntityValue lockedLog = eci.entityFacade.find("co.hotwax.datamanager.DataManagerLog")
                    .condition("logId", log.logId)
                    .disableAuthz().one()
            if (lockedLog?.statusId == "DmlsPending") {
                updateDataManagerLog(eci, lockedLog.getString("logId"), CollectionUtilities.toHashMap("statusId","DmlsQueued"))
                if (isMultiThreadedConfig(eci, configId)) {
                    processFileInChunks(lockedLog, queueType)
                } else {
                    submitTask(lockedLog, queueType)
                }
            }
        }
    }

    /**
     * Checks if queue has available slots.
     * @param queueType queue type (PRIORITY, LOW, NORMAL)
     * @return true if slots available
     */
    private boolean jobSlotsAvailable(String queueType) {
        switch (queueType) {
            case "PRIORITY": return priorityActive.get() < 10
            //case "LOW":      return lowActive.get() < 10
            default:         return normalActive.get() < 10
        }
    }
    /*private boolean jobSlotsAvailable(String queueType) {
        ThreadPoolExecutor pool = getPool(queueType)
        // if any worker idle OR queue has space, we can accept a job
        return pool.activeCount < pool.corePoolSize || pool.queue.remainingCapacity() > 0
    }*/

    /**
     * Submits a single DML log processing task.
     * <p>Returns a Future so caller can optionally wait (for sequential mode) or ignore (for parallel mode).</p>
     * <p>Each task creates its own ECI and destroys it after processing to prevent memory leaks.</p>
     */
    private Future<Void> submitTask(EntityValue log, String queueType) {
        Callable<Void>  task = {
            increment(queueType)
            ExecutionContextImpl eci = ecfi.getEci()
            try {
                updateDataManagerLog(eci, log.getString("logId"), CollectionUtilities.toHashMap("statusId","DmlsRunning"))
                EntityValue uploadedContent= getContent(eci, log.getString("logId"), "DmcntImported")
                String filePath = uploadedContent.getString("contentLocation")

                Map<String,Object> defaultValues = getDataManagerParameters(eci, log.getString("logId"))

                MaargDataLoaderImpl mdli = new MaargDataLoaderImpl(ecfi)
                        .serviceName(getServiceName(eci, log.getString("configId")))
                        .location(filePath)
                if (defaultValues) {
                    mdli.defaultValues(defaultValues)
                }
                if (uploadedContent.getString("fileName") != null && !uploadedContent.getString("fileName").isEmpty()) {
                    mdli.fileName(uploadedContent.getString("fileName"));
                }
                long totalRecordCount = mdli.load()
                if (mdli.errorFileLocation) {
                    long fileSize = new File(eci.resourceFacade.getLocationReference(mdli.errorFileLocation).getUrl().toURI().getPath()).length()
                    createErrorContent(eci, log.getString("logId"), mdli.errorFileName, mdli.errorFileLocation, fileSize)
                }
                updateDataManagerLog(eci, log.getString("logId"),
                        CollectionUtilities.toHashMap("statusId","DmlsFinished", "totalRecordCount", totalRecordCount, "failedRecordCount", mdli.getFailedRecordCount()))
            } catch (Exception e) {
                logger.error("Error processing DataManagerLog ${log.logId}", e)
                updateDataManagerLog(eci, log.getString("logId"), CollectionUtilities.toHashMap("statusId","DmlsFailed"))
            } finally {
                eci.destroy() // important for preventing thread-local leak
                decrement(queueType)
            }
            return null
        } as Callable<Void>
        // Use submitWithWait to respect queue capacity & backpressure
        return submitWithWait(queueType, task)
    }

    /**
     * Processes file chunks in parallel.
     * <p>Each chunk gets its own ECI and runs as an independent task. Waits for all chunks to finish.</p>
     */
    /** Orchestrate chunking on workerPool and execute chunks on PRIORITY/NORMAL pools */
    private Future<Void> processFileInChunks (EntityValue log, String queueType) {
        return ecfi.workerPool.submit({
            ExecutionContextImpl eci = ecfi.getEci()
        try {
            def uploadedContent = getContent(eci, log.getString("logId"), "DmcntImported")
            def filePath = uploadedContent.getString("contentLocation")
            def defaultValues = getDataManagerParameters(eci, log.getString("logId"))

            List<String> chunks = splitFile(filePath, log.getString("logId"), (String) defaultValues.get("groupBy"))
            updateDataManagerLog(eci, log.getString("logId"), CollectionUtilities.toHashMap("statusId","DmlsRunning"))

            CountDownLatch latch = new CountDownLatch(chunks.size())
            List<Future<Map<String,Object>>> futures = new ArrayList<>(chunks.size())
            long startms = System.currentTimeMillis()

            // enqueue each chunk on the PRIORITY/NORMAL pool
            for (String chunk : chunks) {
                Callable<Map<String,Object>> chunkTask = {
                    increment(queueType)
                    ExecutionContextImpl ecChunk = ecfi.getEci()
                    try {
                        MaargDataLoaderImpl mdli = new MaargDataLoaderImpl(ecfi)
                                .serviceName(getServiceName(ecChunk, log.getString("configId")))
                                .location(chunk.toString())
                        if (defaultValues) {
                            mdli.defaultValues(defaultValues)
                        }
                        long totalRecordCount = mdli.load()
                        return [totalRecordCount: totalRecordCount, errorFileLocation: mdli.getErrorFileLocation(), failedRecordCount: mdli.getFailedRecordCount() ]
                    } catch (Exception e) {
                        logger.error("Chunk failed for log {} file {}", log.getString("logId"), chunk, e)
                    } finally {
                        try {
                        ecChunk.destroy()
                        } catch (Throwable ignore) {
                            logger.error("Error while destroying eci {}", ignore.getMessage())
                        }
                        decrement(queueType)
                        latch.countDown()
                    }
                } as Callable<Map<String,Object>>

                futures.add(submitWithWait(queueType, chunkTask)) // uses PRIORITY/NORMAL pools
            }

            // wait for all chunks (only blocks a workerPool thread, not your domain pools)
            latch.await()

            // aggregate results
            long totalRecords = 0L, totalFailed = 0L
            List<String> errorFiles = []
            for (Future<Map<String,Object>> future : futures) {
                Map<String,Object> chunkFuture = null
                try {
                    chunkFuture = future.get()
                } catch (Throwable t) {
                    logger.error("Chunk future get failed", t)
                }
                if (chunkFuture == null) {
                    continue
                }
                totalRecords += ((Number) chunkFuture.getOrDefault("totalRecordCount", 0L)).longValue()
                totalFailed  += ((Number) chunkFuture.getOrDefault("failedRecordCount", 0L)).longValue()
                String efl = (String) chunkFuture.get("errorFileLocation")
                if (efl) errorFiles.add(efl)
            }

            if (!errorFiles.isEmpty()) {
                String fileName = uploadedContent.getString("fileName") ?: FilenameUtils.getName(filePath)
                String errorName = "Error_" + fileName
                String toLocation = FilenameUtils.getFullPath(filePath) + MaargDataLoaderImpl.prepareErrorFileName(fileName)
                long fileSize = mergeErrorFiles(eci, errorFiles, toLocation)
                createErrorContent(eci, log.getString("logId"), errorName, toLocation, fileSize)
            }

            updateDataManagerLog(eci, log.getString("logId"),
                    CollectionUtilities.toHashMap("statusId","DmlsFinished",
                            "totalRecordCount", totalRecords, "failedRecordCount", totalFailed))

            logger.info("Loaded {} records ({} failed) from {} in {}s.",
                    totalRecords, totalFailed, filePath, (System.currentTimeMillis()-startms)/1000)

        } catch (Throwable t) {
            logger.error("Error running chunked job for log {}", log.getString("logId"), t)
            updateDataManagerLog(eci, log.getString("logId"), CollectionUtilities.toHashMap("statusId","DmlsFailed"))
        } finally {
            try {
                eci.destroy()
            } catch (Throwable ignore) {
                logger.error("Error while destroying eci {}", ignore.getMessage())
            }
            try {
                cleanUp(log.getString("logId"))
            } catch (Throwable ce) {
                logger.warn("Cleanup failed for {}", log.getString("logId"), ce)
            }
        }
        return null
        } as Callable<Void>)
    }

    /** Deletes temporary split file directory */
    private void cleanUp(String logId) {
        try {
            FileUtils.deleteDirectory(new File(ecfi.runtimePath, "tmp/DM_${logId}"))
        } catch (IOException e) {
            logger.error("Error file cleanup files for logId ${logId}", e);
        }
    }

    /**
     * Submits a task with backpressure handling.
     * <p>Waits until queue capacity is available before submitting.</p>
     */
    private <T> Future<T> submitWithWait(String queueType, Callable<T> task) {
        ThreadPoolExecutor pool = getPool(queueType)
        long lastLog = 0L
        while (pool.queue.remainingCapacity() == 0) {
            long now = System.currentTimeMillis()
            if (now - lastLog > 10_000) {
                logger.warn("Pool {} full (size {}, remaining 0), pausing...", queueType, pool.queue.size())
                lastLog = now
            }
            try {
                Thread.sleep(1_000)
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt()
                throw ie
            }
        }
        return pool.submit(task)
    }

    /** Returns thread pool for queue type */
    private ThreadPoolExecutor getPool(String queueType) {
        switch (queueType) {
            case "PRIORITY": return priorityPool
            //case "LOW":      return lowPool
            default:         return normalPool
        }
    }

    /** Merges multiple error files into one (supports CSV and JSON) */
    private static long mergeErrorFiles(ExecutionContextImpl ec, List<String> files, String toLocation) {
        String filePath = ec.resourceFacade.getLocationReference(toLocation).getUrl().toURI().getPath()
        if (!files || files.isEmpty()) {
            return
        }
        File errorFile = new File(filePath);
        if (filePath.toLowerCase().endsWith(".json")) {
            JsonFactory factory = new JsonFactory()
            ObjectMapper mapper = new ObjectMapper(factory)
            errorFile.withWriter("UTF-8") { writer ->
                try (JsonGenerator generator = factory.createGenerator(writer)) {
                    generator.setCodec(mapper)
                    generator.writeStartArray()
                    files.each { path ->
                        File jsonFile = new File(ec.resourceFacade.getLocationReference(path).getUrl().toURI().getPath())
                        try (JsonParser parser = factory.createParser(jsonFile)) {
                            if (parser.nextToken() != JsonToken.START_ARRAY) {
                                throw new RuntimeException("Expected JSON array at root in file: $path")
                            }
                            while (parser.nextToken() == JsonToken.START_OBJECT) {
                                def node = parser.readValueAsTree()
                                generator.writeTree(node)
                            }
                        }
                        jsonFile.delete()
                    }
                    generator.writeEndArray()
                }
            }
        } else {
            errorFile.withWriter("UTF-8") { writer ->
                boolean headerWritten = false
                files.each { path ->
                    File f = new File(ec.resourceFacade.getLocationReference(path).getUrl().toURI().getPath())
                    f.eachLine { line, idx ->
                        if (idx == 1 && headerWritten) {
                            return
                        }
                        writer.writeLine(line)
                    }
                    headerWritten = true
                    f.delete()
                }
            }
        }
        return errorFile.length()
    }

    /** Increments active counter for queue type */
    private void increment(String queueType) {
        switch (queueType) {
            case "PRIORITY": priorityActive.incrementAndGet(); break
                //case "LOW":      lowActive.incrementAndGet(); break
                default:         normalActive.incrementAndGet()
            }
    }

    /** Decrements active counter for queue type */
    private void decrement(String queueType) {
        switch (queueType) {
            case "PRIORITY": priorityActive.decrementAndGet(); break
            //case "LOW":      lowActive.decrementAndGet(); break
            default:         normalActive.decrementAndGet()
        }
    }


    /**
     * Splits a large input file into smaller chunks based on the provided group key or a default chunk size.
     * <p>
     * <b>Supported file types:</b>
     * <ul>
     *   <li><b>CSV:</b> The file is parsed using Apache Commons CSV. A separate chunk file is created
     *   for each group key value if provided, or otherwise, records are divided into chunks
     *   of a fixed size (50,000 rows by default).</li>
     *   <li><b>JSON:</b> The file is parsed using Jackson streaming API. Each JSON object is grouped
     *   by the provided key or split into chunks of a fixed size.</li>
     *   <li><b>Other formats:</b> If the file is not CSV or JSON, no split is performed, and the
     *   original file path is returned as a single-element list.</li>
     * </ul>
     *
     * <p><b>Memory considerations:</b>
     * Uses streaming APIs (Commons CSV parser, Jackson streaming) to avoid loading entire file into memory.
     * Each chunk is written as a separate file under a temporary directory (runtime/tmp/DM_{logId}/).</p>
     *
     * @param filePath  The full path to the source file
     * @param logId     The log ID used for temporary file naming
     * @param groupBy   Optional group-by key (CSV header or JSON field) for grouping records
     * @return A list of chunk file paths generated by this operation. Returns a single-element list containing
     *         the original file path if no splitting was necessary.
     * @throws IOException if reading or writing chunk files fails
     */
    private List<String> splitFile(String filePath, String logId, String groupBy) {
        int chunkSize = 50000
        //Cleanup old split file if exists
        cleanUp(logId)
        String outputFileLocation = ecfi.resourceFacade.getLocationReference(filePath).url.toURI().path
        File tmpDir = new File(ecfi.runtimePath, "tmp/DM_${logId}/")

        if (!tmpDir.exists()) {
            tmpDir.mkdirs()
        }

        List<String> chunkPaths = []
        String lower = outputFileLocation.toLowerCase()

        if (lower.endsWith(".csv")) {
            //TODO: There is no better way to use try with resource for dynamic CSVPrinter
            Map<String, CSVPrinter> printers = [:]
            Map<String, Writer> writers = [:]
            try (Reader reader = Files.newBufferedReader(Paths.get(outputFileLocation));
                CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(reader)) {
                Map<String, Integer> headerMap = parser.headerMap
                List<String> headers = new ArrayList<>(headerMap.keySet())
                boolean hasGroup = groupBy && headerMap.containsKey(groupBy)
                int sequence = 1

                for (CSVRecord rec : parser) {
                    String key = hasGroup ? rec.get(headerMap[groupBy]) : ((rec.recordNumber % chunkSize == 0) ? (++sequence).toString() : sequence.toString())

                    if (!printers.containsKey(key)) {
                        File cf = new File(tmpDir, "${logId}_${key}.csv")
                        Writer w = Files.newBufferedWriter(cf.toPath())
                        CSVPrinter p = CSVFormat.Builder.create().setHeader(headers as String[]).get().print(w);
                        printers[key] = p
                        writers[key] = w
                        chunkPaths << cf.absolutePath

                    }
                    printers[key].printRecord(headers.collect { rec.get(it) ?: "" })
                }
            } finally {
                printers.each { _, p -> p.close() }
                writers.each { _, w -> w.close() }
            }
        } else if (lower.endsWith(".json")) {
            JsonFactory jf = new JsonFactory()
            ObjectMapper mapper = new ObjectMapper()
    
            try (JsonParser jp = jf.createParser(new File(outputFileLocation))) {
                while (jp.nextToken() != JsonToken.START_ARRAY) {
                    if (jp.currentToken() == null)  {
                        break
                    }
                }
                Map<String, JsonGenerator> gens = [:]
                Map<String, Writer> gWriters = [:]
                long recordIndex = 0; int sequence = 1

                while (jp.nextToken() == JsonToken.START_OBJECT) {
                    JsonNode node = mapper.readTree(jp)
                    String key
                    if (groupBy && node.has(groupBy)) {
                        key = node.get(groupBy).asText()
                    } else {
                        if (recordIndex > 0 && (recordIndex % chunkSize) == 0) {
                            sequence++
                        }
                        key = Integer.toString(sequence)
                        recordIndex++
                    }

                    if (!gens.containsKey(key)) {
                        File cf = new File(tmpDir, "${logId}_${key}.json")
                        Writer w = Files.newBufferedWriter(cf.toPath())
                        JsonGenerator gen = jf.createGenerator(w)
                        gen.setCodec(mapper)
                        gen.writeStartArray()
                        gens[key] = gen
                        gWriters[key] = w
                        chunkPaths << cf.absolutePath
                    }
                    gens[key].writeObject(node)
                }
                // end arrays and cleanup
                gens.each { _, gen -> gen.writeEndArray(); gen.close() }
                gWriters.each { _, w -> w.close() }
            }
        } else {
            chunkPaths << filePath
        }
        return chunkPaths
    }

    /**
     * Gets the execution mode for the given DataManagerConfig.
     * <p>
     * Determines whether the configuration should be executed sequentially in a queue
     * or in parallel with the default pool-based execution.
     * </p>
     *
     * @param ec        The execution context
     * @param configId  The DataManagerConfig ID
     * @return The execution mode, e.g., "DMC_QUEUE" or "DMC_ASYNC"
     */
    private static String getExecutionMode(ExecutionContextImpl ec, String configId) {
        EntityValue config = ec.entityFacade.find("co.hotwax.datamanager.DataManagerConfig")
                .condition("configId", configId).useCache(true).disableAuthz().one()
        return config?.getString("executionModeId") ?: "DMC_QUEUE"
    }

    /**
     * Checks if the given DataManagerConfig supports multi-threaded processing.
     * <p>
     * If multi-threading is enabled, file data will be split into chunks and processed in parallel.
     * </p>
     *
     * @param ec        The execution context
     * @param configId  The DataManagerConfig ID
     * @return True if multi-threaded processing is enabled, false otherwise
     */
    private static boolean isMultiThreadedConfig(ExecutionContextImpl ec, String configId) {
        EntityValue config = ec.entityFacade.find("co.hotwax.datamanager.DataManagerConfig")
                .condition("configId", configId).useCache(true).disableAuthz().one()
        return config && "Y".equalsIgnoreCase(config.getString("multiThreading"))
    }

    /**
     * Gets the queue type (PRIORITY, NORMAL, LOW) based on configuration priority value.
     * <p>
     * Priority greater than 7 is treated as PRIORITY, less than 5 as LOW, and everything else as NORMAL.
     * </p>
     *
     * @param ec        The execution context
     * @param configId  The DataManagerConfig ID
     * @return The queue type as a String
     */
    private static String getQueueType(ExecutionContextImpl ec, String configId) {
        EntityValue config = ec.entityFacade.find("co.hotwax.datamanager.DataManagerConfig")
                .condition("configId", configId).useCache(true).disableAuthz().one()
        long priority = config.getLong("priority") ?: 5
        //return (priority > 7) ? "PRIORITY" : (priority < 5) ? "LOW" : "NORMAL"
        return (priority > 6) ? "PRIORITY" : "NORMAL"
    }

    /**
     * Gets the service name to be invoked for importing data based on DataManagerConfig.
     * <p>
     * Each DataManagerConfig defines an import service responsible for handling its data.
     * </p>
     *
     * @param ec        The execution context
     * @param configId  The DataManagerConfig ID
     * @return The service name as defined in configuration
     */
    private static String getServiceName(ExecutionContextImpl ec, String configId) {
        EntityValue config = ec.entityFacade.find("co.hotwax.datamanager.DataManagerConfig")
                .condition("configId", configId).useCache(true).disableAuthz().one()
        return config.getString("importServiceName")
    }

    /**
     * Fetches the content record associated with the given DataManagerLog.
     * <p>
     * This content record represents the file or data that needs to be imported.
     * </p>
     *
     * @param ec                      The execution context
     * @param logId                   The DataManagerLog ID
     * @param logContentTypeEnumId    The log content type (e.g., imported file)
     * @return The content EntityValue
     * @throws BaseException if no content is found for the given log ID
     */
    private static EntityValue getContent (ExecutionContextImpl ec, String logId, String logContentTypeEnumId) {
        List<EntityValue> dmContents = ec.entityFacade.find("co.hotwax.datamanager.DataManagerContent")
                .condition("logId", logId)
                .condition("logContentTypeEnumId", logContentTypeEnumId)
                .condition("contentLocation", EntityCondition.ComparisonOperator.IS_NOT_NULL, null)
                .orderBy("contentDate desc").disableAuthz().list()
        if (!dmContents) {
            throw new BaseException("No import file content found for logId ${logId}")
        }
        return dmContents[0]
    }

    /**
     * Creates an error content record in DataManagerContent entity.
     * <p>
     * This is used to persist the error file generated during import processing.
     * </p>
     *
     * @param ec                 The execution context
     * @param logId              The DataManagerLog ID
     * @param errorFileName      The name of the error file
     * @param errorFileLocation  The location of the error file
     */
    private static void createErrorContent (ExecutionContextImpl ec, String logId, String errorFileName, String errorFileLocation, long fileSize) {
        ec.serviceFacade.sync().name("create#co.hotwax.datamanager.DataManagerContent")
                .parameters([logId: logId, contentLocation: errorFileLocation, fileSize: fileSize,
                             fileName: errorFileName, logContentTypeEnumId: "DmcntError"])
                .requireNewTransaction(true).ignorePreviousError(true)
                .disableAuthz().call()
    }

    /**
     * Updates the status of a DataManagerLog.
     * <p>
     * Depending on the status, it also updates start or finish timestamps
     * and records the thread executing the log.
     * </p>
     *
     * @param ec        The execution context
     * @param logId     The DataManagerLog ID
     * @param statusId  The status to set (e.g., DmlsRunning, DmlsFinished, DmlsFailed)
     */
    private static void updateDataManagerLog(ExecutionContextImpl ec, String logId, Map<String,Object> data) {
        String statusId = (data?.get("statusId") ?: "") as String
        Map<String, Object> serviceInCtx = new HashMap<>(data ?: [:])
        serviceInCtx.put("logId", logId)

        if ("DmlsRunning".equals(statusId)) {
            serviceInCtx.put('startDateTime', ec.userFacade.nowTimestamp);
            serviceInCtx.put('runThread', Thread.currentThread().getName());
        } else if ("DmlsQueued".equals(statusId)) {
            serviceInCtx.put('runByInstanceId', SystemBinding.getPropOrEnv("unique.instance.id"));
        } else if (!"DmlsQueued".equals(statusId)) {
            serviceInCtx.put('finishDateTime', ec.userFacade.nowTimestamp);
        }
        ec.serviceFacade.sync().name("update#co.hotwax.datamanager.DataManagerLog")
                .parameters(serviceInCtx)
                .disableAuthz().ignorePreviousError(true).requireNewTransaction(true).call()
        if (ec.messageFacade.hasError()) {
            logger.error(ec.messageFacade.errorsString); ec.messageFacade.clearErrors()
        }
    }

    /**
     * Fetches additional parameters associated with a DataManagerLog.
     * <p>
     * These parameters are passed as default values to the import service during processing.
     * </p>
     *
     * @param ec     The execution context
     * @param logId  The DataManagerLog ID
     * @return A map of parameter name to parameter value
     */
    private static Map<String, Object> getDataManagerParameters(ExecutionContextImpl ec, String logId) {
        Map<String, Object> parameters = [:]
        ec.entityFacade.find("co.hotwax.datamanager.DataManagerParameter").condition("logId", logId).disableAuthz().list()
                .each { parameters.put(it.parameterName as String, it.parameterValue) }
        parameters.put("logId", logId)
        return parameters
    }

    /**
     * Submits a background task to purge old DataManagerLog records and their associated contents.
     * The task is only submitted if all queues are idle and no pending configs are found.
     */
    private void purgeDataManagerLogs() {
        Callable<Void> purgeTask = {
            ExecutionContextImpl eci = ecfi.getEci()
            eci.userFacade.loginAnonymousIfNoUser()
            try {
                List<EntityValue> oldLogs = eci.entityFacade.find("co.hotwax.datamanager.DataManagerLog")
                        .condition("statusId", EntityCondition.IN, ["DmlsFinished", "DmlsFailed", "DmlsCrashed", "DmlsCancelled"])
                        .condition("createdDate", EntityCondition.LESS_THAN, Timestamp.valueOf(LocalDateTime.now().minusDays(purgeDays)))
                        .condition("runByInstanceId", instanceId)
                        .selectField("logId")
                        .orderBy("createdDate asc")
                        .limit(50)
                        .disableAuthz()
                        .list()
                oldLogs.each { log ->
                    eci.serviceFacade.sync().name("co.hotwax.common.CommonServices.remove#DataManagerLog")
                            .parameters([logId: log.logId])
                            .requireNewTransaction(true)
                            .ignorePreviousError(true)
                            .disableAuthz()
                            .call()
                    if (eci.messageFacade.hasError()) {
                        eci.messageFacade.clearErrors()
                        eci.messageFacade.clearAll()
                    }
                }
                if (oldLogs.size() > 0) {
                    logger.info("Purged {} DataManagerLog records older than {} days", oldLogs.size(), purgeDays)
                }
            } catch (Exception e) {
                logger.warn("Error purging DataManagerLog records : ${e.message}", e)
            } finally {
                eci.destroy()
            }
            return null
        } as Callable<Void>

        submitWithWait("NORMAL", purgeTask)
    }
}
