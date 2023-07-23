package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.Altitude;
import ir.baho.framework.validation.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AltitudeCheck implements ConstraintValidator<Altitude, Number> {

    @Override
    public void initialize(Altitude username) {
    }

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        return value == null || Validator.isAltitude(value);
    }

}
