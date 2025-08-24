package co.hotwax.datamanager;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.moqui.BaseException;
import org.moqui.context.TransactionFacade;
import org.moqui.impl.context.ContextJavaUtil;
import org.moqui.impl.context.ExecutionContextFactoryImpl;
import org.moqui.impl.context.ExecutionContextImpl;
import org.moqui.impl.context.L10nFacadeImpl;
import org.moqui.impl.service.ServiceCallSyncImpl;
import org.moqui.impl.service.ServiceFacadeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Loader for CSV and JSON based imports into Moqui services.
 * <p>
 * Key Features:
 * <ul>
 *     <li>Streams CSV/JSON to avoid holding entire file in memory.</li>
 *     <li>Creates and destroys temporary ExecutionContextImpl (ECI) after every N rows
 *         to prevent memory leaks from artifact execution history, caches and messages.</li>
 *     <li>Clears success/error messages after each service call to prevent unbounded growth.</li>
 *     <li>Collects errors in memory in small batches and writes them to disk periodically.</li>
 * </ul>
 */
public class MaargDataLoaderImpl {
    private static final Logger logger = LoggerFactory.getLogger(MaargDataLoaderImpl.class);

    private final ExecutionContextFactoryImpl ecfi;
    private final ServiceFacadeImpl sfi;

    private String location;
    private long failedRecordCount;
    private boolean disableEeca = false;
    private int transactionTimeout = 7200;
    private boolean disableAuditLog = false;
    private boolean disableFkCreate = false;
    private boolean disableDataFeed = true;

    private CSVFormat csvFormat = CSVFormat.Builder.create()
            .setHeader()
            .setCommentMarker('#')
            .setEscape('\\')
            .setIgnoreEmptyLines(true)
            .setIgnoreSurroundingSpaces(true)
            .get();

    private String serviceName;
    private String errorFileLocation;
    private String fileName;
    private String errorFileName;
    private List<String> csvFieldNames;
    private Map<String, Object> defaultValues;

    /**
     * Constructs a new Data Loader with required ExecutionContextFactoryImpl reference.
     *
     * @param ecfi Moqui ExecutionContextFactoryImpl
     */
    public MaargDataLoaderImpl(ExecutionContextFactoryImpl ecfi) {
        this.ecfi = ecfi;
        this.sfi = ecfi.serviceFacade;
    }


    /**
     * @return Path of the generated error file, if any
     */
    public String getErrorFileLocation() {
        return this.errorFileLocation;
    }

    /**
     * @return File name of the generated error file, if any
     */
    public String getErrorFileName() {
        return this.errorFileName;
    }

    /**
     * Sets the input file location.
     *
     * @param location File location path
     * @return current instance
     */
    public MaargDataLoaderImpl location(String location) {
        this.location = location;
        return this;
    }

    /**
     * Sets the input file location.
     *
     * @param fileName file name
     * @return current instance
     */
    public MaargDataLoaderImpl fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * Sets transaction timeout (in seconds).
     *
     * @param tt Timeout value
     * @return current instance
     */
    public MaargDataLoaderImpl transactionTimeout (int tt) {
        this.transactionTimeout = tt;
        return this;
    }

    /**
     * Enables or disables Entity EECA execution.
     *
     * @param disable true to disable, false to enable
     * @return current instance
     */
    public MaargDataLoaderImpl disableEntityEca(boolean disable) {
        this.disableEeca = disable;
        return this;
    }

    /**
     * Enables or disables audit log.
     *
     * @param disable true to disable, false to enable
     * @return current instance
     */
    public MaargDataLoaderImpl disableAuditLog(boolean disable) {
        this.disableAuditLog = disable;
        return this;
    }

    /**
     * Enables or disables audit log.
     *
     * @param disable true to disable, false to enable
     * @return current instance
     */
    public MaargDataLoaderImpl disableFkCreate(boolean disable) {
        this.disableFkCreate = disable;
        return this;
    }

    /**
     * Enables or disables audit log.
     *
     * @param disable true to disable, false to enable
     * @return current instance
     */
    public MaargDataLoaderImpl disableDataFeed(boolean disable) {
        this.disableDataFeed = disable;
        return this;
    }

