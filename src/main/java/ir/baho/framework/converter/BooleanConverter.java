package ir.baho.framework.converter;

import java.util.List;

public class BooleanConverter extends StringConverter<Boolean> {

    public BooleanConverter() {
        super(BooleanConverter.class.getSimpleName());
    }

    @Override
    public Boolean convert(String source) {
        return Boolean.valueOf(source);
    }

    @Override
    protected List<Class<?>> supportedTypes() {
        return List.of(boolean.class, Boolean.class);
    }

}
