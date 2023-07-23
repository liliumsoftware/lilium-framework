package ir.baho.framework.time;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;

@ReadingConverter
public class TimeReadingConverter implements Converter<Date, LocalTime> {

    @Override
    public LocalTime convert(Date source) {
        return LocalDateTime.ofInstant(source.toInstant(), ZoneOffset.UTC).toLocalTime();
    }

}