    /**
     * Sets the CSV format for parsing.
     *
     * @param csvFormat Apache Commons CSV format
     * @return current instance
     */
    public MaargDataLoaderImpl csvFormat(CSVFormat csvFormat) {
        this.csvFormat = csvFormat;
        return this;
    }

    /**
     * Sets service name to invoke for each row.
     * Validates that the service is defined.
     *
     * @param serviceName Moqui service name
     * @return current instance
     */
    public MaargDataLoaderImpl serviceName(String serviceName) {
        if (serviceName == null || !sfi.isServiceDefined(serviceName)) {
            throw new IllegalArgumentException("Name " + serviceName + " is not a valid service name");
        }
        this.serviceName = serviceName;
        return this;
    }

    /**
     * Sets explicit CSV field names (optional).
     *
     * @param fieldNames CSV column names
     * @return current instance
     */

    public MaargDataLoaderImpl csvFieldNames(List<String> fieldNames) {
        this.csvFieldNames = fieldNames;
        return this;
    }

    /**
     * Sets default parameter values for service invocation.
     * These are merged with values from each row.
     *
     * @param defaultValues map of default parameters
     * @return current instance
     */
    public MaargDataLoaderImpl defaultValues(Map<String, Object> defaultValues) {
        if (this.defaultValues == null) {
            this.defaultValues = new HashMap<>();
        }
        this.defaultValues.putAll(defaultValues);
        return this;
    }

    /**
     * Loads the file from configured location.
     * Ensures ExecutionContextImpl is destroyed after execution to avoid memory leaks.
     */
    public long load() {
        if (fileName == null) {
            fileName = FilenameUtils.getName(location);
        }
        try {
            return loadSingleFile(location);
        } catch (Exception e) {
            throw new BaseException("Issue while loading data file " + e.getLocalizedMessage(), e);
        }
    }
    /**
     * Return the failed record count
     * @return failed record count
     */
    public long getFailedRecordCount() {
        return failedRecordCount;
    }

    /**
     * Loads the specific file using the appropriate handler (CSV or JSON)
     * and runs configured Moqui service for each row.
     *
     * @param location File location (CSV or JSON)
     */
    private long loadSingleFile(String location) throws IOException {
        ExecutionContextImpl eci = ecfi.getEci();
        long totalRecordCount = 0L;
        TransactionFacade tf = eci.transactionFacade;
        boolean beganTx = tf.begin(transactionTimeout);

        try (InputStream inputStream = ecfi.resourceFacade.getLocationStream(location)) {
            if (inputStream == null) {
                throw new BaseException("Data file not found at " + location);
            }

            try (DataHandler handler = getHandlerForFile(location)) {
                handler.fileName = fileName;
                logger.info("Loading data from {} ", location);
                long beforeTime = System.currentTimeMillis();
                handler.loadFile(location, inputStream);
                handler.flushErrorRecords(true);
                this.errorFileLocation = handler.errorFileLocation;
                this.errorFileName = handler.errorFileName;
                totalRecordCount = handler.getValuesRead();
                failedRecordCount = handler.getFailedRecordCount();
                logger.info("Processed {} records from {} in {} seconds, with {} records failed to process",
                        totalRecordCount,
                        location,
                        (System.currentTimeMillis() - beforeTime) / 1000,
                        failedRecordCount);
            }
            tf.commit(beganTx);
        } catch (Exception e) {
            tf.rollback(beganTx, "Error loading data from " + location, e);
        }

        if (eci.messageFacade.hasError()) {
            logger.error("Error messages loading data: " + eci.messageFacade.getErrorsString());
            eci.messageFacade.clearErrors();
            eci.messageFacade.clearAll();
        }
        eci.destroy();
        return totalRecordCount;
    }

    /**
     * Determines file handler (CSV or JSON) based on file extension.
     *
     * @param location file path
     * @return handler implementation
     */
    private DataHandler getHandlerForFile(String location) {
        String ext = location.toLowerCase();
        if (ext.endsWith(".csv")) {
            return new CsvHandler(this);
        }
        if (ext.endsWith(".json")) {
            return new JsonHandler(this);
        }
        throw new BaseException("Unsupported file type for: " + location);
    }

