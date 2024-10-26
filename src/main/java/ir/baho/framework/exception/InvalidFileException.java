package ir.baho.framework.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
public class InvalidFileException extends RuntimeException {

    private Map<String, List<Object>> keyParams;

    public InvalidFileException(String key, Object... params) {
        super(params.length == 0 ? key : key + ": " + Arrays.asList(params));
        this.keyParams = Map.of(key, Arrays.asList(params));
    }

    public InvalidFileException(Map<String, List<Object>> keyParams) {
        super(keyParams.entrySet().stream().map((e) -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(",\n")));
        this.keyParams = keyParams;
    }

}
