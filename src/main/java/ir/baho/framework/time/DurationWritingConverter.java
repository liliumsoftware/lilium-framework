package ir.baho.framework.time;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.Duration;

@WritingConverter
public class DurationWritingConverter implements Converter<Duration, Long> {

    @Override
    public Long convert(Duration source) {
        return source.toNanos();
    }

}
