package ir.baho.framework.repository.specification;

import ir.baho.framework.metadata.Constraint;

import java.util.Comparator;

@FunctionalInterface
public interface ComparatorPath<E> {

    void add(String field, Class<?> type, Comparator<E> comparator, Constraint... constraints);

}
