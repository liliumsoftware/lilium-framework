package ir.baho.framework.converter;

import java.math.BigInteger;

public class BigIntegerConverter extends StringConverter<BigInteger> {

    public BigIntegerConverter() {
        super(BigIntegerConverter.class.getSimpleName());
    }

    @Override
    public BigInteger convert(String source) {
        return new BigInteger(source);
    }

}
