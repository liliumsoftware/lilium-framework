package ir.baho.framework.repository.specification;

import org.springframework.data.mongodb.core.query.Criteria;

@FunctionalInterface
public interface MongoSpecification extends PredicateMongoSpecification {

    void voidPredicate(SimplePath p);

    @Override
    default Criteria predicate(SimplePath p) {
        voidPredicate(p);
        return null;
    }

}
