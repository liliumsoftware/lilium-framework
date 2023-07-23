package ir.baho.framework.repository.specification;

import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Predicates {

    private final Predicate wherePredicate;
    private final Predicate havingPredicate;

    public static Predicates where(Predicate predicate) {
        return new Predicates(predicate, null);
    }

    public static Predicates having(Predicate predicate) {
        return new Predicates(null, predicate);
    }

}