    // ================= Base Handler =================
    private abstract static class DataHandler implements AutoCloseable {
        protected final MaargDataLoaderImpl mdli;
        protected String errorFileLocation;
        protected String fileName;
        protected String errorFileName;
        protected String outputDirectory;
        protected long valuesRead = 0L;
        protected long failedRecordCount = 0L;
        protected ExecutionContextImpl eci;
        protected static final int BATCH_SIZE = 5000;

        public DataHandler(MaargDataLoaderImpl mdli) {
            this.mdli = mdli;
        }

        public long getValuesRead() {
            return valuesRead;
        }
        public long getFailedRecordCount() {
            return failedRecordCount;
        }

        public abstract void loadFile(String location, InputStream is) throws Exception;
        public abstract void flushErrorRecords(boolean forceFlush) throws Exception;
        @Override public void close() throws Exception {}

        /**
         * Initializes a temporary ECI and reapplies disable flags.
         * Called after every N rows to prevent memory leaks from long-lived context.
         */
        public ExecutionContextImpl initTempEci() {
            ExecutionContextImpl eci = this.mdli.ecfi.getEci();
            eci.userFacade.loginAnonymousIfNoUser();
            if (mdli.disableEeca) {
                eci.artifactExecutionFacade.disableEntityEca();
            }
            if (mdli.disableAuditLog) {
                eci.artifactExecutionFacade.disableEntityAuditLog();
            }
            if (mdli.disableFkCreate) {
                eci.artifactExecutionFacade.disableEntityFkCreate();
            }
            if (mdli.disableDataFeed) {
                eci.artifactExecutionFacade.disableEntityDataFeed();
            }
            return eci;
        }
    }

    // ================= CSV Handler =================
    private static class CsvHandler extends DataHandler {
        private final List<List<String>> errorRecords = new ArrayList<>();
        protected Map<String, Integer> headerMap = new LinkedHashMap<>();

        private BufferedWriter errorWriter;
        private final StringBuilder sb = new StringBuilder();
        public CsvHandler(MaargDataLoaderImpl mdli) {
            super(mdli);
        }

        private void initErrorFile() throws Exception {
            if (this.errorFileLocation != null) {
                return;
            }
            this.errorFileName = "Error_" + fileName;
            this.errorFileLocation = outputDirectory + MaargDataLoaderImpl.prepareErrorFileName(fileName);
            String outputFileLocation = mdli.ecfi.resourceFacade
                    .getLocationReference(this.errorFileLocation).getUrl().toURI().getPath();
            File outputFile = new File(outputFileLocation);
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            String headerLine = String.join(",", headerMap.keySet()) + ",ErrorMessage" + System.lineSeparator();
            errorWriter = new BufferedWriter(new FileWriter(outputFile), 64 * 1024);
            errorWriter.write(headerLine);
        }

        @Override
        public void close() throws Exception {
            flushErrorRecords(true);
            if (errorWriter != null) {
                errorWriter.flush();
                errorWriter.close();
                errorWriter = null;
            }
        }

