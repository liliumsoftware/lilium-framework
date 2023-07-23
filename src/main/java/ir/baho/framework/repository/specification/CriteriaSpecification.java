package ir.baho.framework.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@FunctionalInterface
public interface CriteriaSpecification<E> extends PredicateCriteriaSpecification<E> {

    void voidPredicate(Root<E> r, CriteriaQuery<?> cq, CriteriaBuilder cb, ExpressionPath p);

    @Override
    default Predicates predicate(Root<E> r, CriteriaQuery<?> cq, CriteriaBuilder cb, ExpressionPath p) {
        voidPredicate(r, cq, cb, p);
        return null;
    }

}
