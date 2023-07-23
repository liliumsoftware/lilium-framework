package ir.baho.framework.exception;

import lombok.Getter;

@Getter
public class ObjectError extends Error {

    private final String objectName;

    public ObjectError(String defaultMessage, String objectName) {
        super(defaultMessage);
        this.objectName = objectName;
    }

}