        @Override
        public void loadFile(String location, InputStream is) throws Exception {
            CSVFormat format = mdli.csvFormat.builder().get();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 CSVParser parser = format.parse(reader)) {

                if (!parser.iterator().hasNext()) {
                    throw new BaseException("Not loading file [" + location + "], no data found");
                }

                if (!mdli.sfi.isServiceDefined(mdli.serviceName)) {
                    throw new BaseException("Service " + mdli.serviceName + " not found");
                }

                headerMap = (mdli.csvFieldNames != null && !mdli.csvFieldNames.isEmpty())
                        ? buildHeaderMap(mdli.csvFieldNames)
                        : parser.getHeaderMap();

                Map<String, Integer> normalizedHeaderMap = buildFlexibleHeaderMap(headerMap);

                outputDirectory = FilenameUtils.getFullPath(location);

                ExecutionContextImpl tempEci = initTempEci();
                for (CSVRecord record : parser) {
                    ServiceCallSyncImpl scs = (ServiceCallSyncImpl) mdli.sfi.sync()
                            .name(mdli.serviceName).requireNewTransaction(true).ignorePreviousError(true);
                    scs.parameter("_recordNumber", record.getRecordNumber());
                    if (mdli.defaultValues != null) {
                        scs.parameters(mdli.defaultValues);
                    }
                    for (Map.Entry<String, Integer> entry : normalizedHeaderMap.entrySet()) {
                        int idx = entry.getValue();
                        if (idx < record.size()) {
                            scs.parameter(entry.getKey(), record.get(idx));
                        }
                    }

                    try {
                        scs.disableAuthz().call();
                        if (tempEci.messageFacade.hasError()) {
                            addErrorRecord(record, tempEci.messageFacade.getErrorsString());
                            tempEci.messageFacade.clearErrors();
                        }
                        tempEci.messageFacade.clearAll();
                    } catch (Exception e) {
                        addErrorRecord(record, e.getMessage());
                    }
                    valuesRead++;
                    // Reset ECI periodically to prevent artifact stack & cache memory growth
                    if (valuesRead % 1000 == 0) {
                        tempEci.destroy();
                        tempEci = initTempEci();
                    }
                }
            }
        }
        /**
         * Normalizes a header to lowerCamelCase and stores both original and normalized forms
         * in the headerMap if they differ.
         *
         * @param rawHeaderMap the original header map from CSVParser
         * @return a new Map with normalized and/or original keys mapped to column index
         */
        private Map<String, Integer> buildFlexibleHeaderMap(Map<String, Integer> rawHeaderMap) {
            Map<String, Integer> flexibleHeaderMap = new LinkedHashMap<>();

            for (Map.Entry<String, Integer> entry : rawHeaderMap.entrySet()) {
                String original = entry.getKey();
                Integer index = entry.getValue();
                String normalized = normalizeToCamelCase(original);

                // Add normalized key
                flexibleHeaderMap.put(normalized, index);

                // Add original key if different
                if (!normalized.equals(original)) {
                    flexibleHeaderMap.put(original, index);
                }
            }
            return flexibleHeaderMap;
        }

        /*
         * Converts header name to lowerCamelCase.
         * Supports PascalCase, kebab-case, and Pascal-kebab formats.
         */
        private static String normalizeToCamelCase(String input) {
            if (input == null || input.isEmpty()) return input;

            if (input.contains("-")) {
                String[] parts = input.toLowerCase().split("-");
                StringBuilder sb = new StringBuilder(parts[0]);
                for (int i = 1; i < parts.length; i++) {
                    sb.append(Character.toUpperCase(parts[i].charAt(0)))
                            .append(parts[i].substring(1));
                }
                return sb.toString();
            }

            return Character.toLowerCase(input.charAt(0)) + input.substring(1);
        }

        private Map<String,Integer> buildHeaderMap(List<String> fields) {
            Map<String,Integer> map = new LinkedHashMap<>();
            for (int i = 0; i < fields.size(); i++) {
                map.put(fields.get(i), i);
            }
            return map;
        }

        private void addErrorRecord(CSVRecord record, String errorMessage) throws Exception {
            failedRecordCount++;
            List<String> errorRecord = new ArrayList<>(record.size() + 1);
            for (String val : record) {
                errorRecord.add(val);
            }
            errorRecord.add(errorMessage);
            errorRecords.add(errorRecord);
            if (errorRecords.size() >= BATCH_SIZE) {
                flushErrorRecords(false);
            }
        }

