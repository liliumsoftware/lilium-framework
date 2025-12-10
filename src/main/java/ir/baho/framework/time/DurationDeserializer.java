package ir.baho.framework.time;

import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import org.springframework.core.convert.converter.Converter;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.Duration;

public class DurationDeserializer extends StdDeserializer<Duration> implements Converter<String, Duration> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    public DurationDeserializer(DateTimes dateTimes, CurrentUser currentUser) {
        super(Duration.class);
        this.dateTimes = dateTimes;
        this.currentUser = currentUser;
    }

    @Override
    public Duration convert(String source) {
        return dateTimes.parseDuration(source, currentUser.durationType());
    }

    @Override
    public Duration deserialize(JsonParser p, DeserializationContext ctxt) {
        return convert(p.getValueAsString());
    }

}
