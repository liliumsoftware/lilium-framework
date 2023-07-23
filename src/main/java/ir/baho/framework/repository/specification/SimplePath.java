package ir.baho.framework.repository.specification;

import ir.baho.framework.metadata.Constraint;

@FunctionalInterface
public interface SimplePath {

    void add(String field, Class<?> type, Constraint... constraints);

}
