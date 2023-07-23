package ir.baho.framework.time;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

@WritingConverter
public class DateWritingConverter implements Converter<LocalDate, Date> {

    @Override
    public Date convert(LocalDate source) {
        return Date.from(source.atStartOfDay().toInstant(ZoneOffset.UTC));
    }

}
