package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.Longitude;
import ir.baho.framework.validation.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LongitudeCheck implements ConstraintValidator<Longitude, Number> {

    @Override
    public void initialize(Longitude username) {
    }

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        return value == null || Validator.isLongitude(value);
    }

}
