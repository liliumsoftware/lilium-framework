package ir.baho.framework.metadata.report;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class PieChart extends Chart {

    @NotBlank
    private String series;

    private boolean threeDimension = true;

}
