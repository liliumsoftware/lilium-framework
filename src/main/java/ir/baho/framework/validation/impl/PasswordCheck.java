package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.Password;
import ir.baho.framework.validation.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordCheck implements ConstraintValidator<Password, String> {

    @Override
    public void initialize(Password password) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || Validator.isPassword(value);
    }

}
