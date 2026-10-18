package co.tapizadosmiura.api.admin;

import co.tapizadosmiura.api.ApiExcepcion;
import co.tapizadosmiura.api.ApiServlet;
import co.tapizadosmiura.api.CamposJson;
import co.tapizadosmiura.api.dto.TrabajoDTO;
import co.tapizadosmiura.api.publico.TrabajoApiServlet;
import co.tapizadosmiura.dao.TrabajoDAO;
import co.tapizadosmiura.modelo.Trabajo;
import co.tapizadosmiura.util.AlmacenImagenes;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Gestión de la galería de trabajos por la API (CU08). Requiere sesión de administrador.
 *
 * <ul>
 *   <li><b>GET /api/admin/trabajos</b>: todos los trabajos, visibles y ocultos ({@code tipo} opcional).</li>
 *   <li><b>GET /api/admin/trabajos/{id}</b>: un trabajo con todas sus fotos.</li>
 *   <li><b>PUT /api/admin/trabajos/{id}/estado</b> {@code {"estado": true|false}}: lo muestra u oculta.</li>
 *   <li><b>DELETE /api/admin/trabajos/{id}</b>: lo elimina junto con sus fotos y sus archivos.</li>
 * </ul>
 *
 * <p>Crear y editar trabajos exige subir fotos (RF08), por eso se hace desde el panel.</p>
 */
@WebServlet(name = "TrabajoAdminApiServlet", urlPatterns = {"/api/admin/trabajos/*"})
public class TrabajoAdminApiServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;

    private final transient TrabajoDAO trabajoDAO = new TrabajoDAO();

    @Override
    protected void get(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        List<String> ruta = segmentos(request);
        String ctx = contexto(request);
        if (ruta.isEmpty()) {
            String tipo = TrabajoApiServlet.tipoValido(parametro(request, "tipo"));
            ok(response, trabajoDAO.listar(tipo).stream().map(t -> TrabajoDTO.admin(t, ctx)).toList());
        } else if (ruta.size() == 1) {
            ok(response, TrabajoDTO.admin(existente(id(ruta.get(0))), ctx));
        } else {
            throw ApiExcepcion.noEncontrado("La ruta solicitada no existe.");
        }
    }

    @Override
    protected void put(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        List<String> ruta = segmentos(request);
        if (ruta.size() != 2 || !"estado".equals(ruta.get(1))) {
            throw ApiExcepcion.noEncontrado("La ruta solicitada no existe.");
        }
        Trabajo trabajo = existente(id(ruta.get(0)));
        boolean visible = CamposJson.booleanoObligatorio(leerJson(request), "estado");
        trabajoDAO.cambiarEstado(trabajo.getIdTrabajo(), visible);
        trabajo.setEstado(visible);
        exito(response, HttpServletResponse.SC_OK,
                visible ? "Trabajo visible en la galería." : "Trabajo oculto de la galería.",
                TrabajoDTO.admin(trabajo, contexto(request)));
    }

    @Override
    protected void delete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        List<String> ruta = segmentos(request);
        if (ruta.size() != 1) {
            throw ApiExcepcion.noEncontrado("La ruta solicitada no existe.");
        }
        int idTrabajo = id(ruta.get(0));
        Trabajo trabajo = existente(idTrabajo);
        List<String> rutas = trabajoDAO.eliminar(idTrabajo);
        if (rutas != null) {
            // Primero la BD; después los archivos, que ya nadie usa
            rutas.forEach(r -> AlmacenImagenes.eliminar(getServletContext(), r));
        }
        exito(response, HttpServletResponse.SC_OK,
                "Trabajo \"" + trabajo.getTitulo() + "\" eliminado junto con sus fotos.", null);
    }

    /** @throws ApiExcepcion 404 si el trabajo no existe */
    private Trabajo existente(int idTrabajo) throws SQLException {
        Trabajo trabajo = trabajoDAO.buscarPorId(idTrabajo);
        if (trabajo == null) {
            throw ApiExcepcion.noEncontrado("El trabajo no existe.");
        }
        return trabajo;
    }
}
