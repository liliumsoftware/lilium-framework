package ir.baho.framework.config;

import ir.baho.framework.converter.BigDecimalConverter;
import ir.baho.framework.converter.BigIntegerConverter;
import ir.baho.framework.converter.BooleanConverter;
import ir.baho.framework.converter.CharacterConverter;
import ir.baho.framework.converter.DoubleConverter;
import ir.baho.framework.converter.IntegerConverter;
import ir.baho.framework.converter.LongConverter;
import ir.baho.framework.converter.StringConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.math.BigInteger;

@AutoConfiguration
public class ConverterConfig {

    @Bean
    @ConditionalOnMissingBean(value = String.class, parameterizedContainer = StringConverter.class)
    public StringConverter<String> stringConverter() {
        return StringConverter.stringConverter();
    }

    @Bean
    @ConditionalOnMissingBean(value = BigDecimal.class, parameterizedContainer = StringConverter.class)
    public StringConverter<BigDecimal> bigDecimalStringConverter() {
        return new BigDecimalConverter();
    }

    @Bean
    @ConditionalOnMissingBean(value = BigInteger.class, parameterizedContainer = StringConverter.class)
    public StringConverter<BigInteger> bigIntegerStringConverter() {
        return new BigIntegerConverter();
    }

    @Bean
    @ConditionalOnMissingBean(value = Boolean.class, parameterizedContainer = StringConverter.class)
    public StringConverter<Boolean> booleanStringConverter() {
        return new BooleanConverter();
    }

    @Bean
    @ConditionalOnMissingBean(value = Character.class, parameterizedContainer = StringConverter.class)
    public StringConverter<Character> characterStringConverter() {
        return new CharacterConverter();
    }

    @Bean
    @ConditionalOnMissingBean(value = Double.class, parameterizedContainer = StringConverter.class)
    public StringConverter<Double> doubleStringConverter() {
        return new DoubleConverter();
    }

    @Bean
    @ConditionalOnMissingBean(value = Integer.class, parameterizedContainer = StringConverter.class)
    public StringConverter<Integer> integerStringConverter() {
        return new IntegerConverter();
    }

    @Bean
    @ConditionalOnMissingBean(value = Long.class, parameterizedContainer = StringConverter.class)
    public StringConverter<Long> longStringConverter() {
        return new LongConverter();
    }

}
