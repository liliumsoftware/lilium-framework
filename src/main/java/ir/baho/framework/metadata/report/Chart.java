package ir.baho.framework.metadata.report;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({@JsonSubTypes.Type(AreaChart.class), @JsonSubTypes.Type(BarChart.class), @JsonSubTypes.Type(LineChart.class), @JsonSubTypes.Type(PieChart.class)})
public abstract sealed class Chart permits AreaChart, BarChart, LineChart, PieChart {

    private String title;

    @NotNull
    private TextFormat titleFormat = new TextFormat();

    @NotBlank
    private String category;

    private boolean vertical = true;

}
