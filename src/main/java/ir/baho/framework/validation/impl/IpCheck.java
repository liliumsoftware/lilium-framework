package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.Ip;
import ir.baho.framework.validation.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IpCheck implements ConstraintValidator<Ip, String> {

    @Override
    public void initialize(Ip ip) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || Validator.isIp(value);
    }

}
