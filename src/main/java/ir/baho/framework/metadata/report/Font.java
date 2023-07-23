package ir.baho.framework.metadata.report;

import ir.baho.framework.enumeration.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.util.Base64;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum Font implements EnumValue<Integer> {

    Arial(0),
    Baho(1),
    Calibri(2),
    Courier(3),
    Lilium(4),
    Naskh(5),
    Tahoma(6),
    Times(7),
    Yekan(8);

    private final Integer value;

    public static Font of(int value) {
        return Stream.of(values()).filter(type -> type.value.equals(value)).findAny()
                .orElseThrow(() -> new IllegalArgumentException("No Font found with value: " + value));
    }

    @SneakyThrows
    public String getBase64() {
        try (InputStream font = Font.class.getResourceAsStream("/fonts/" + name() + ".ttf")) {
            return Base64.getEncoder().encodeToString(font.readAllBytes());
        }
    }

}
