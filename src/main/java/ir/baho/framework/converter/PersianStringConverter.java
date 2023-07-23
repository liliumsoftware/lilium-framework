package ir.baho.framework.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.ibm.icu.text.ArabicShaping;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

public class PersianStringConverter extends JsonDeserializer<String> implements Converter<String, String> {

    private static final ArabicShaping SHAPING = new ArabicShaping(ArabicShaping.DIGITS_AN2EN + ArabicShaping.DIGIT_TYPE_AN_EXTENDED);

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
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return fix(jsonParser.getValueAsString());
    }

}
