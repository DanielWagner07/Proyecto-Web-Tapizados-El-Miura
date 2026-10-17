package co.tapizadosmiura.util;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Conversión de los valores de texto de un formulario a tipos Java.
 * La usan los validadores; todos los métodos devuelven {@code null} si el
 * campo viene vacío o no tiene el formato esperado.
 */
final class CamposFormulario {

    private CamposFormulario() {
    }

    /** Texto sin espacios sobrantes; {@code null} si viene vacío. */
    static String texto(Map<String, String> campos, String nombre) {
        String valor = campos.get(nombre);
        if (valor == null) {
            return null;
        }
        valor = valor.trim();
        return valor.isEmpty() ? null : valor;
    }

    /** Texto opcional con longitud máxima; si la supera, registra el error. */
    static String textoLimitado(Map<String, String> campos, String nombre, int maximo,
                                Map<String, String> errores) {
        String valor = texto(campos, nombre);
        if (valor != null && valor.length() > maximo) {
            errores.put(nombre, "Máximo " + maximo + " caracteres.");
        }
        return valor;
    }

    static Integer entero(Map<String, String> campos, String nombre) {
        String valor = texto(campos, nombre);
        if (valor == null) {
            return null;
        }
        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Acepta coma o punto como separador decimal (ej. 62,2 o 62.2). */
    static BigDecimal decimal(Map<String, String> campos, String nombre) {
        String valor = texto(campos, nombre);
        if (valor == null) {
            return null;
        }
        try {
            return new BigDecimal(valor.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
