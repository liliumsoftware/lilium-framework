package ir.baho.framework.time;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

@ReadingConverter
public class DateReadingConverter implements Converter<Date, LocalDate> {

    @Override
    public LocalDate convert(Date source) {
        return LocalDate.ofInstant(source.toInstant(), ZoneOffset.UTC);
    }

}
