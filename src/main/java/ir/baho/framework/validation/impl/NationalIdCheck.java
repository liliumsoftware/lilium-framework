package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.NationalId;
import ir.baho.framework.validation.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NationalIdCheck implements ConstraintValidator<NationalId, String> {

    @Override
    public void initialize(NationalId id) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || Validator.isNationalId(value);
    }

}
