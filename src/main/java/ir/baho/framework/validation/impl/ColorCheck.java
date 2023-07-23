package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.Color;
import ir.baho.framework.validation.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ColorCheck implements ConstraintValidator<Color, String> {

    @Override
    public void initialize(Color color) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || Validator.isHexadecimal(value);
    }

}
