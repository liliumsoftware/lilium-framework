package ir.baho.framework.time;

import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.i18n.Strings;
import ir.baho.framework.service.CurrentUser;

import java.time.Duration;
import java.util.Locale;

public class DurationConverter extends StringConverter<Duration> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    public DurationConverter(DateTimes dateTimes, CurrentUser currentUser) {
        super(DurationConverter.class.getSimpleName());
        this.dateTimes = dateTimes;
        this.currentUser = currentUser;
    }

    @Override
    public Duration convert(String source) {
        return dateTimes.parseDuration(source, currentUser.durationType());
    }

    @Override
    public String print(Duration value, Locale locale) {
        return Strings.getText(dateTimes.format(value, currentUser.durationType()), locale);
    }

}
