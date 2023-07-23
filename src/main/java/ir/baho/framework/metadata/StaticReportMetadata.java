package ir.baho.framework.metadata;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class StaticReportMetadata implements Serializable {

    @NotNull
    Map<String, Object> params = new HashMap<>();

    @NotBlank
    private String name;

    private String query;

    private Locale locale;

    public StaticReportMetadata(String name) {
        this.name = name;
    }

    public StaticReportMetadata param(String key, Object value) {
        this.params.put(key, value);
        return this;
    }

}
