package ir.baho.framework.time;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.Duration;

@RequiredArgsConstructor
public class DurationSerializer extends JsonSerializer<Duration> implements Converter<Duration, String> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    @Override
    public String convert(Duration duration) {
        return dateTimes.format(duration, currentUser.durationType());
    }

    @Override
    public void serialize(Duration duration, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(convert(duration));
    }

}
