package ir.baho.framework.service;

import ir.baho.framework.enumeration.EnumType;
import ir.baho.framework.metadata.UserOptions;
import ir.baho.framework.time.CalendarType;
import ir.baho.framework.time.DurationType;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public interface CurrentUser extends TimeZoneAwareLocaleContext {

    default String id() {
        return username();
    }

    String username();

    String email();

    String firstName();

    String lastName();

    Locale locale();

    ZoneId zoneId();

    CalendarType calendarType();

    String dateFormat();

    String dateTimeFormat();

    String timeFormat();

    DurationType durationType();

    EnumType enumType();

    List<String> roles();

    List<String> groups();

    List<String> scopes();

    List<String> permissions();

    default <V> V getValue(String name) {
        return null;
    }

    @Override
    default Locale getLocale() {
        return locale();
    }

    @Override
    default TimeZone getTimeZone() {
        ZoneId zoneId = zoneId();
        return zoneId != null ? TimeZone.getTimeZone(zoneId) : null;
    }

    default boolean isRtl() {
        Locale locale = locale();
        if (locale != null) {
            String lang = locale.getLanguage();
            if (!lang.isBlank()) {
                return lang.startsWith("fa") || lang.startsWith("ar");
            }
        }
        return false;
    }

    default UserOptions getOptions() {
        return new UserOptions(username(), email(), firstName(), lastName(), LocaleContextHolder.getLocale(), LocaleContextHolder.getTimeZone().toZoneId(),
                calendarType(), dateFormat(), dateTimeFormat(), timeFormat(), durationType(), enumType(), roles(), groups(), scopes(), permissions(), isRtl());
    }

}
