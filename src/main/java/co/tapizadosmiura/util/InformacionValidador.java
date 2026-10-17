package co.tapizadosmiura.util;

import java.util.regex.Pattern;

/**
 * Validación de los datos editables del negocio (CU09).
 *
 * <p>Todos los valores son obligatorios y tienen máximo {@value #LONGITUD_MAXIMA}
 * caracteres (CU09 flujo 2a y CHECK {@code ck_informacion_negocio_valor}).
 * Algunas claves tienen además un formato propio:</p>
 * <ul>
 *   <li>{@code whatsapp}: solo números con indicativo de país (lo exige el enlace wa.me).</li>
 *   <li>{@code telefono}: números, espacios, guiones o el signo +.</li>
 *   <li>{@code correo}: dirección de correo válida.</li>
 *   <li>Grupo {@code redes}: dirección web que empiece por http:// o https://.</li>
 * </ul>
 */
public final class InformacionValidador {

    public static final int LONGITUD_MAXIMA = 2000;

    private static final Pattern WHATSAPP = Pattern.compile("\\d{10,15}");
    private static final Pattern TELEFONO = Pattern.compile("[+]?[0-9][0-9 -]{5,19}");
    private static final Pattern CORREO = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private static final Pattern URL = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);

    private InformacionValidador() {
    }

    /**
     * Deja el valor en el formato en que se guarda. Por ejemplo, el WhatsApp
     * "+57 312 000 0000" se guarda como "573120000000".
     *
     * @param clave clave del dato
     * @param valor texto escrito por el administrador (puede ser null)
     * @return valor sin espacios sobrantes y normalizado
     */
    public static String normalizar(String clave, String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = valor.trim();
        if ("whatsapp".equals(clave)) {
            limpio = limpio.replaceAll("[\\s+()-]", "");
        }
        return limpio;
    }

    /**
     * Valida un dato ya normalizado.
     *
     * @param clave clave del dato (whatsapp, correo...)
     * @param grupo grupo al que pertenece (contacto, redes...)
     * @param valor valor normalizado
     * @return mensaje de error, o {@code null} si es válido
     */
    public static String validar(String clave, String grupo, String valor) {
        if (valor == null || valor.isEmpty()) {
            return "Este dato es obligatorio.";
        }
        if (valor.length() > LONGITUD_MAXIMA) {
            return "Máximo " + LONGITUD_MAXIMA + " caracteres.";
        }
        if ("whatsapp".equals(clave) && !WHATSAPP.matcher(valor).matches()) {
            return "Escriba solo números con el indicativo del país, ej. 573120000000.";
        }
        if ("telefono".equals(clave) && !TELEFONO.matcher(valor).matches()) {
            return "Use solo números, espacios, guiones o el signo +.";
        }
        if ("correo".equals(clave) && !CORREO.matcher(valor).matches()) {
            return "Escriba un correo válido, ej. contacto@tapizadosmiura.com.";
        }
        if ("redes".equals(grupo) && !URL.matcher(valor).matches()) {
            return "Escriba la dirección completa, empezando por https://";
        }
        return null;
    }
}
