package ir.baho.framework.metadata;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProjectionMetadata extends Metadata {

    @NotNull
    @Size(min = 1)
    private String[] field;

    public ProjectionMetadata(@NotNull @Size(min = 1) String... field) {
        this.field = field;
    }

    public ProjectionMetadata(@Valid Sort... sort) {
        super(sort);
    }

    public ProjectionMetadata(@Valid Search... search) {
        super(search);
    }

    public String[] getField() {
        return field;
    }

    public void setField(String... field) {
        this.field = field;
    }

}
