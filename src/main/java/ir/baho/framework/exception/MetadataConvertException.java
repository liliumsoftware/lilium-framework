package ir.baho.framework.exception;

import lombok.Getter;

@Getter
public class MetadataConvertException extends RuntimeException {

    private final Class<?> type;
    private final String propertyName;
    private final Object value;

    public MetadataConvertException(Class<?> type, String propertyName, Object value) {
        super("Can not convert " + value + " to " + propertyName + " in " + type);
        this.type = type;
        this.propertyName = propertyName;
        this.value = value;
    }

    public MetadataConvertException(Class<?> type, Object value) {
        super("Can not convert " + value + " to " + type);
        this.type = type;
        this.propertyName = null;
        this.value = value;
    }

}
