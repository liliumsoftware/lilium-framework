package ir.baho.framework.metadata;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Sort implements Serializable {

    @NotBlank
    private String field;
    private boolean asc;

    public Sort(String field) {
        this.field = field;
        this.asc = true;
    }

}
