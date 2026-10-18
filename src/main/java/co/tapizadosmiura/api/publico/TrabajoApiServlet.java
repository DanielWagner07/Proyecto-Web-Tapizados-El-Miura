package co.tapizadosmiura.api.publico;

import co.tapizadosmiura.api.ApiExcepcion;
import co.tapizadosmiura.api.ApiServlet;
import co.tapizadosmiura.api.dto.TrabajoDTO;
import co.tapizadosmiura.dao.TrabajoDAO;
import co.tapizadosmiura.modelo.Trabajo;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Galería pública de trabajos realizados (RF05 / CU06).
 *
 * <ul>
 *   <li><b>GET /api/trabajos</b>: trabajos visibles con sus fotos. Parámetro opcional
 *       {@code tipo}: {@code restauracion} (página "Tapizado y restauración") o
 *       {@code colaboracion} (página "Renovaciones").</li>
 *   <li><b>GET /api/trabajos/{id}</b>: un trabajo visible; 404 si no existe o está oculto.</li>
 * </ul>
 */
@WebServlet(name = "TrabajoApiServlet", urlPatterns = {"/api/trabajos/*"})
public class TrabajoApiServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;

    /** Tipos de trabajo admitidos en el filtro. */
    public static final Set<String> TIPOS = Set.of(Trabajo.RESTAURACION, Trabajo.COLABORACION);

    private final transient TrabajoDAO trabajoDAO = new TrabajoDAO();

    @Override
    protected void get(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        List<String> ruta = segmentos(request);
        String ctx = contexto(request);
        if (ruta.isEmpty()) {
            String tipo = tipoValido(parametro(request, "tipo"));
            ok(response, trabajoDAO.listarPublicos(tipo).stream().map(t -> TrabajoDTO.publico(t, ctx)).toList());
        } else if (ruta.size() == 1) {
            Trabajo trabajo = trabajoDAO.buscarPublicoPorId(id(ruta.get(0)));
            if (trabajo == null) {
                throw ApiExcepcion.noEncontrado("El trabajo no existe o no está publicado en la galería.");
            }
            ok(response, TrabajoDTO.publico(trabajo, ctx));
        } else {
            throw ApiExcepcion.noEncontrado("La ruta solicitada no existe.");
        }
    }

    /**
     * @return el tipo si es válido, o cadena vacía si no se envió
     * @throws ApiExcepcion 400 si se envió un tipo desconocido
     */
    public static String tipoValido(String tipo) {
        if (!tipo.isEmpty() && !TIPOS.contains(tipo)) {
            throw ApiExcepcion.solicitudInvalida("Parámetro con errores.",
                    Map.of("tipo", "Use \"restauracion\" o \"colaboracion\"."));
        }
        return tipo;
    }
}
