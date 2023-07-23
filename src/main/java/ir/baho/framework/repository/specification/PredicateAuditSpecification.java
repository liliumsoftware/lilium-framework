package ir.baho.framework.repository.specification;

import org.hibernate.envers.query.criteria.AuditCriterion;

@FunctionalInterface
public interface PredicateAuditSpecification {

    AuditCriterion predicate(SimplePath p);

}
