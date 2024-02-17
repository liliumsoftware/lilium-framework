package ir.baho.framework.config;

import feign.RequestInterceptor;
import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.service.CurrentUser;
import ir.baho.framework.time.DateConverter;
import ir.baho.framework.time.DateTimeConverter;
import ir.baho.framework.time.DurationConverter;
import ir.baho.framework.time.TimeConverter;
import ir.baho.framework.web.Headers;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpHeaders;

@ConditionalOnClass(name = "org.springframework.cloud.openfeign.FeignFormatterRegistrar")
@AutoConfiguration
@RequiredArgsConstructor
public class FeignConfig implements FeignFormatterRegistrar {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addFormatter(new DateConverter(dateTimes, currentUser));
        registry.addFormatter(new DateTimeConverter(dateTimes, currentUser));
        registry.addFormatter(new TimeConverter(dateTimes, currentUser));
        registry.addFormatter(new DurationConverter(dateTimes, currentUser));
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header(HttpHeaders.ACCEPT_LANGUAGE, LocaleContextHolder.getLocale().toString());
            requestTemplate.header(Headers.TIME_ZONE, LocaleContextHolder.getTimeZone().getID());
            if (currentUser.calendarType() != null) {
                requestTemplate.header(Headers.CALENDAR_TYPE, currentUser.calendarType().name());
            }
            if (currentUser.dateFormat() != null) {
                requestTemplate.header(Headers.DATE_FORMAT, currentUser.dateFormat());
            }
            if (currentUser.dateTimeFormat() != null) {
                requestTemplate.header(Headers.DATETIME_FORMAT, currentUser.dateTimeFormat());
            }
            if (currentUser.timeFormat() != null) {
                requestTemplate.header(Headers.TIME_FORMAT, currentUser.timeFormat());
            }
            if (currentUser.durationType() != null) {
                requestTemplate.header(Headers.DURATION_TYPE, currentUser.durationType().name());
            }
            if (currentUser.enumType() != null) {
                requestTemplate.header(Headers.ENUM_TYPE, currentUser.enumType().name());
            }
        };
    }

}
