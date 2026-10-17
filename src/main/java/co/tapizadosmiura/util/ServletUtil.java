package co.tapizadosmiura.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Operaciones repetidas en los servlets del panel.
 */
public final class ServletUtil {

    private ServletUtil() {
    }

    /**
     * Parámetro de la petición sin espacios sobrantes.
     *
     * @return el valor, o cadena vacía si no se envió
     */
    public static String parametro(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }

    /** Convierte a entero positivo; devuelve 0 si el texto no es un número válido. */
    public static int enteroPositivo(String texto) {
        try {
            return Math.max(0, Integer.parseInt(texto));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Copia a un mapa los campos de texto indicados (los que no se enviaron no se incluyen,
     * así una casilla desmarcada queda ausente).
     */
    public static Map<String, String> leerCampos(HttpServletRequest request, String... nombres) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (String nombre : nombres) {
            String valor = request.getParameter(nombre);
            if (valor != null) {
                campos.put(nombre, valor);
            }
        }
        return campos;
    }

    /** true si la petición es multipart/form-data (formularios con archivos). */
    public static boolean esMultipart(HttpServletRequest request) {
        String tipo = request.getContentType();
        return tipo != null && tipo.startsWith("multipart/");
    }

    /**
     * Guarda un aviso de una sola lectura en la sesión y redirige (patrón Post/Redirect/Get).
     * La cabecera del panel lo muestra y lo borra.
     *
     * @param ruta  ruta dentro de la aplicación, por ejemplo "/admin/trabajos"
     * @param tipo  "exito" o "error" (define el color del aviso)
     * @param texto mensaje para el usuario
     */
    public static void redirigirConAviso(HttpServletRequest request, HttpServletResponse response,
                                         String ruta, String tipo, String texto) throws IOException {
        request.getSession().setAttribute("aviso", Map.of("tipo", tipo, "texto", texto));
        response.sendRedirect(request.getContextPath() + ruta);
    }
}
