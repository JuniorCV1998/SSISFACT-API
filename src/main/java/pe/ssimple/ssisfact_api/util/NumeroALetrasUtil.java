package pe.ssimple.ssisfact_api.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Convierte un monto en soles a su representación en letras para el ticket de
// venta, ej. 260.43 -> "DOSCIENTOS SESENTA CON 43/100 SOLES".
public final class NumeroALetrasUtil {

    private static final String[] UNIDADES = {
            "", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE"
    };
    private static final String[] DIEZ_A_DIECINUEVE = {
            "DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE",
            "DIECISÉIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE"
    };
    private static final String[] DECENAS = {
            "", "", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"
    };
    private static final String[] CENTENAS = {
            "", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS",
            "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"
    };

    private NumeroALetrasUtil() {
    }

    public static String convertir(BigDecimal monto) {

        BigDecimal valor = monto.setScale(2, RoundingMode.HALF_UP);
        long parteEntera = valor.longValue();
        int centavos = valor.subtract(BigDecimal.valueOf(parteEntera)).movePointRight(2).intValue();

        String letras = parteEntera == 0 ? "CERO" : convertirEntero(parteEntera);

        return letras + " CON " + String.format("%02d", centavos) + "/100 SOLES";
    }

    private static String convertirEntero(long numero) {

        if (numero < 1_000_000_000L) {
            return convertirHastaMillones(numero);
        }
        // Más de mil millones no aplica a un ticket de venta; se corta como salvaguarda.
        return convertirHastaMillones(numero % 1_000_000_000L);
    }

    private static String convertirHastaMillones(long numero) {

        if (numero >= 1_000_000) {
            long millones = numero / 1_000_000;
            long resto = numero % 1_000_000;
            String prefijo = millones == 1 ? "UN MILLÓN" : convertirHastaMiles(millones) + " MILLONES";
            return resto == 0 ? prefijo : prefijo + " " + convertirHastaMiles(resto);
        }
        return convertirHastaMiles(numero);
    }

    private static String convertirHastaMiles(long numero) {

        if (numero >= 1000) {
            long miles = numero / 1000;
            long resto = numero % 1000;
            String prefijo = miles == 1 ? "MIL" : convertirCentenas(miles) + " MIL";
            return resto == 0 ? prefijo : prefijo + " " + convertirCentenas(resto);
        }
        return convertirCentenas(numero);
    }

    private static String convertirCentenas(long numero) {

        if (numero == 100) {
            return "CIEN";
        }
        if (numero >= 100) {
            return CENTENAS[(int) (numero / 100)] + " " + convertirDecenas(numero % 100);
        }
        return convertirDecenas(numero);
    }

    private static String convertirDecenas(long numero) {

        if (numero < 10) {
            return UNIDADES[(int) numero];
        }
        if (numero < 20) {
            return DIEZ_A_DIECINUEVE[(int) (numero - 10)];
        }
        if (numero < 30) {
            long unidad = numero - 20;
            return unidad == 0 ? "VEINTE" : "VEINTI" + UNIDADES[(int) unidad];
        }

        long decena = numero / 10;
        long unidad = numero % 10;
        return unidad == 0 ? DECENAS[(int) decena] : DECENAS[(int) decena] + " Y " + UNIDADES[(int) unidad];
    }
}
