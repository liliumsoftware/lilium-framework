package ir.baho.framework.repository.impl.mongo;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationAlternate;
import com.mongodb.client.model.CollationCaseFirst;
import com.mongodb.client.model.CollationMaxVariable;
import com.mongodb.client.model.CollationStrength;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
public class MongoDbQuery {

    private static final String FIND_QUERY_KEY = "findQuery";
    private static final String FIND_QUERY_REGEXP_KEY = "findQueryRegEx";
    private static final String FIND_FIELDS_KEY = "findFields";
    private static final String SORT_KEY = "sort";
    private static final String LIMIT_KEY = "limit";
    private static final String COLLECTION_NAME_KEY = "collectionName";
    private static final String ROWS_TO_PROCESS_KEY = "rowsToProcess";
    private static final String RUN_COMMAND_KEY = "runCommand";
    private static final String RUN_AGGREGATE_KEY = "aggregate";
    private static final String RUN_AGGREGATE_PIPELINE_KEY = "pipeline";
    private static final String BATCH_SIZE_KEY = "batchSize";
    private static final String COLLATION_KEY = "collation";
    private static final String MAXTIME_KEY = "maxTime";

    private final MongoDatabase mongoDatabase;
    private final Map<String, Object> parameters;
    public MongoCursor<?> iterator;
    public BasicDBObject queryObject;
    public int rowsToProcess;
    public List<?> commandResults;

    public MongoDbQuery(String queryString, MongoDatabase mongoDatabase, Map<String, Object> parameters) throws JRException {
        this.rowsToProcess = 5;
        this.mongoDatabase = mongoDatabase;
        this.parameters = parameters;
        this.processQuery(queryString);
    }

    public void processQuery(String queryString) throws JRException {
        log.info("Processing mongoDB query");
        if (queryString == null || queryString.isBlank()) {
            throw new JRException("Query is empty");
        }
        if (queryString.startsWith("\"")) {
            queryString = queryString.substring(1);
        }
        if (queryString.endsWith("\"")) {
            queryString = queryString.substring(0, queryString.length() - 1);
        }
        BasicDBObject parseResult = BasicDBObject.parse(queryString);
        log.debug("Query: " + queryString);
        this.fixQueryObject(this.queryObject = parseResult, this.parameters);
        if (this.queryObject.containsField(RUN_COMMAND_KEY)) {
            BasicDBObject command = (BasicDBObject) this.queryObject.removeField(RUN_COMMAND_KEY);
            if (command.containsField(RUN_AGGREGATE_KEY)) {
                this.runAggregate(command.removeField(RUN_AGGREGATE_PIPELINE_KEY), command.getString(RUN_AGGREGATE_KEY));
                log.warn("runCommand aggregations are deprecated ! use API Driven query for aggregation in future");
            }
        } else {
            this.createIterator();
        }
        if (this.queryObject.containsField(ROWS_TO_PROCESS_KEY)) {
            Integer value = this.processInteger(this.queryObject.get(ROWS_TO_PROCESS_KEY));
            if (value != null) {
                this.rowsToProcess = value;
            }
        }
        if (this.rowsToProcess == 0) {
            this.rowsToProcess = Integer.MAX_VALUE;
        }
    }

    private Object fixQueryObject(BasicDBObject queryObjectToFix, Map<String, Object> reportParameters) {
        Set<String> keySet = queryObjectToFix.keySet();
        if (keySet.size() == 1) {
            String key = keySet.iterator().next();
            if (reportParameters.containsKey(key) && queryObjectToFix.get(key) == null) {
                return reportParameters.get(key);
            }
        }
        for (String key : queryObjectToFix.keySet()) {
            Object value = queryObjectToFix.get(key);
            if (value instanceof BasicDBObject) {
                queryObjectToFix.put(key, this.fixQueryObject((BasicDBObject) value, reportParameters));
            }
        }
        return queryObjectToFix;
    }

    private void runAggregate(Object commandValue, String collectionName) throws JRException {
        if (!(commandValue instanceof BasicDBList)) {
            throw new JRException("Command must be a valid BSON object");
        }
        ArrayList<Bson> commandObject = new ArrayList<>(((BasicDBList) commandValue).size());
        ((BasicDBList) commandValue).forEach(command -> commandObject.add((Bson) command));
        log.debug("Command object: " + commandObject);
        if (collectionName != null && !collectionName.isEmpty()) {
            this.iterator = this.mongoDatabase.getCollection(collectionName).aggregate(commandObject, BasicDBObject.class).iterator();
        } else {
            this.iterator = this.mongoDatabase.aggregate(commandObject, BasicDBObject.class).iterator();
        }
    }

