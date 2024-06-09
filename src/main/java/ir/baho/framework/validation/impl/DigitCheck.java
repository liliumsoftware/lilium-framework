package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.Digit;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DigitCheck implements ConstraintValidator<Digit, Object> {

    private int minIntegerLen;
    private int minDecimalLen;
    private int maxIntegerLen;
    private int maxDecimalLen;
    private double min;
    private double max;

    @Override
    public void initialize(Digit digit) {
        maxIntegerLen = digit.maxIntegerLen();
        maxDecimalLen = digit.maxDecimalLen();
        minIntegerLen = digit.minIntegerLen();
        minDecimalLen = digit.minDecimalLen();
        min = digit.min();
        max = digit.max();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null || (value instanceof String && value.toString().isBlank())) {
            return true;
        }
        String str = String.valueOf(value);
        if (str.endsWith("\\.0")) {
            str = str.substring(0, str.length() - 2);
        }
        if (str.matches("([-]?[0-9]{" + minIntegerLen + "," + maxIntegerLen
                + "})|([-]?[0-9]{" + minIntegerLen + "," + maxIntegerLen
                + "}\\.[0-9]{" + minDecimalLen + "," + maxDecimalLen + "})")) {
            if (value instanceof Number) {
                double number = ((Number) value).doubleValue();
                return number >= min && number <= max;
            } else {
                return value instanceof String && Double.parseDouble(str) >= min && Double.parseDouble(str) <= max;
            }
        }
        return false;
    }

}
