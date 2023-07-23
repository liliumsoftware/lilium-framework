package ir.baho.framework.metadata.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public final class LineChart extends Chart {

    private String label;

    @NotNull
    private TextFormat labelFormat = new TextFormat();

    @NotEmpty
    private List<@NotBlank String> series;

    public void setSeries(String... series) {
        this.series = Stream.of(series).collect(Collectors.toList());
    }

}
