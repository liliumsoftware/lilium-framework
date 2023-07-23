package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.CompanyId;
import ir.baho.framework.validation.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CompanyIdCheck implements ConstraintValidator<CompanyId, String> {

    @Override
    public void initialize(CompanyId id) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || Validator.isCompanyId(value);
    }

}
