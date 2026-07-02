package pe.ssimple.ssisfact_api.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlErrorMapper {

    private static final Pattern CODE_PATTERN = Pattern.compile("(?:Error\\s+)?(\\d{4})");

    private static final Map<Integer, String> MESSAGES = Map.of(
            1062, "Ya existe un registro con esos datos en esta empresa",
            1452, "La categoría, empresa o referencia especificada no existe",
            1048, "Faltan datos requeridos para completar la operación",
            1406, "El valor ingresado supera el tamaño permitido en el campo"
    );

    private SqlErrorMapper() {}

    public static String toSafeMessage(String rawMessage) {
        int code = extractCode(rawMessage);
        return MESSAGES.getOrDefault(code, "Error al procesar la solicitud. Intente nuevamente");
    }

    public static int extractCode(String message) {
        if (message == null) return 0;
        try {
            Matcher matcher = CODE_PATTERN.matcher(message);
            if (matcher.find()) return Integer.parseInt(matcher.group(1));
        } catch (Exception ignored) {}
        return 0;
    }
}
