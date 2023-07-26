package ir.baho.framework.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Size(max = 15)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface PhoneNumber {

    Type value() default Type.TELL_OR_CELL;

    String message() default "{ir.baho.framework.validation.PhoneNumber.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    enum Type {
        CEll, TELL, TELL_OR_CELL
    }

}
