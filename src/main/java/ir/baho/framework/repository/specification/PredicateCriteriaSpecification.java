package ir.baho.framework.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@FunctionalInterface
public interface PredicateCriteriaSpecification<E> {

    Predicates predicate(Root<E> r, CriteriaQuery<?> cq, CriteriaBuilder cb, ExpressionPath e);

}
