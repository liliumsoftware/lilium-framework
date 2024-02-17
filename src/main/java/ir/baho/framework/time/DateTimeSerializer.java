package ir.baho.framework.time;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class DateTimeSerializer extends JsonSerializer<LocalDateTime> implements Converter<LocalDateTime, String> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    @Override
    public String convert(LocalDateTime localDateTime) {
        return dateTimes.format(localDateTime, currentUser.dateTimeFormat());
    }

    @Override
    public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(convert(localDateTime));
    }

}
