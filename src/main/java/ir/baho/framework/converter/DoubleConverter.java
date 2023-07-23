package ir.baho.framework.converter;

import lombok.SneakyThrows;

import java.text.NumberFormat;
import java.util.List;

public class DoubleConverter extends StringConverter<Double> {

    public DoubleConverter() {
        super(DoubleConverter.class.getSimpleName());
    }

    @SneakyThrows
    @Override
    public Double convert(String source) {
        return NumberFormat.getInstance().parse(source).doubleValue();
    }

    @Override
    protected List<Class<?>> supportedTypes() {
        return List.of(double.class, Double.class, float.class, Float.class);
    }

}