        @Override
        public void flushErrorRecords(boolean forceFlush) throws Exception {
            if (errorRecords.isEmpty()) {
                return;
            }
            if (this.errorFileLocation == null) {
                initErrorFile();
            }
            for (List<String> record : errorRecords) {
                sb.setLength(0);
                for (int i = 0; i < record.size(); i++) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    String value = record.get(i);
                    sb.append(value == null ? "" : value.replace(',', ' '));
                }
                errorWriter.write(sb.toString());
            }
            if (forceFlush) {
                errorWriter.flush();
            }
            errorRecords.clear();
        }
    }

    // ================= JSON Handler =================
    private static class JsonHandler extends DataHandler {
        private final List<Map<String, Object>> errorRecords = new ArrayList<>();
        private JsonGenerator generator;

        public JsonHandler(MaargDataLoaderImpl mdli) {
            super(mdli);
        }

        private void initErrorFile() throws Exception {
            if (this.errorFileLocation != null) {
                return;
            }
            this.errorFileName = "Error_" + fileName;
            this.errorFileLocation = outputDirectory + MaargDataLoaderImpl.prepareErrorFileName(fileName);
            String outputFileLocation = mdli.ecfi.resourceFacade.getLocationReference(this.errorFileLocation).getUrl().toURI().getPath();
            File outputFile = new File(outputFileLocation);
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            generator = ContextJavaUtil.jacksonMapper.getFactory()
                    .createGenerator(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8));
            generator.writeStartArray();
        }

        @Override
        public void close() throws Exception {
            flushErrorRecords(true);
            if (generator != null) {
                generator.writeEndArray();
                generator.close();
            }
        }

        @Override
        public void loadFile(String location, InputStream is) throws Exception {
            JsonFactory factory = new JsonFactory();
            ObjectMapper mapper = ContextJavaUtil.jacksonMapper;
            outputDirectory = FilenameUtils.getFullPath(location);
            try (JsonParser parser = factory.createParser(is)) {
                ExecutionContextImpl tempEci = initTempEci();
                if (parser.nextToken() != JsonToken.START_ARRAY) {
                    throw new BaseException("Expected JSON array at root in file: " + location);
                }
                int recordNumber = 0;
                while (parser.nextToken() == JsonToken.START_OBJECT) {
                    recordNumber++;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> value = mapper.readValue(parser, Map.class);
                    if (mdli.defaultValues != null) {
                        value.putAll(mdli.defaultValues);
                    }
                    value.put("_recordNumber", recordNumber);
                    try {
                        mdli.sfi.sync().name(mdli.serviceName)
                                .requireNewTransaction(true)
                                .parameters(value)
                                .disableAuthz()
                                .ignorePreviousError(true)
                                .call();
                        if (tempEci.messageFacade.hasError()) {
                            addErrorRecord(value, tempEci.messageFacade.getErrorsString());
                            tempEci.messageFacade.clearErrors();
                        }
                        tempEci.messageFacade.clearAll();
                    } catch (Exception e) {
                        addErrorRecord(value, e.getMessage());
                    }
                    valuesRead++;
                    if (valuesRead % 10000 == 0) {
                        tempEci.destroy();
                        tempEci = initTempEci();
                    }
                }
            }
        }

        private void addErrorRecord(Map<String, Object> errorRecord, String errorMessage) throws Exception {
            failedRecordCount++;
            errorRecord.put("_ERROR_MESSAGE_", errorMessage);
            errorRecords.add(errorRecord);
            if (errorRecords.size() >= BATCH_SIZE) {
                flushErrorRecords(false);
            }
        }

        @Override
        public void flushErrorRecords(boolean forceFlush) throws Exception {
            if (errorRecords.isEmpty()) {
                return;
            }
            if (this.errorFileLocation == null) {
                initErrorFile();
            }
            for (Map<String, Object> record : errorRecords) {
                ContextJavaUtil.jacksonMapper.writeValue(generator, record);
            }
            if (forceFlush) {
                generator.flush();
            }
            errorRecords.clear();
        }
    }

    /**
     * Generate timestamped error file name.
     *
     * @param fileName base file name
     * @return error file name
     */

    public static String prepareErrorFileName(String fileName) {
        return prepareFileName(fileName, "Error_");
    }

    /**
     * Generate timestamped error file name.
     *
     * @param fileName base file name
     * @param prefix file name prefix
     * @return  file name
     */
    public static String prepareFileName(String fileName, String prefix) {
        String baseName = FilenameUtils.getBaseName(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        String now = L10nFacadeImpl.formatTimestamp(new Timestamp(System.currentTimeMillis()), "yyyyMMddHHmmssSSS", null, null);

        StringBuilder finalName = new StringBuilder();
        if (prefix != null && !prefix.isEmpty()) {
            finalName.append(prefix);
        }
        finalName.append(baseName)
                .append("_")
                .append(now);
        if (extension != null && !extension.isEmpty()) {
            finalName.append(".").append(extension);
        }
        return finalName.toString();
    }
}
