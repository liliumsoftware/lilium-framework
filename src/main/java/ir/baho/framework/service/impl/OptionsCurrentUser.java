package ir.baho.framework.service.impl;

import ir.baho.framework.enumeration.EnumType;
import ir.baho.framework.metadata.UserOptions;
import ir.baho.framework.service.CurrentUser;
import ir.baho.framework.time.CalendarType;
import ir.baho.framework.time.DurationType;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.TimeZone;

@RequiredArgsConstructor
public class OptionsCurrentUser implements CurrentUser {

    private final UserOptions options;

    @Override
    public Locale getLocale() {
        return options.getLocale();
    }

    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(options.getZoneId());
    }

    @Override
    public String getUsername() {
        return options.getUsername();
    }

    @Override
    public CalendarType getCalendarType() {
        return options.getCalendarType();
    }

    @Override
    public String getDateFormat() {
        return options.getDateFormat();
    }

    @Override
    public String getDateTimeFormat() {
        return options.getDateTimeFormat();
    }

    @Override
    public String getTimeFormat() {
        return options.getTimeFormat();
    }

    @Override
    public DurationType getDurationType() {
        return options.getDurationType();
    }

    @Override
    public EnumType getEnumType() {
        return options.getEnumType();
    }

}
