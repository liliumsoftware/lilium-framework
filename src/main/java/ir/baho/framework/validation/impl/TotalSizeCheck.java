package ir.baho.framework.validation.impl;

import ir.baho.framework.validation.TotalSize;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TotalSizeCheck implements ConstraintValidator<TotalSize, Iterable<?>> {

    private int maxLength;
    private int separatorLength;

    @Override
    public void initialize(TotalSize totalSize) {
        this.maxLength = totalSize.value();
        this.separatorLength = totalSize.separatorLength();
    }

    @Override
    public boolean isValid(Iterable<?> objects, ConstraintValidatorContext context) {
        if (objects == null) {
            return true;
        }
        int totalLength = 0;
        int count = 0;
        for (Object o : objects) {
            if (o != null) {
                totalLength += o.toString().length();
                count++;
            }
        }
        // Calculate total length including separators
        totalLength += separatorLength * (count - 1);
        return totalLength <= maxLength;
    }

}
