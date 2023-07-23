package ir.baho.framework.time;

import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.i18n.Strings;
import ir.baho.framework.service.CurrentUser;
import lombok.SneakyThrows;

import java.time.LocalDateTime;
import java.util.Locale;

public class DateTimeConverter extends StringConverter<LocalDateTime> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    public DateTimeConverter(DateTimes dateTimes, CurrentUser currentUser) {
        super(DateTimeConverter.class.getSimpleName());
        this.dateTimes = dateTimes;
        this.currentUser = currentUser;
    }

    @SneakyThrows
    public LocalDateTime convert(String source) {
        return dateTimes.parseDateTime(source, currentUser.getDateTimeFormat());
    }

    @Override
    public String print(LocalDateTime value, Locale locale) {
        return Strings.getText(dateTimes.format(value, currentUser.getDateTimeFormat()), locale);
    }

}