    private void createIterator() throws JRException {
        log.info(this.queryObject.toString());
        if (!this.queryObject.containsField(COLLECTION_NAME_KEY)) {
            throw new JRException("\"collectionName\" must be part of the query object");
        }
        BasicDBObject findQueryObject = (BasicDBObject) this.queryObject.get(FIND_QUERY_KEY);
        if (findQueryObject == null) {
            findQueryObject = new BasicDBObject();
        }
        if (this.queryObject.containsField(FIND_QUERY_REGEXP_KEY)) {
            BasicDBObject regExpObject = (BasicDBObject) this.queryObject.get(FIND_QUERY_REGEXP_KEY);
            for (String key : regExpObject.keySet()) {
                String value = (String) regExpObject.get(key);
                if (!value.startsWith("/")) {
                    throw new JRException("Regular expressions must start with: /");
                }
                value = value.substring(1);
                if (!value.contains("/")) {
                    throw new JRException("No ending symbol found: /");
                }
                int index = value.lastIndexOf("/");
                String flags = null;
                if (index != value.length() - 1) {
                    flags = value.substring(index + 1);
                }
                value = value.substring(0, index);
                findQueryObject.put(key, Pattern.compile(((flags != null) ? ("(?" + flags + ")") : "") + value));
            }
        }
        MongoCollection<?> collection = mongoDatabase.getCollection((String) this.queryObject.removeField(COLLECTION_NAME_KEY));
        Integer limitValue = 0;
        Integer batchSize = 0;
        long maxTime = 0L;
        Collation collation = null;
        if (this.queryObject.containsField(LIMIT_KEY)) {
            limitValue = this.processInteger(this.queryObject.getInt(LIMIT_KEY, 0));
        }
        if (this.queryObject.containsField(BATCH_SIZE_KEY)) {
            batchSize = this.processInteger(this.queryObject.getInt(BATCH_SIZE_KEY, 0));
        }
        if (this.queryObject.containsField(MAXTIME_KEY)) {
            maxTime = this.queryObject.getLong(MAXTIME_KEY, 0L);
        }
        if (this.queryObject.containsField(COLLATION_KEY)) {
            if (this.queryObject.get(COLLATION_KEY) == null) {
                throw new JRException("Collation document error: no document !");
            }
            Document col = Document.parse(this.queryObject.getString(COLLATION_KEY));
            if (col.getString("locale") == null) {
                throw new JRException("Collation document require locale to be present.");
            }
            collation = Collation.builder().locale(col.getString("locale")).collationStrength(CollationStrength.fromInt(col.getInteger("strength", 3))).caseLevel(col.getBoolean("caseLevel", false)).collationCaseFirst(CollationCaseFirst.fromString((String) col.get("caseFirst", (Object) "off"))).numericOrdering(col.getBoolean("numericOrdering", false)).collationAlternate(CollationAlternate.fromString((String) col.get("alternate", (Object) "non-ignorable"))).collationMaxVariable(CollationMaxVariable.fromString((String) col.get("maxVariable", (Object) "punct"))).backwards(col.getBoolean("backwards", false)).normalization(col.getBoolean("normalization", false)).build();
        }
        if (this.queryObject.containsField(RUN_AGGREGATE_KEY)) {
            Object commandValue = this.queryObject.get(RUN_AGGREGATE_KEY);
            ArrayList<Bson> commandObject = new ArrayList<>(((BasicDBList) commandValue).size());
            ((BasicDBList) commandValue).forEach(command -> commandObject.add((Bson) command));
            AggregateIterable<?> aggregateIterable = collection.aggregate(commandObject, BasicDBObject.class)
                    .batchSize(batchSize).maxTime(maxTime, TimeUnit.MILLISECONDS).collation(collation);
            this.iterator = aggregateIterable.iterator();
        } else {
            FindIterable<?> findIterable = collection.find(findQueryObject, BasicDBObject.class)
                    .projection((Bson) this.queryObject.get(FIND_FIELDS_KEY))
                    .limit(limitValue).collation(collation)
                    .batchSize(batchSize).maxTime(maxTime, TimeUnit.MILLISECONDS)
                    .sort((Bson) this.queryObject.get(SORT_KEY));
            this.iterator = findIterable.iterator();
        }
    }

    private Integer processInteger(Object value) {
        try {
            if (value instanceof Integer) {
                return (Integer) value;
            }
            return Integer.parseInt((String) value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

}
