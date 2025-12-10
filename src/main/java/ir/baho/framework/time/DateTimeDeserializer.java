package ir.baho.framework.time;

import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import org.springframework.core.convert.converter.Converter;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.LocalDateTime;

public class DateTimeDeserializer extends StdDeserializer<LocalDateTime> implements Converter<String, LocalDateTime> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    public DateTimeDeserializer(DateTimes dateTimes, CurrentUser currentUser) {
        super(LocalDateTime.class);
        this.dateTimes = dateTimes;
        this.currentUser = currentUser;
    }

    @Override
    public LocalDateTime convert(String localDateTime) {
        return dateTimes.parseDateTime(localDateTime, currentUser.dateTimeFormat());
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) {
        return convert(p.getValueAsString());
    }

}
