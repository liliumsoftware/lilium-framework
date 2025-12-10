package ir.baho.framework.converter;

import com.ibm.icu.text.ArabicShaping;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.web.bind.annotation.RequestMapping;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class PersianStringConverter extends StdDeserializer<String> implements Converter<String, String> {

    private static final ArabicShaping SHAPING = new ArabicShaping(ArabicShaping.DIGITS_AN2EN + ArabicShaping.DIGIT_TYPE_AN_EXTENDED);

    public PersianStringConverter() {
        super(String.class);
    }

    @SneakyThrows
    public static String fix(String source) {
        return SHAPING.shape(source.replaceAll("ك", "ک")
                .replaceAll("ي", "ی")
                .replaceAll("ى", "ی"));
    }

    @Override
    @RequestMapping
    public String convert(String source) {
        return fix(source);
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) {
        return fix(p.getValueAsString());
    }

}
