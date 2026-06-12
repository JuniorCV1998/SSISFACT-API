package pe.ssimple.ssisfact_api.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UsernameValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUsername {
    String message() default "El username debe ser un email válido o un documento de 6 a 12 dígitos";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}