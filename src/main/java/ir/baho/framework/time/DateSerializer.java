package ir.baho.framework.time;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.LocalDate;

@RequiredArgsConstructor
public class DateSerializer extends JsonSerializer<LocalDate> implements Converter<LocalDate, String> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    @Override
    public String convert(LocalDate date) {
        return dateTimes.format(date, currentUser.getDateFormat());
    }

    @Override
    public void serialize(LocalDate localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(convert(localDateTime));
    }

}
