package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.Cron;
import ir.baho.framework.validation.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CronCheck implements ConstraintValidator<Cron, String> {

    @Override
    public void initialize(Cron cron) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || Validator.isCron(value);
    }

}
