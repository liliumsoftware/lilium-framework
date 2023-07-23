package ir.baho.framework.metadata.report;

import ir.baho.framework.enumeration.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum ReportType implements EnumValue<Integer> {

    TABLE_ONLY(0),
    CHART_ONLY(1),
    TABLE_CHART(2),
    CHART_TABLE(3);

    private final Integer value;

    public static ReportType of(int value) {
        return Stream.of(values()).filter(type -> type.value.equals(value)).findAny()
                .orElseThrow(() -> new IllegalArgumentException("No ReportType found with value: " + value));
    }

}
