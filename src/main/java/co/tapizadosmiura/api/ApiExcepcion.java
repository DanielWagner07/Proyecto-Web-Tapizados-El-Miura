package co.tapizadosmiura.api;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Error previsto de la API: lleva el código HTTP y el mensaje que verá quien
 * consume el servicio.
 *
 * <p>{@link ApiServlet} la captura y la convierte en una respuesta JSON con el
 * formato único de error:</p>
 * <pre>{"mensaje": "...", "errores": {"campo": "detalle"}}</pre>
 *
 * <p>Se lanza desde cualquier punto del servicio con los métodos de fábrica,
 * por ejemplo {@code throw ApiExcepcion.noEncontrado("El producto no existe.")}.</p>
 */
public class ApiExcepcion extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int estadoHttp;
    private final transient Map<String, String> errores;

    public ApiExcepcion(int estadoHttp, String mensaje, Map<String, String> errores) {
        super(mensaje);
        this.estadoHttp = estadoHttp;
        // Copia que conserva el orden en que se detectaron los errores
        this.errores = errores == null || errores.isEmpty()
                ? null : Collections.unmodifiableMap(new LinkedHashMap<>(errores));
    }

    /** 400: los datos enviados no son válidos; {@code errores} indica el detalle por campo. */
    public static ApiExcepcion solicitudInvalida(String mensaje, Map<String, String> errores) {
        return new ApiExcepcion(HttpServletResponse.SC_BAD_REQUEST, mensaje, errores);
    }

    /** 400 sin detalle por campo. */
    public static ApiExcepcion solicitudInvalida(String mensaje) {
        return solicitudInvalida(mensaje, null);
    }

    /** 401: hace falta iniciar sesión o las credenciales no son válidas. */
    public static ApiExcepcion noAutorizado(String mensaje) {
        return new ApiExcepcion(HttpServletResponse.SC_UNAUTHORIZED, mensaje, null);
    }

    /** 404: el recurso no existe o no está publicado. */
    public static ApiExcepcion noEncontrado(String mensaje) {
        return new ApiExcepcion(HttpServletResponse.SC_NOT_FOUND, mensaje, null);
    }

    /** 405: la ruta existe pero no admite ese método HTTP. */
    public static ApiExcepcion metodoNoPermitido(String metodo) {
        return new ApiExcepcion(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Esta ruta no admite el método " + metodo + ".", null);
    }

    /** 409: la operación choca con una regla del negocio (referencia repetida, producto sin foto...). */
    public static ApiExcepcion conflicto(String mensaje, Map<String, String> errores) {
        return new ApiExcepcion(HttpServletResponse.SC_CONFLICT, mensaje, errores);
    }

    /** 415: el cuerpo no se envió como JSON. */
    public static ApiExcepcion tipoNoSoportado() {
        return new ApiExcepcion(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                "El cuerpo debe enviarse en JSON con la cabecera Content-Type: application/json.", null);
    }

    public int getEstadoHttp() {
        return estadoHttp;
    }

    /** @return detalle por campo, o {@code null} si no aplica */
    public Map<String, String> getErrores() {
        return errores;
    }
}
