package ir.baho.framework.time;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.LocalTime;

@RequiredArgsConstructor
public class TimeSerializer extends JsonSerializer<LocalTime> implements Converter<LocalTime, String> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    @Override
    public String convert(LocalTime localTime) {
        return dateTimes.format(localTime, currentUser.getTimeFormat());
    }

    @Override
    public void serialize(LocalTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(convert(localDateTime));
    }

}
