package ir.baho.framework.metadata.report;

import ir.baho.framework.validation.Color;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextFormat {

    @NotNull
    private Font font = Font.Arial;

    @Min(8)
    @Max(72)
    private int fontSize = 12;

    @NotBlank
    @Color
    private String fontColor = "#000000";

    private boolean bold = false;

    private boolean italic = false;

    private boolean underline = false;

    private boolean strikeThrough = false;

}
