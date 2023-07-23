package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.PhoneNumber;
import ir.baho.framework.validation.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberCheck implements ConstraintValidator<PhoneNumber, String> {

    private PhoneNumber.Type type;

    @Override
    public void initialize(PhoneNumber phoneNumber) {
        this.type = phoneNumber.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        if (type == PhoneNumber.Type.CEll) {
            return Validator.isCell(value);
        } else if (type == PhoneNumber.Type.TELL) {
            return Validator.isTel(value);
        } else {
            return Validator.isTelOrCell(value);
        }
    }

}
