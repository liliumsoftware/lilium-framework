package ir.baho.framework.time;

import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import org.springframework.core.convert.converter.Converter;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.LocalTime;

public class TimeDeserializer extends StdDeserializer<LocalTime> implements Converter<String, LocalTime> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    public TimeDeserializer(DateTimes dateTimes, CurrentUser currentUser) {
        super(LocalTime.class);
        this.dateTimes = dateTimes;
        this.currentUser = currentUser;
    }

    @Override
    public LocalTime convert(String source) {
        return dateTimes.parseTime(source, currentUser.timeFormat());
    }

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) {
        return convert(p.getValueAsString());
    }

}
