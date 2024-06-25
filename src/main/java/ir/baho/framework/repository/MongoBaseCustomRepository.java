package ir.baho.framework.repository;

import com.mongodb.BasicDBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class MongoBaseCustomRepository {

    @Autowired
    protected MongoTemplate mongoTemplate;

    protected <E> Page<E> getPage(Class<E> clas, Query query, Pageable pageable) {
        return PageableExecutionUtils.getPage(mongoTemplate.find(query.with(pageable), clas), pageable,
                () -> mongoTemplate.count(query.limit(-1).skip(-1), clas));
    }

    protected <E, P> Page<E> getPage(Class<E> clas, Class<P> projection, List<AggregationOperation> aggregationOperations, Pageable pageable) {
        aggregationOperations = new ArrayList<>(aggregationOperations);
        List<AggregationOperation> countAggregationOperations = new ArrayList<>(aggregationOperations);
        countAggregationOperations.add(Aggregation.count().as("count"));
        aggregationOperations.add(Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()));
        aggregationOperations.add(Aggregation.limit(pageable.getPageSize()));
        return PageableExecutionUtils.getPage(mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperations), projection, clas).getMappedResults(),
                pageable, () -> mongoTemplate.aggregate(Aggregation.newAggregation(countAggregationOperations), projection, BasicDBObject.class).getUniqueMappedResult().getLong("count"));
    }

}
