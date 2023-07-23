package ir.baho.framework.metadata;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProjectionPageMetadata extends ProjectionMetadata implements PageSortMetadata {

    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(1000)
    private int size = 10;

    public ProjectionPageMetadata(@NotNull @Size(min = 1) String... field) {
        super(field);
    }

    public ProjectionPageMetadata(@Valid Sort... sort) {
        super(sort);
    }

    public ProjectionPageMetadata(@Valid Search... search) {
        super(search);
    }

    public ProjectionPageMetadata(int page, int size, @NotNull @Size(min = 1) String... field) {
        super(field);
        this.page = page;
        this.size = size;
    }

    public ProjectionPageMetadata(int page, int size, @Valid Sort... sort) {
        super(sort);
        this.page = page;
        this.size = size;
    }

    public ProjectionPageMetadata(int page, int size, @Valid Search... search) {
        super(search);
        this.page = page;
        this.size = size;
    }

}
