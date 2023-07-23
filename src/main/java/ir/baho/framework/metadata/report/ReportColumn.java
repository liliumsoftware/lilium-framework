package ir.baho.framework.metadata.report;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ReportColumn {

    @NotBlank
    private String name;

    private String header;

    private Style headerStyle;

    private Style style;

    private Integer width;

    private String format;

    private boolean grouped;

    private Style groupStyle;

    private Style groupLabelStyle;

    private List<Subtotal> subtotals;

    public ReportColumn(String name) {
        this.name = name;
    }

}
