package pe.ssimple.ssisfact_api.util;

// Código listo para imprimir de un comprobante: serie + correlativo con 6
// dígitos, ej. "NP01-000001".
public final class ComprobanteCodigoUtil {

    private ComprobanteCodigoUtil() {
    }

    public static String formatear(String serie, Integer numero) {
        if (serie == null || numero == null) {
            return null;
        }
        return serie + "-" + String.format("%06d", numero);
    }
}
