package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.PostalCode;
import ir.baho.framework.validation.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PostalCodeCheck implements ConstraintValidator<PostalCode, String> {

    @Override
    public void initialize(PostalCode postalCode) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || Validator.isPostalCode(value);
    }

}
