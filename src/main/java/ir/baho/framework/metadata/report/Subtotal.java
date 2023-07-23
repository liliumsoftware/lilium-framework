package ir.baho.framework.metadata.report;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Subtotal {

    @NotNull
    private Function function = Function.SUM;

    @NotNull
    private Position position = Position.SUMMARY;

    private Style style;

    private String label;

    private Style labelStyle;

    public enum Function {
        COUNT, MIN, MAX, AVG, SUM
    }

    public enum Position {
        TITLE, COLUMN_HEADER, COLUMN_FOOTER, PAGE_HEADER, PAGE_FOOTER, SUMMARY
    }

}
