package ir.baho.framework.converter;

import java.math.BigDecimal;

public class BigDecimalConverter extends StringConverter<BigDecimal> {

    public BigDecimalConverter() {
        super(BigDecimalConverter.class.getSimpleName());
    }

    @Override
    public BigDecimal convert(String source) {
        return new BigDecimal(source);
    }

}
