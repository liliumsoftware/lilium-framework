package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.Imei;
import ir.baho.framework.validation.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ImeiCheck implements ConstraintValidator<Imei, String> {

    @Override
    public void initialize(Imei id) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || Validator.isImei(value);
    }

}
