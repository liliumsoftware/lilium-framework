package ir.baho.framework.time;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;

@WritingConverter
public class TimeWritingConverter implements Converter<LocalTime, Date> {

    @Override
    public Date convert(LocalTime source) {
        return Date.from(LocalDate.ofEpochDay(0).atTime(source).toInstant(ZoneOffset.UTC));
    }

}
