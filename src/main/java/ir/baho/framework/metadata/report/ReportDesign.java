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

    @Min(74)
    @Max(3368)
    private int width = 595;

    @Min(74)
    @Max(3368)
    private int height = 842;

    @Min(0)
    @Max(100)
    private int marginTop = 20;

    @Min(0)
    @Max(100)
    private int marginLeft = 20;

    @Min(0)
    @Max(100)
    private int marginRight = 20;

    @Min(0)
    @Max(100)
    private int marginBottom = 20;

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
