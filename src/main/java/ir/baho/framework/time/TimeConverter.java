package ir.baho.framework.time;

import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.i18n.Strings;
import ir.baho.framework.service.CurrentUser;

import java.time.LocalTime;
import java.util.Locale;

public class TimeConverter extends StringConverter<LocalTime> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    public TimeConverter(DateTimes dateTimes, CurrentUser currentUser) {
        super(TimeConverter.class.getSimpleName());
        this.dateTimes = dateTimes;
        this.currentUser = currentUser;
    }

    @Override
    public LocalTime convert(String source) {
        return dateTimes.parseTime(source, currentUser.timeFormat());
    }

    @Override
    public String print(LocalTime value, Locale locale) {
        return Strings.getText(dateTimes.format(value, currentUser.timeFormat()), locale);
    }

}
