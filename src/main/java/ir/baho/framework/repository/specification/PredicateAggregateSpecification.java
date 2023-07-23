package ir.baho.framework.repository.specification;

import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.List;

@FunctionalInterface
public interface PredicateAggregateSpecification {

    List<AggregationOperation> predicate(SimplePath p);

}
