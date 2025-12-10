package ir.baho.framework.repository.impl;

import com.mongodb.BasicDBObject;
import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.domain.Entity;
import ir.baho.framework.exception.MetadataFieldAccessException;
import ir.baho.framework.metadata.Constraint;
import ir.baho.framework.metadata.Metadata;
import ir.baho.framework.metadata.PageMetadata;
import ir.baho.framework.metadata.ProjectionMetadata;
import ir.baho.framework.metadata.ProjectionPageMetadata;
import ir.baho.framework.metadata.Search;
import ir.baho.framework.repository.MongoCriteriaRepository;
import ir.baho.framework.repository.specification.PredicateAggregateSpecification;
import ir.baho.framework.repository.specification.PredicateMongoSpecification;
import ir.baho.framework.repository.specification.SimpleSelections;
import org.bson.types.ObjectId;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.SerializationUtils;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.support.PageableExecutionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoCriteriaRepositoryImpl<E extends Entity<?, ID>, ID extends Serializable & Comparable<ID>>
        extends SimpleMongoRepository<E, ID> implements MongoCriteriaRepository<E, ID> {

    private final MongoEntityInformation<E, ID> entityInformation;
    private final MongoOperations mongoOperations;
    private final List<StringConverter<? extends Comparable<?>>> converters;

    public MongoCriteriaRepositoryImpl(MongoEntityInformation<E, ID> entityInformation, MongoOperations mongoOperations,
                                       List<StringConverter<? extends Comparable<?>>> converters) {
        super(entityInformation, mongoOperations);
        this.entityInformation = entityInformation;
        this.mongoOperations = mongoOperations;
        this.converters = converters;
    }

    @Override
    public E findOne(Metadata metadata, PredicateMongoSpecification specification) {
        return mongoOperations.findOne(getQuery(metadata, specification).with(getSort(metadata)), this.entityInformation.getJavaType());
    }

    @Override
    public Page<E> findAll(PageMetadata metadata, PredicateMongoSpecification specification) {
        Query query = getQuery(metadata, specification);
        Pageable pageable = getPageable(metadata);
        return PageableExecutionUtils.getPage(mongoOperations.find(query.with(pageable), this.entityInformation.getJavaType()),
                pageable, () -> mongoOperations.count(query.limit(-1).skip(-1), this.entityInformation.getJavaType()));
    }

    @Override
    public List<E> findAll(Metadata metadata, PredicateMongoSpecification specification) {
        return mongoOperations.find(getQuery(metadata, specification).with(getSort(metadata)), this.entityInformation.getJavaType());
    }

    @Override
    public <P> P findOne(Metadata metadata, Class<P> projection, PredicateMongoSpecification specification, SimpleSelections selections) {
        Query query = getQuery(metadata, specification).with(getSort(metadata));
        query.fields().include(selections.apply().keySet().toArray(String[]::new));
        return mongoOperations.query(this.entityInformation.getJavaType()).as(projection).matching(query).firstValue();
    }

    @Override
    public <P> Page<P> findAll(PageMetadata metadata, Class<P> projection, PredicateMongoSpecification specification, SimpleSelections selections) {
        Query query = getQuery(metadata, specification);
        Pageable pageable = getPageable(metadata);
        query.fields().include(selections.apply().keySet().toArray(String[]::new));
        return PageableExecutionUtils.getPage(mongoOperations.query(this.entityInformation.getJavaType()).as(projection).matching(query.with(pageable)).all(),
                pageable, () -> mongoOperations.count(query.limit(-1).skip(-1), this.entityInformation.getJavaType()));
    }

    @Override
    public <P> List<P> findAll(Metadata metadata, Class<P> projection, PredicateMongoSpecification specification, SimpleSelections selections) {
        Query query = getQuery(metadata, specification).with(getSort(metadata));
        query.fields().include(selections.apply().keySet().toArray(String[]::new));
        return mongoOperations.query(this.entityInformation.getJavaType()).as(projection).matching(query).all();
    }

    @Override
    public Map<String, Object> findOne(ProjectionMetadata metadata, PredicateMongoSpecification specification, SimpleSelections selections) {
        Query query = getQuery(metadata, specification).with(getSort(metadata));
        query.fields().include(getSelections(metadata.getField(), selections.apply().keySet()));
        return mongoOperations.query(this.entityInformation.getJavaType()).as(BasicDBObject.class).matching(query).first()
                .map(o -> o.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new)))
                .orElseThrow(() -> new EmptyResultDataAccessException(1));
    }

    @Override
    public Page<Map<String, Object>> findAll(ProjectionPageMetadata metadata, PredicateMongoSpecification specification, SimpleSelections selections) {
        Query query = getQuery(metadata, specification);
        Pageable pageable = getPageable(metadata);
        query.fields().include(getSelections(metadata.getField(), selections.apply().keySet()));
        return PageableExecutionUtils.getPage(mongoOperations.query(this.entityInformation.getJavaType()).as(BasicDBObject.class).matching(query.with(pageable)).all()
                        .stream().map(o -> o.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new))).collect(Collectors.toList()),
                pageable, () -> mongoOperations.count(query.limit(-1).skip(-1), this.entityInformation.getJavaType()));
    }

    @Override
    public List<Map<String, Object>> findAll(ProjectionMetadata metadata, PredicateMongoSpecification specification, SimpleSelections selections) {
        Query query = getQuery(metadata, specification).with(getSort(metadata));
        query.fields().include(getSelections(metadata.getField(), selections.apply().keySet()));
        return mongoOperations.query(this.entityInformation.getJavaType()).as(BasicDBObject.class).matching(query).all()
                .stream().map(o -> o.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new))).collect(Collectors.toList());
    }

    @Override
    public <P> P findOne(Metadata metadata, Class<P> projection, PredicateAggregateSpecification specification) {
        Aggregation aggregation = Aggregation.newAggregation(getAggregationOperations(metadata, projection, specification));
        return mongoOperations.aggregate(aggregation, this.entityInformation.getJavaType(), projection).getUniqueMappedResult();
    }

    @Override
    public <P> Page<P> findAll(PageMetadata metadata, Class<P> projection, PredicateAggregateSpecification specification) {
        List<AggregationOperation> aggregationOperations = getAggregationOperations(metadata, projection, specification);
        List<AggregationOperation> countAggregationOperations = new ArrayList<>(aggregationOperations);
        countAggregationOperations.add(Aggregation.count().as("count"));
        aggregationOperations.add(Aggregation.skip((long) metadata.getPage() * metadata.getSize()));
        aggregationOperations.add(Aggregation.limit(metadata.getSize()));
        return PageableExecutionUtils.getPage(mongoOperations.aggregate(Aggregation.newAggregation(aggregationOperations), this.entityInformation.getJavaType(), projection).getMappedResults(),
                getPageable(metadata), () -> mongoOperations.aggregate(Aggregation.newAggregation(countAggregationOperations), this.entityInformation.getJavaType(), BasicDBObject.class).getUniqueMappedResult().getLong("count"));
    }

    @Override
    public <P> List<P> findAll(Metadata metadata, Class<P> projection, PredicateAggregateSpecification specification) {
        Aggregation aggregation = Aggregation.newAggregation(getAggregationOperations(metadata, projection, specification));
        return mongoOperations.aggregate(aggregation, this.entityInformation.getJavaType(), projection).getMappedResults();
    }

    protected Query getQuery(Metadata metadata, PredicateMongoSpecification specification) {
        Map<String, Class<?>> paths = new LinkedHashMap<>();
        Map<String, Constraint[]> constraints = new LinkedHashMap<>();
        Criteria c = specification.predicate((path, type, constraint) -> {
            paths.put(path, type);
            if (constraint == null || constraint.length == 0) {
                constraint = Constraint.all();
            }
            constraints.put(path, constraint);
        });
        List<Criteria> criteria = new ArrayList<>();
        if (c != null) {
            criteria.add(c);
        }
        if (metadata.getSearch() != null) {
            if (metadata.notConverted()) {
                convert(metadata, paths, constraints, entityInformation.getJavaType(), converters);
            }
            for (Search field : metadata.getSearch()) {
                String fieldName = field.getField();
                Comparable<?> fieldValue = field.getValue();
                Comparable<?> fieldAnother = field.getAnother();
                List<Comparable<?>> fieldValues = field.getValues();
                if (fieldName.endsWith(".id")) {
                    fieldName = fieldName.substring(0, fieldName.length() - 3) + ".$id";
                    if (fieldValue != null) {
                        fieldValue = new ObjectId(fieldValue.toString());
                    }
                    if (fieldAnother != null) {
                        fieldAnother = new ObjectId(fieldAnother.toString());
                    }
                    if (fieldValues != null) {
                        fieldValues = fieldValues.stream().map(f -> new ObjectId(f.toString())).collect(Collectors.toList());
                    }
                }
                switch (field.getConstraint()) {
                    case IS_NULL -> criteria.add(Criteria.where(fieldName).is(null));
                    case IS_NOT_NULL -> criteria.add(Criteria.where(fieldName).ne(null));
                    case EQUALS -> criteria.add(Criteria.where(fieldName).is(fieldValue));
                    case NOT_EQUALS -> criteria.add(Criteria.where(fieldName).ne(fieldValue));
                    case EQUALS_IGNORE_CASE ->
                            criteria.add(Criteria.where(fieldName).regex("^" + fieldValue + "$", "i"));
                    case NOT_EQUALS_IGNORE_CASE ->
                            criteria.add(Criteria.where(fieldName).not().regex("^" + fieldValue + "$", "i"));
                    case GREATER_THAN -> criteria.add(Criteria.where(fieldName).gt(fieldValue));
                    case LESS_THAN -> criteria.add(Criteria.where(fieldName).lt(fieldValue));
                    case GREATER_THAN_OR_EQUALS -> criteria.add(Criteria.where(fieldName).gte(fieldValue));
                    case LESS_THAN_OR_EQUALS -> criteria.add(Criteria.where(fieldName).lte(fieldValue));
                    case CONTAINS -> criteria.add(Criteria.where(fieldName).regex(".*" + fieldValue + ".*"));
                    case NOT_CONTAINS -> criteria.add(Criteria.where(fieldName).not().regex(".*" + fieldValue + ".*"));
                    case STARTS_WITH -> criteria.add(Criteria.where(fieldName).regex("^" + fieldValue));
                    case NOT_STARTS_WITH -> criteria.add(Criteria.where(fieldName).not().regex("^" + fieldValue));
                    case ENDS_WITH -> criteria.add(Criteria.where(fieldName).regex(fieldValue + "$"));
                    case NOT_ENDS_WITH -> criteria.add(Criteria.where(fieldName).not().regex(fieldValue + "$"));
                    case CONTAINS_IGNORE_CASE ->
                            criteria.add(Criteria.where(fieldName).regex(".*" + fieldValue + ".*", "i"));
                    case NOT_CONTAINS_IGNORE_CASE ->
                            criteria.add(Criteria.where(fieldName).not().regex(".*" + fieldValue + ".*", "i"));
                    case STARTS_WITH_IGNORE_CASE ->
                            criteria.add(Criteria.where(fieldName).regex("^" + fieldValue, "i"));
                    case NOT_STARTS_WITH_IGNORE_CASE ->
                            criteria.add(Criteria.where(fieldName).not().regex("^" + fieldValue, "i"));
                    case ENDS_WITH_IGNORE_CASE -> criteria.add(Criteria.where(fieldName).regex(fieldValue + "$", "i"));
                    case NOT_ENDS_WITH_IGNORE_CASE ->
                            criteria.add(Criteria.where(fieldName).not().regex(fieldValue + "$", "i"));
                    case BETWEEN -> criteria.add(Criteria.where(fieldName).gte(fieldValue).lte(fieldAnother));
                    case NOT_BETWEEN ->
                            criteria.add(Criteria.where(fieldName).lt(fieldValue).orOperator(Criteria.where(fieldName).gt(fieldAnother)));
                    case IN -> criteria.add(Criteria.where(fieldName).in(fieldValues));
                    case NOT_IN -> criteria.add(Criteria.where(fieldName).not().in(fieldValues));
                }
            }
        }
        if (criteria.isEmpty()) {
            return new Query();
        }
        if (metadata.isAnd()) {
            return Query.query(new Criteria().andOperator(criteria.toArray(Criteria[]::new)));
        } else {
            return Query.query(new Criteria().orOperator(criteria.toArray(Criteria[]::new)));
        }
    }

    protected List<AggregationOperation> getAggregationOperations(Metadata metadata, Class<?> projection, PredicateAggregateSpecification specification) {
        Map<String, Class<?>> paths = new LinkedHashMap<>();
        Map<String, Constraint[]> constraints = new LinkedHashMap<>();
        List<AggregationOperation> aggregationOperations = specification.predicate((path, type, constraint) -> {
            paths.put(path, type);
            if (constraint == null || constraint.length == 0) {
                constraint = Constraint.all();
            }
            constraints.put(path, constraint);
        });
        Criteria c = null;
        if (metadata.getSearch() != null) {
            if (metadata.notConverted()) {
                convert(metadata, paths, constraints, projection, converters);
            }
            for (Search field : metadata.getSearch()) {
                String fieldName = field.getField();
                Comparable<?> fieldValue = field.getValue();
                Comparable<?> fieldAnother = field.getAnother();
                List<Comparable<?>> fieldValues = field.getValues();
                if (fieldName.endsWith(".id")) {
                    fieldName = fieldName.substring(0, fieldName.length() - 3) + ".$id";
                    if (fieldValue != null) {
                        fieldValue = new ObjectId(fieldValue.toString());
                    }
                    if (fieldAnother != null) {
                        fieldAnother = new ObjectId(fieldAnother.toString());
                    }
                    if (fieldValues != null) {
                        fieldValues = fieldValues.stream().map(f -> new ObjectId(f.toString())).collect(Collectors.toList());
                    }
                }
                switch (field.getConstraint()) {
                    case IS_NULL ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).is(null)) : c.orOperator(Criteria.where(fieldName).is(null)) : Criteria.where(fieldName).is(null);
                    case IS_NOT_NULL ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).ne(null)) : c.orOperator(Criteria.where(fieldName).ne(null)) : Criteria.where(fieldName).ne(null);
                    case EQUALS ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).is(fieldValue)) : c.orOperator(Criteria.where(fieldName).is(fieldValue)) : Criteria.where(fieldName).is(fieldValue);
                    case NOT_EQUALS ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).ne(fieldValue)) : c.orOperator(Criteria.where(fieldName).ne(fieldValue)) : Criteria.where(fieldName).ne(fieldValue);
                    case EQUALS_IGNORE_CASE ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).regex("^" + fieldValue + "$", "i")) : c.orOperator(Criteria.where(fieldName).regex("^" + fieldValue + "$", "i")) : Criteria.where(fieldName).regex("^" + fieldValue + "$", "i");
                    case NOT_EQUALS_IGNORE_CASE ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).not().regex("^" + fieldValue + "$", "i")) : c.orOperator(Criteria.where(fieldName).not().regex("^" + fieldValue + "$", "i")) : Criteria.where(fieldName).not().regex("^" + fieldValue + "$", "i");
                    case GREATER_THAN ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).gt(fieldValue)) : c.orOperator(Criteria.where(fieldName).gt(fieldValue)) : Criteria.where(fieldName).gt(fieldValue);
                    case LESS_THAN ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).lt(fieldValue)) : c.orOperator(Criteria.where(fieldName).lt(fieldValue)) : Criteria.where(fieldName).lt(fieldValue);
                    case GREATER_THAN_OR_EQUALS ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).gte(fieldValue)) : c.orOperator(Criteria.where(fieldName).gte(fieldValue)) : Criteria.where(fieldName).gte(fieldValue);
                    case LESS_THAN_OR_EQUALS ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).lte(fieldValue)) : c.orOperator(Criteria.where(fieldName).lte(fieldValue)) : Criteria.where(fieldName).lte(fieldValue);
                    case CONTAINS ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).regex(".*" + fieldValue + ".*")) : c.orOperator(Criteria.where(fieldName).regex(".*" + fieldValue + ".*")) : Criteria.where(fieldName).regex(".*" + fieldValue + ".*");
                    case NOT_CONTAINS ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).not().regex(".*" + fieldValue + ".*")) : c.orOperator(Criteria.where(fieldName).not().regex(".*" + fieldValue + ".*")) : Criteria.where(fieldName).not().regex(".*" + fieldValue + ".*");
                    case STARTS_WITH ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).regex("^" + fieldValue)) : c.orOperator(Criteria.where(fieldName).regex("^" + fieldValue)) : Criteria.where(fieldName).regex("^" + fieldValue);
                    case NOT_STARTS_WITH ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).not().regex("^" + fieldValue)) : c.orOperator(Criteria.where(fieldName).not().regex("^" + fieldValue)) : Criteria.where(fieldName).not().regex("^" + fieldValue);
                    case ENDS_WITH ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).regex(fieldValue + "$")) : c.orOperator(Criteria.where(fieldName).regex(fieldValue + "$")) : Criteria.where(fieldName).regex(fieldValue + "$");
                    case NOT_ENDS_WITH ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).not().regex(fieldValue + "$")) : c.orOperator(Criteria.where(fieldName).not().regex(fieldValue + "$")) : Criteria.where(fieldName).not().regex(fieldValue + "$");
                    case CONTAINS_IGNORE_CASE ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).regex(".*" + fieldValue + ".*", "i")) : c.orOperator(Criteria.where(fieldName).regex(".*" + fieldValue + ".*", "i")) : Criteria.where(fieldName).regex(".*" + fieldValue + ".*", "i");
                    case NOT_CONTAINS_IGNORE_CASE ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).not().regex(".*" + fieldValue + ".*", "i")) : c.orOperator(Criteria.where(fieldName).not().regex(".*" + fieldValue + ".*", "i")) : Criteria.where(fieldName).not().regex(".*" + fieldValue + ".*", "i");
                    case STARTS_WITH_IGNORE_CASE ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).regex("^" + fieldValue, "i")) : c.orOperator(Criteria.where(fieldName).regex("^" + fieldValue, "i")) : Criteria.where(fieldName).regex("^" + fieldValue, "i");
                    case NOT_STARTS_WITH_IGNORE_CASE ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).not().regex("^" + fieldValue, "i")) : c.orOperator(Criteria.where(fieldName).not().regex("^" + fieldValue, "i")) : Criteria.where(fieldName).not().regex("^" + fieldValue, "i");
                    case ENDS_WITH_IGNORE_CASE ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).regex(fieldValue + "$", "i")) : c.orOperator(Criteria.where(fieldName).regex(fieldValue + "$", "i")) : Criteria.where(fieldName).regex(fieldValue + "$", "i");
                    case NOT_ENDS_WITH_IGNORE_CASE ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).not().regex(fieldValue + "$", "i")) : c.orOperator(Criteria.where(fieldName).not().regex(fieldValue + "$", "i")) : Criteria.where(fieldName).not().regex(fieldValue + "$", "i");
                    case BETWEEN ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).gte(fieldValue).lte(fieldAnother)) : c.orOperator(Criteria.where(fieldName).gte(fieldValue).lte(fieldAnother)) : Criteria.where(fieldName).gte(fieldValue).lte(fieldAnother);
                    case NOT_BETWEEN ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).lt(fieldValue).orOperator(Criteria.where(fieldName).gt(fieldAnother))) : c.orOperator(Criteria.where(fieldName).lt(fieldValue).orOperator(Criteria.where(fieldName).gt(fieldAnother))) : Criteria.where(fieldName).lt(fieldValue).orOperator(Criteria.where(fieldName).gt(fieldAnother));
                    case IN ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).in(fieldValues)) : c.orOperator(Criteria.where(fieldName).in(fieldValues)) : Criteria.where(fieldName).in(fieldValues);
                    case NOT_IN ->
                            c = c != null ? metadata.isAnd() ? c.andOperator(Criteria.where(fieldName).not().in(fieldValues)) : c.orOperator(Criteria.where(fieldName).not().in(fieldValues)) : Criteria.where(fieldName).not().in(fieldValues);
                }
            }
        }
        aggregationOperations = new ArrayList<>(aggregationOperations);
        if (c != null) {
            aggregationOperations.add(Aggregation.match(c));
        }
        Sort sort = getSort(metadata);
        if (sort.isSorted()) {
            aggregationOperations.add(Aggregation.sort(sort));
        }
        return aggregationOperations;
    }

    protected String[] getSelections(String[] fields, Collection<? extends String> selections) {
        List<String> paths = new ArrayList<>();
        for (String field : fields) {
            String selection = null;
            for (String path : selections) {
                if (path.equals(field)) {
                    selection = path;
                }
            }
            if (selection == null) {
                throw new MetadataFieldAccessException(entityInformation.getJavaType(), field);
            }
            paths.add(selection);
        }
        return paths.toArray(String[]::new);
    }

    protected String getQueryString(Query query) {
        String queryString = "{\n"
                + "  'collectionName' : '" + mongoOperations.getCollectionName(entityInformation.getJavaType()) + "' ,\n"
                + "  'findQuery' : " + SerializationUtils.serializeToJsonSafely(query.getQueryObject()) + " ,\n"
                + "  'findFields' : " + SerializationUtils.serializeToJsonSafely(query.getFieldsObject());
        if (query.isSorted()) {
            queryString += " ,\n  'sort' : " + SerializationUtils.serializeToJsonSafely(query.getSortObject());
        }
        queryString += "\n}";
        return queryString;
    }

    protected String getQueryString(Aggregation aggregation) {
        return "{\n"
                + "  'collectionName' : '" + mongoOperations.getCollectionName(entityInformation.getJavaType()) + "' ,\n"
                + "  'aggregate' : " + SerializationUtils.serializeToJsonSafely(aggregation.toDocument(entityInformation.getCollectionName(), Aggregation.DEFAULT_CONTEXT).get("pipeline")) + "\n"
                + "}";
    }

}
