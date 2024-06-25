package ir.baho.framework.metadata.report;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class ReportDesign {

    private String title;

    private Style titleStyle;

    private String subtitle;

    private Style subtitleStyle;

    private String header;

    private Style headerStyle;

    private String footer;

    private Style footerStyle;

    private String summary;

    private Style summaryStyle;

    private Style dateTimeStyle;

    @NotNull
    private Position dateTimePosition = Position.TOP;

    private Style usernameStyle;

    @NotNull
    private Position usernamePosition = Position.TOP;

    private boolean rowNumber = true;

    private Integer rowNumberWidth;

    private Style rowNumberStyle;

    private Style rowNumberHeaderStyle;

    private boolean pageNumber = true;

    private Style pageNumberStyle;

    private boolean highlightAlternateRows = true;

    private Boolean paginate;

    private Boolean digitsUnicode;

    @Min(74)
    @Max(4008)
    private Integer width;

    @Min(74)
    @Max(4008)
    private Integer height;

    @Min(0)
    @Max(100)
    private Integer marginTop;

    @Min(0)
    @Max(100)
    private Integer marginLeft;

    @Min(0)
    @Max(100)
    private Integer marginRight;

    @Min(0)
    @Max(100)
    private Integer marginBottom;

    @NotNull
    private ReportType type = ReportType.TABLE_ONLY;

    private List<@Valid ReportColumn> columns = new ArrayList<>();

    private List<@Valid Chart> charts = new ArrayList<>();

    public void setColumns(ReportColumn... columns) {
        this.columns = Stream.of(columns).collect(Collectors.toList());
    }

    public void setCharts(Chart... charts) {
        this.charts = Stream.of(charts).collect(Collectors.toList());
    }

}
