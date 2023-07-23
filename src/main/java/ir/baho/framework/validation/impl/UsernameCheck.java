package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.Username;
import ir.baho.framework.validation.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UsernameCheck implements ConstraintValidator<Username, String> {

    @Override
    public void initialize(Username username) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || Validator.isUsername(value);
    }

}
