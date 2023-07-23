package ir.baho.framework.metadata;

import ir.baho.framework.metadata.report.DateTimeFormatters;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ReportMetadata extends ProjectionMetadata {

    @NotBlank
    private String name;

    private UserOptions options;

    public ReportMetadata(String name) {
        this.name = name;
    }

    public ReportMetadata(String name, @NotNull @Size(min = 1) String... field) {
        super(field);
        this.name = name;
    }

    public ReportMetadata(String name, @Valid Sort... sort) {
        super(sort);
        this.name = name;
    }

    public ReportMetadata(String name, @Valid Search... search) {
        super(search);
        this.name = name;
    }

    public DateTimeFormatters getDateTimeFormatters() {
        return new DateTimeFormatters(options.getLocale(),
                options.getZoneId(), options.getCalendarType(),
                options.getDateFormat(), options.getDateTimeFormat(),
                options.getTimeFormat(), options.getDurationType());
    }

}
