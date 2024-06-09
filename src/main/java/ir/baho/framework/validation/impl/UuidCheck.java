package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.Uuid;
import ir.baho.framework.validation.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UuidCheck implements ConstraintValidator<Uuid, String> {

    @Override
    public void initialize(Uuid uuid) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || Validator.isUuid(value);
    }

}
