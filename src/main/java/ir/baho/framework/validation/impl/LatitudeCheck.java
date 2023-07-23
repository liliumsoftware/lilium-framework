package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.Latitude;
import ir.baho.framework.validation.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LatitudeCheck implements ConstraintValidator<Latitude, Number> {

    @Override
    public void initialize(Latitude username) {
    }

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        return value == null || Validator.isLatitude(value);
    }

}
