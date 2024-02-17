package ir.baho.framework.service.impl;

import ir.baho.framework.enumeration.EnumType;
import ir.baho.framework.metadata.UserOptions;
import ir.baho.framework.service.CurrentUser;
import ir.baho.framework.time.CalendarType;
import ir.baho.framework.time.DurationType;
import lombok.RequiredArgsConstructor;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class OptionsCurrentUser implements CurrentUser {

    private final UserOptions options;

    @Override
    public Locale locale() {
        return options.getLocale();
    }

    @Override
    public ZoneId zoneId() {
        return options.getZoneId();
    }

    @Override
    public String username() {
        return options.getUsername();
    }

    @Override
    public CalendarType calendarType() {
        return options.getCalendarType();
    }

    @Override
    public String dateFormat() {
        return options.getDateFormat();
    }

    @Override
    public String dateTimeFormat() {
        return options.getDateTimeFormat();
    }

    @Override
    public String timeFormat() {
        return options.getTimeFormat();
    }

    @Override
    public DurationType durationType() {
        return options.getDurationType();
    }

    @Override
    public EnumType enumType() {
        return options.getEnumType();
    }

    @Override
    public List<String> roles() {
        return options.getRoles();
    }

}
