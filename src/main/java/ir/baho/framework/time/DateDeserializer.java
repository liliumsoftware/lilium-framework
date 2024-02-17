package ir.baho.framework.time;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.LocalDate;

@RequiredArgsConstructor
public class DateDeserializer extends JsonDeserializer<LocalDate> implements Converter<String, LocalDate> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    @SneakyThrows
    @Override
    public LocalDate convert(String date) {
        String format = currentUser.dateFormat();
        return dateTimes.parseDate(date, format);
    }

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return convert(jsonParser.getValueAsString());
    }

}
