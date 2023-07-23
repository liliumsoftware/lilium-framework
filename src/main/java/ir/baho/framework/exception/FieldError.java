package ir.baho.framework.exception;

import lombok.Getter;

@Getter
public class FieldError extends ObjectError {

    private final String field;
    private final Object rejectedValue;
    private final String[] codes;

    public FieldError(String defaultMessage, String objectName, String field, Object rejectedValue, String... codes) {
        super(defaultMessage, objectName);
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.codes = codes;
    }

}
