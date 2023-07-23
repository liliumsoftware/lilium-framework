package ir.baho.framework.metadata;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
public class Sort implements Serializable {

    @NotBlank
    private final String field;
    private final boolean asc;

    public Sort(String field) {
        this.field = field;
        this.asc = true;
    }

}
