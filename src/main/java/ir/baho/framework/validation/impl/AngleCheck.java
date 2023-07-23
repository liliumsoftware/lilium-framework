package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.Angle;
import ir.baho.framework.validation.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AngleCheck implements ConstraintValidator<Angle, Number> {

    @Override
    public void initialize(Angle username) {
    }

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        return value == null || Validator.isAngle(value);
    }

}
