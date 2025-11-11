package ir.baho.framework.exception;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class ValidationException extends RuntimeException {

    private Map<String, List<Object>> keyParams;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String key, Object... params) {
        super(params.length == 0 ? key : key + ": " + Arrays.asList(params));
        this.keyParams = Map.of(key, Arrays.asList(params));
    }

    public ValidationException(Map<String, List<Object>> keyParams) {
        super(keyParams.entrySet().stream().map((e) -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(",\n")));
        this.keyParams = keyParams;
    }

}
