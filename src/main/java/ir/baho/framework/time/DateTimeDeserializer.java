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
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class DateTimeDeserializer extends JsonDeserializer<LocalDateTime> implements Converter<String, LocalDateTime> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    @SneakyThrows
    @Override
    public LocalDateTime convert(String localDateTime) {
        return dateTimes.parseDateTime(localDateTime, currentUser.getDateTimeFormat());
    }

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return convert(jsonParser.getValueAsString());
    }

}
