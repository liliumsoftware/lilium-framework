package ir.baho.framework.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface TotalSize {

    int value();

    int separatorLength() default 1;

    String message() default "{ir.baho.framework.validation.TotalSize.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
