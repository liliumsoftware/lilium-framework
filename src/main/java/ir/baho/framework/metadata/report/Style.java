package ir.baho.framework.metadata.report;

import ir.baho.framework.validation.Color;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.LineSpacing;
import net.sf.dynamicreports.report.constant.Rotation;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;

@Getter
@Setter
public class Style {

    @NotNull
    private TextFormat textFormat = new TextFormat();

    @NotBlank
    @Color
    private String bgColor = "#ffffff";

    @NotNull
    private HorizontalTextAlignment horizontalAlign = HorizontalTextAlignment.CENTER;

    @NotNull
    private VerticalTextAlignment verticalAlign = VerticalTextAlignment.MIDDLE;

    @Valid
    private Border borderRight;

    @Valid
    private Border borderLeft;

    @Valid
    private Border borderTop;

    @Valid
    private Border borderBottom;

    @Min(0)
    @Max(100)
    private int paddingRight = 2;

    @Min(0)
    @Max(100)
    private int paddingLeft = 2;

    @Min(0)
    @Max(100)
    private int paddingTop = 1;

    @Min(0)
    @Max(100)
    private int paddingBottom = 1;

    @Min(0)
    @Max(100)
    private Integer indentRight;

    @Min(0)
    @Max(100)
    private Integer indentLeft;

    @NotNull
    private Rotation rotation = Rotation.NONE;

    @NotNull
    private LineSpacing lineSpacing = LineSpacing.SINGLE;

}
