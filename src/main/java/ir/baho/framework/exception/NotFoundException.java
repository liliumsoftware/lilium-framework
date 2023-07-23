package ir.baho.framework.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
public class NotFoundException extends RuntimeException {

    private Map<String, List<Object>> keyParams;

    public NotFoundException(String key, Object... params) {
        super(key);
        this.keyParams = Map.of(key, Arrays.asList(params));
    }

    public NotFoundException(Map<String, List<Object>> keyParams) {
        super(keyParams.keySet().toString());
        this.keyParams = keyParams;
    }

}
