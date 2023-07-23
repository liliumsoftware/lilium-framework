package ir.baho.framework.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface Digit {

    String message() default "{ir.baho.framework.validation.Digit.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int minDecimalLen() default 0;

    int minIntegerLen() default 0;

    int maxDecimalLen() default 9;

    int maxIntegerLen() default 15;

    double min() default Double.MIN_VALUE;

    double max() default Double.MAX_VALUE;

}
