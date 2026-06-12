package pe.ssimple.ssisfact_api.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UsernameValidator implements ConstraintValidator<ValidUsername, String> {

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.isBlank()) {
            return false;
        }

        // Validar si es email
        if (isValidEmail(username)) {
            return true;
        }

        // Validar si es documento de 6 a 12 dígitos
        if (isValidDocument(username)) {
            return true;
        }

        return false;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private boolean isValidDocument(String documento) {
        // 👇 CAMBIO AQUÍ
        return documento.matches("^\\d{6,12}$");
    }
}