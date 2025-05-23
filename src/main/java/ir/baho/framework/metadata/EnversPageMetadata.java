package ir.baho.framework.metadata;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EnversPageMetadata extends EnversMetadata implements PageSortMetadata {

    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(1000)
    private int size = 10;

    public EnversPageMetadata(@Valid Sort... sort) {
        super(sort);
    }

    public EnversPageMetadata(@Valid Search... search) {
        super(search);
    }

    public EnversPageMetadata(int page, int size, @Valid Sort... sort) {
        super(sort);
        this.page = page;
        this.size = size;
    }

    public EnversPageMetadata(int page, int size, @Valid Search... search) {
        super(search);
        this.page = page;
        this.size = size;
    }

}
