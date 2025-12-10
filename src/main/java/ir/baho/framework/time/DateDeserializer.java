package ir.baho.framework.time;

import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import org.springframework.core.convert.converter.Converter;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.LocalDate;

public class DateDeserializer extends StdDeserializer<LocalDate> implements Converter<String, LocalDate> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    public DateDeserializer(DateTimes dateTimes, CurrentUser currentUser) {
        super(LocalDate.class);
        this.dateTimes = dateTimes;
        this.currentUser = currentUser;
    }

    @Override
    public LocalDate convert(String date) {
        String format = currentUser.dateFormat();
        return dateTimes.parseDate(date, format);
    }

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) {
        return convert(p.getValueAsString());
    }

}
