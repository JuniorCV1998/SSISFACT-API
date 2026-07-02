package pe.ssimple.ssisfact_api.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class SafeTextValidator implements ConstraintValidator<SafeText, String> {

    private static final Pattern FORBIDDEN = Pattern.compile("[<>*;'\"\\\\]");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        return !FORBIDDEN.matcher(value).find();
    }
}
