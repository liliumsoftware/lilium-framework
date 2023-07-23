package ir.baho.framework.metadata.report;

import ir.baho.framework.validation.Color;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import net.sf.dynamicreports.report.constant.LineStyle;

@Getter
@Setter
public class Border {

    @DecimalMin("0.1")
    @DecimalMax("10.0")
    private float width = 1.0f;

    @NotBlank
    @Color
    private String color = "#000000";

    @NotNull
    private LineStyle type = LineStyle.SOLID;

}
