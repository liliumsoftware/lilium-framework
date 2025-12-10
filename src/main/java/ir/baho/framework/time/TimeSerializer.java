package ir.baho.framework.time;

import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import org.springframework.core.convert.converter.Converter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.LocalTime;

public class TimeSerializer extends StdSerializer<LocalTime> implements Converter<LocalTime, String> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    public TimeSerializer(DateTimes dateTimes, CurrentUser currentUser) {
        super(LocalTime.class);
        this.dateTimes = dateTimes;
        this.currentUser = currentUser;
    }

    @Override
    public String convert(LocalTime localTime) {
        return dateTimes.format(localTime, currentUser.timeFormat());
    }

    @Override
    public void serialize(LocalTime value, JsonGenerator gen, SerializationContext ctxt) {
        gen.writeString(convert(value));
    }

}
