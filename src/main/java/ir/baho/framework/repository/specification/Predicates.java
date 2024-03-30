package ir.baho.framework.repository.specification;

import jakarta.persistence.criteria.Predicate;

public record Predicates(Predicate wherePredicate, Predicate havingPredicate) {

    public static Predicates where(Predicate predicate) {
        return new Predicates(predicate, null);
    }

    public static Predicates having(Predicate predicate) {
        return new Predicates(null, predicate);
    }

}
