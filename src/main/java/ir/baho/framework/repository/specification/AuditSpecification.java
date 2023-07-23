package ir.baho.framework.repository.specification;

import org.hibernate.envers.query.criteria.AuditCriterion;

@FunctionalInterface
public interface AuditSpecification extends PredicateAuditSpecification {

    void voidPredicate(SimplePath p);

    @Override
    default AuditCriterion predicate(SimplePath p) {
        voidPredicate(p);
        return null;
    }

}
