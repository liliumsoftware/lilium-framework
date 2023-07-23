package ir.baho.framework.time;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.LocalTime;

@RequiredArgsConstructor
public class TimeDeserializer extends JsonDeserializer<LocalTime> implements Converter<String, LocalTime> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    @Override
    public LocalTime convert(String source) {
        return dateTimes.parseTime(source, currentUser.getTimeFormat());
    }

    @Override
    public LocalTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return convert(jsonParser.getValueAsString());
    }

}
