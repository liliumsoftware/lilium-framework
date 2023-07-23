package ir.baho.framework.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class HttpError implements Serializable {

    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final List<Error> errors;

    public HttpError(HttpStatus status, String message) {
        this(status, message, List.of(new Error(message)));
    }

    public HttpError(HttpStatus status, String message, Error error) {
        this(status, message, List.of(error));
    }

    public HttpError(HttpStatus status, String message, List<Error> errors) {
        this.timestamp = LocalDateTime.now();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.errors = errors;
    }

}
