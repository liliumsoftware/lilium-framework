package ir.baho.framework.metadata;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Search implements Serializable {

    @NotBlank
    private String field;

    @NotNull
    private Constraint constraint;

    private Comparable<?> value;
    private Comparable<?> another;
    private List<Comparable<?>> values;

    public Search(String field, Constraint constraint) {
        this.field = field;
        this.constraint = constraint;
    }

    public Search(String field, Constraint constraint, Comparable<?> value) {
        this.field = field;
        this.constraint = constraint;
        this.value = value;
    }

    public Search(String field, Constraint constraint, Comparable<?> value, Comparable<?> another) {
        this.field = field;
        this.constraint = constraint;
        this.value = value;
        this.another = another;
    }

    public Search(String field, Constraint constraint, List<? extends Comparable<?>> values) {
        this.field = field;
        this.constraint = constraint;
        this.values = new ArrayList<>(values);
    }

}
