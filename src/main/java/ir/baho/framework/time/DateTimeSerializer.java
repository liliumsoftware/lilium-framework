package ir.baho.framework.time;

import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import org.springframework.core.convert.converter.Converter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.LocalDateTime;

public class DateTimeSerializer extends StdSerializer<LocalDateTime> implements Converter<LocalDateTime, String> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    public DateTimeSerializer(DateTimes dateTimes, CurrentUser currentUser) {
        super(LocalDateTime.class);
        this.dateTimes = dateTimes;
        this.currentUser = currentUser;
    }

    @Override
    public String convert(LocalDateTime localDateTime) {
        return dateTimes.format(localDateTime, currentUser.dateTimeFormat());
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext provider) {
        gen.writeString(convert(value));
    }

}
