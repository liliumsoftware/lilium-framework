package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.ExactLength;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ExactLengthCheck implements ConstraintValidator<ExactLength, Object> {

    private boolean isNumber;
    private int length;

    @Override
    public void initialize(ExactLength exactLength) {
        isNumber = exactLength.isNumber();
        length = exactLength.value();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null || value.toString().isBlank()) {
            return true;
        }
        if (isNumber) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{ir.baho.framework.validation.ExactNumberLength.message}").addConstraintViolation();
        }
        return value.toString().length() == length;
    }

}
