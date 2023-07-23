package ir.baho.framework.config;

import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.metadata.UserOptions;
import ir.baho.framework.service.CurrentUser;
import ir.baho.framework.service.impl.OptionsCurrentUser;
import ir.baho.framework.time.DateConverter;
import ir.baho.framework.time.DateTimeConverter;
import ir.baho.framework.time.DurationConverter;
import ir.baho.framework.time.TimeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@AutoConfiguration
@RequiredArgsConstructor
public class DateTimeConfig {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;

    public static List<StringConverter<?>> getUserOptionsConverters(UserOptions options) {
        CurrentUser user = new OptionsCurrentUser(options);
        DateTimes dateTimes = new DateTimes(user);
        return List.of(
                new DateConverter(dateTimes, user),
                new DateTimeConverter(dateTimes, user),
                new TimeConverter(dateTimes, user),
                new DurationConverter(dateTimes, user)
        );
    }

    @Bean
    @ConditionalOnMissingBean(value = LocalDate.class, parameterizedContainer = StringConverter.class)
    public StringConverter<LocalDate> dateStringConverter() {
        return new DateConverter(dateTimes, currentUser);
    }

    @Bean
    @ConditionalOnMissingBean(value = LocalDateTime.class, parameterizedContainer = StringConverter.class)
    public StringConverter<LocalDateTime> dateTimeStringConverter() {
        return new DateTimeConverter(dateTimes, currentUser);
    }

    @Bean
    @ConditionalOnMissingBean(value = LocalTime.class, parameterizedContainer = StringConverter.class)
    public StringConverter<LocalTime> timeStringConverter() {
        return new TimeConverter(dateTimes, currentUser);
    }

    @Bean
    @ConditionalOnMissingBean(value = Duration.class, parameterizedContainer = StringConverter.class)
    public StringConverter<Duration> durationStringConverter() {
        return new DurationConverter(dateTimes, currentUser);
    }

}
