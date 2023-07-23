package ir.baho.framework.time;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.Duration;

@ReadingConverter
public class DurationReadingConverter implements Converter<Long, Duration> {

    @Override
    public Duration convert(Long source) {
        return Duration.ofNanos(source);
    }

}
