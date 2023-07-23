package ir.baho.framework.repository.specification;

import ir.baho.framework.metadata.Constraint;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;

public interface ExpressionPath {

    void add(Path<?> path, Constraint... constraints);

    void exp(Expression<?> expression, String name, Constraint... constraints);

    void func(Expression<?> expression, String name, Constraint... constraints);

    void summary(Expression<?> expression, String name);

}
