package ir.baho.framework.repository.specification;

import org.springframework.data.mongodb.core.query.Criteria;

@FunctionalInterface
public interface PredicateMongoSpecification {

    Criteria predicate(SimplePath p);

}
