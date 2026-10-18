package co.tapizadosmiura.api;

import co.tapizadosmiura.api.dto.ErrorDTO;
import co.tapizadosmiura.api.dto.RespuestaDTO;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base de todos los servicios de la API ({@code /api/...}).
 *
 * <p>Se encarga de lo que es igual en todos los servicios, para que cada
 * servlet solo escriba su lógica:</p>
 * <ul>
 *   <li>Responde siempre en JSON (UTF-8), también cuando hay errores.</li>
 *   <li>Convierte {@link ApiExcepcion} en el formato único de error y los errores
 *       inesperados (por ejemplo, la BD apagada) en un 500 sin detalles internos.</li>
 *   <li>Responde 405 en JSON a los métodos que el servicio no implementa.</li>
 *   <li>Lee el cuerpo JSON y exige {@code Content-Type: application/json}. Ese
 *       requisito también impide que un formulario de otra página dispare
 *       acciones del administrador (protección CSRF).</li>
 * </ul>
 *
 * <p>Cada servicio sobrescribe solo los métodos que admite: {@link #get},
 * {@link #post}, {@link #put} o {@link #delete}.</p>
 */
public abstract class ApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ApiServlet.class.getName());

    /** Tamaño máximo aceptado para un cuerpo JSON (los de esta API son pequeños). */
    private static final int MAXIMO_CUERPO_BYTES = 64 * 1024;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Los datos cambian desde el panel: el navegador no debe guardar respuestas viejas.
        response.setHeader("Cache-Control", "no-store");
        try {
            switch (request.getMethod()) {
                case "GET", "HEAD" -> get(request, response);
                case "POST" -> post(request, response);
                case "PUT" -> put(request, response);
                case "DELETE" -> delete(request, response);
                default -> throw ApiExcepcion.metodoNoPermitido(request.getMethod());
            }
        } catch (ApiExcepcion e) {
            enviarError(response, e.getEstadoHttp(), e.getMessage(), e);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos en " + request.getRequestURI(), e);
            enviarError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "No fue posible consultar la base de datos. Intente de nuevo en unos minutos.", null);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Error inesperado en " + request.getRequestURI(), e);
            enviarError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Ocurrió un error inesperado en el servidor.", null);
        }
    }

    // ------------------------------------------------------------------
    // Métodos que cada servicio sobrescribe (por defecto: 405)
    // ------------------------------------------------------------------

    protected void get(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        throw ApiExcepcion.metodoNoPermitido("GET");
    }

    protected void post(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        throw ApiExcepcion.metodoNoPermitido("POST");
    }

    protected void put(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        throw ApiExcepcion.metodoNoPermitido("PUT");
    }

    protected void delete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        throw ApiExcepcion.metodoNoPermitido("DELETE");
    }

    // ------------------------------------------------------------------
    // Respuestas
    // ------------------------------------------------------------------

    /**
     * Escribe un objeto como JSON con el código indicado.
     *
     * @param estadoHttp código HTTP (200, 201...)
     * @param cuerpo     objeto a convertir (DTO, lista o mapa)
     */
    protected static void responder(HttpServletResponse response, int estadoHttp, Object cuerpo)
            throws IOException {
        response.setStatus(estadoHttp);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Json.GSON.toJson(cuerpo, response.getWriter());
    }

    /** 200 con el dato consultado (lecturas). */
    protected static void ok(HttpServletResponse response, Object datos) throws IOException {
        responder(response, HttpServletResponse.SC_OK, datos);
    }

    /** Respuesta de una escritura: mensaje para el usuario y, si aplica, el recurso resultante. */
    protected static void exito(HttpServletResponse response, int estadoHttp, String mensaje, Object datos)
            throws IOException {
        responder(response, estadoHttp, new RespuestaDTO(mensaje, datos));
    }

    private static void enviarError(HttpServletResponse response, int estadoHttp, String mensaje,
                                    ApiExcepcion origen) throws IOException {
        if (response.isCommitted()) {
            return; // ya se envió parte de la respuesta: no se puede cambiar el código
        }
        response.resetBuffer();
        responder(response, estadoHttp, new ErrorDTO(mensaje, origen == null ? null : origen.getErrores()));
    }

    // ------------------------------------------------------------------
    // Lectura de la petición
    // ------------------------------------------------------------------

    /**
     * Partes de la ruta después del prefijo del servlet. Para {@code /api/productos/5/estado}
     * (servlet en {@code /api/productos/*}) devuelve {@code ["5", "estado"]}; para
     * {@code /api/productos} o {@code /api/productos/} devuelve una lista vacía.
     */
    protected static List<String> segmentos(HttpServletRequest request) {
        List<String> partes = new ArrayList<>();
        String ruta = request.getPathInfo();
        if (ruta != null) {
            for (String parte : ruta.split("/")) {
                if (!parte.isEmpty()) {
                    partes.add(parte);
                }
            }
        }
        return partes;
    }

    /**
     * Convierte un segmento de la ruta en un id.
     *
     * @throws ApiExcepcion 400 si no es un número entero positivo
     */
    protected static int id(String segmento) {
        try {
            int valor = Integer.parseInt(segmento);
            if (valor > 0) {
                return valor;
            }
        } catch (NumberFormatException e) {
            // se responde abajo
        }
        throw ApiExcepcion.solicitudInvalida("El id de la ruta debe ser un número entero positivo.");
    }

    /**
     * Lee el cuerpo de la petición como un objeto JSON.
     *
     * @throws ApiExcepcion 415 si no es JSON; 400 si está vacío, mal escrito o no es un objeto
     */
    protected static JsonObject leerJson(HttpServletRequest request) throws IOException {
        String tipo = request.getContentType();
        if (tipo == null || !tipo.toLowerCase().startsWith("application/json")) {
            throw ApiExcepcion.tipoNoSoportado();
        }
        if (request.getContentLengthLong() > MAXIMO_CUERPO_BYTES) {
            throw ApiExcepcion.solicitudInvalida("El cuerpo de la petición es demasiado grande.");
        }
        try (Reader lector = request.getReader()) {
            JsonElement elemento = JsonParser.parseReader(lector);
            if (!elemento.isJsonObject()) {
                throw ApiExcepcion.solicitudInvalida("El cuerpo debe ser un objeto JSON, por ejemplo {\"campo\": \"valor\"}.");
            }
            return elemento.getAsJsonObject();
        } catch (JsonParseException e) {
            throw ApiExcepcion.solicitudInvalida("El JSON enviado no es válido: revise comillas, comas y llaves.");
        }
    }

    /** Parámetro de la URL sin espacios sobrantes; cadena vacía si no se envió. */
    protected static String parametro(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }

    /** Ruta de la aplicación (ej. "/tapizados-miura"), para armar las URL de las imágenes. */
    protected static String contexto(HttpServletRequest request) {
        return request.getContextPath();
    }
}
