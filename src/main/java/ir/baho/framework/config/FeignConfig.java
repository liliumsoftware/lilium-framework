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
            if (currentUser.getCalendarType() != null) {
                requestTemplate.header(Headers.CALENDAR_TYPE, currentUser.getCalendarType().name());
            }
            if (currentUser.getDateFormat() != null) {
                requestTemplate.header(Headers.DATE_FORMAT, currentUser.getDateFormat());
            }
            if (currentUser.getDateTimeFormat() != null) {
                requestTemplate.header(Headers.DATETIME_FORMAT, currentUser.getDateTimeFormat());
            }
            if (currentUser.getTimeFormat() != null) {
                requestTemplate.header(Headers.TIME_FORMAT, currentUser.getTimeFormat());
            }
            if (currentUser.getDurationType() != null) {
                requestTemplate.header(Headers.DURATION_TYPE, currentUser.getDurationType().name());
            }
            if (currentUser.getEnumType() != null) {
                requestTemplate.header(Headers.ENUM_TYPE, currentUser.getEnumType().name());
            }
        };
    }

}
