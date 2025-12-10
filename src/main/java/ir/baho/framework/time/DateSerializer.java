package ir.baho.framework.time;

import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import org.springframework.core.convert.converter.Converter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.LocalDate;

public class DateSerializer extends StdSerializer<LocalDate> implements Converter<LocalDate, String> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    public DateSerializer(DateTimes dateTimes, CurrentUser currentUser) {
        super(LocalDate.class);
        this.dateTimes = dateTimes;
        this.currentUser = currentUser;
    }

    @Override
    public String convert(LocalDate date) {
        return dateTimes.format(date, currentUser.dateFormat());
    }

    @Override
    public void serialize(LocalDate value, JsonGenerator gen, SerializationContext provider) {
        gen.writeString(convert(value));
    }

}
