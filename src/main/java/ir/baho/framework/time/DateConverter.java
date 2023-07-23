package ir.baho.framework.time;

import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.i18n.Strings;
import ir.baho.framework.service.CurrentUser;
import lombok.SneakyThrows;

import java.time.LocalDate;
import java.util.Locale;

public class DateConverter extends StringConverter<LocalDate> {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    public DateConverter(DateTimes dateTimes, CurrentUser currentUser) {
        super(DateConverter.class.getSimpleName());
        this.dateTimes = dateTimes;
        this.currentUser = currentUser;
    }

    @SneakyThrows
    public LocalDate convert(String source) {
        String format = currentUser.getDateFormat();
        return dateTimes.parseDate(source, format);
    }

    @Override
    public String print(LocalDate value, Locale locale) {
        return Strings.getText(dateTimes.format(value, currentUser.getDateFormat()), locale);
    }

}
