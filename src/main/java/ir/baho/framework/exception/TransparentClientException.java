package ir.baho.framework.exception;

import lombok.Getter;

@Getter
public class TransparentClientException extends RuntimeException {

    private final int status;

    public TransparentClientException(int status, String message) {
        super(message);
        this.status = status;
    }

}
