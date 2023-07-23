package ir.baho.framework.converter;

import lombok.SneakyThrows;

import java.text.NumberFormat;
import java.util.List;

public class IntegerConverter extends StringConverter<Integer> {

    public IntegerConverter() {
        super(IntegerConverter.class.getSimpleName());
    }

    @SneakyThrows
    @Override
    public Integer convert(String source) {
        return NumberFormat.getInstance().parse(source).intValue();
    }

    @Override
    protected List<Class<?>> supportedTypes() {
        return List.of(int.class, Integer.class, short.class, Short.class, byte.class, Byte.class);
    }

}
