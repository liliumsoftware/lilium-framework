package ir.baho.framework.converter;

import lombok.SneakyThrows;

import java.text.NumberFormat;
import java.util.List;

public class LongConverter extends StringConverter<Long> {

    public LongConverter() {
        super(LongConverter.class.getSimpleName());
    }

    @SneakyThrows
    @Override
    public Long convert(String source) {
        return NumberFormat.getInstance().parse(source).longValue();
    }

    @Override
    protected List<Class<?>> supportedTypes() {
        return List.of(long.class, Long.class);
    }

}
