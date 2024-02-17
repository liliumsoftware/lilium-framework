package ir.baho.framework.time;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.Duration;

@RequiredArgsConstructor
public class DurationDeserializer extends JsonDeserializer<Duration> implements Converter<String, Duration> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    @Override
    public Duration convert(String source) {
        return dateTimes.parseDuration(source, currentUser.durationType());
    }

    @Override
    public Duration deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return convert(jsonParser.getValueAsString());
    }

}
