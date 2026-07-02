package pe.ssimple.ssisfact_api.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SafeTextValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeText {
    String message() default "contiene caracteres no permitidos: < > * ; ' \" \\";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
