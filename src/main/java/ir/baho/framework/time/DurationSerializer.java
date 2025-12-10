package ir.baho.framework.time;

import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import org.springframework.core.convert.converter.Converter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.Duration;

public class DurationSerializer extends StdSerializer<Duration> implements Converter<Duration, String> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    public DurationSerializer(DateTimes dateTimes, CurrentUser currentUser) {
        super(Duration.class);
        this.dateTimes = dateTimes;
        this.currentUser = currentUser;
    }

    @Override
    public String convert(Duration duration) {
        return dateTimes.format(duration, currentUser.durationType());
    }

    @Override
    public void serialize(Duration value, JsonGenerator gen, SerializationContext provider) {
        gen.writeString(convert(value));
    }

}
