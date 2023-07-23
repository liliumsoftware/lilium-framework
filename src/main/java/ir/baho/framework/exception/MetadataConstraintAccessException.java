package ir.baho.framework.exception;

import ir.baho.framework.metadata.Constraint;
import lombok.Getter;

@Getter
public class MetadataConstraintAccessException extends RuntimeException {

    private final Class<?> type;
    private final String propertyName;
    private final Constraint constraint;

    public MetadataConstraintAccessException(Class<?> type, String propertyName, Constraint constraint) {
        super("Can not access " + constraint + " on " + propertyName + " in " + type);
        this.type = type;
        this.propertyName = propertyName;
        this.constraint = constraint;
    }

}
