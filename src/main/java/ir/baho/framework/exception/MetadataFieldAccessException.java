package ir.baho.framework.exception;

import lombok.Getter;

@Getter
public class MetadataFieldAccessException extends RuntimeException {

    private final Class<?> type;
    private final String propertyName;

    public MetadataFieldAccessException(Class<?> type, String propertyName) {
        super("Can not access " + propertyName + " in " + type);
        this.type = type;
        this.propertyName = propertyName;
    }

}
