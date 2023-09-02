package ir.baho.framework.service;

import ir.baho.framework.enumeration.EnumType;
import ir.baho.framework.metadata.UserOptions;
import ir.baho.framework.time.CalendarType;
import ir.baho.framework.time.DurationType;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;

public interface CurrentUser extends TimeZoneAwareLocaleContext {

    default String getId() {
        return getUsername();
    }

    String getUsername();

    CalendarType getCalendarType();

    String getDateFormat();

    String getDateTimeFormat();

    String getTimeFormat();

    DurationType getDurationType();

    EnumType getEnumType();

    default boolean isRtl() {
        String lang = getLocale().getLanguage();
        if (lang != null && !lang.isBlank()) {
            return lang.startsWith("fa") || lang.startsWith("ar");
        }
        return false;
    }

    default UserOptions getOptions() {
        return new UserOptions(getUsername(), LocaleContextHolder.getLocale(), LocaleContextHolder.getTimeZone().toZoneId(),
                getCalendarType(), getDateFormat(), getDateTimeFormat(), getTimeFormat(), getDurationType(), getEnumType(), isRtl());
    }

}
