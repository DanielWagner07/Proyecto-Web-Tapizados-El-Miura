package co.tapizadosmiura.api.admin;

import co.tapizadosmiura.api.ApiExcepcion;
import co.tapizadosmiura.api.ApiServlet;
import co.tapizadosmiura.api.CamposJson;
import co.tapizadosmiura.api.dto.CotizacionDTO;
import co.tapizadosmiura.dao.CotizacionDAO;
import co.tapizadosmiura.modelo.Cotizacion;

import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Seguimiento de las cotizaciones recibidas. Requiere sesión de administrador.
 *
 * <ul>
 *   <li><b>GET /api/admin/cotizaciones</b>: listado con totales, las más recientes primero
 *       ({@code estado} opcional: pendiente, atendida, cerrada o descartada).</li>
 *   <li><b>GET /api/admin/cotizaciones/{id}</b>: una cotización con sus productos.</li>
 *   <li><b>PUT /api/admin/cotizaciones/{id}/estado</b>
 *       {@code {"estado": "atendida", "notaInterna": "Cliente pidió otro color"}}: cambia el estado;
 *       la nota es opcional (si no se envía se conserva; vacía la borra).</li>
 * </ul>
 */
@WebServlet(name = "CotizacionAdminApiServlet", urlPatterns = {"/api/admin/cotizaciones/*"})
public class CotizacionAdminApiServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;
    private static final int MAXIMO_NOTA = 2000;
    private static final String ESTADOS_TEXTO = "pendiente, atendida, cerrada o descartada";

    private final transient CotizacionDAO cotizacionDAO = new CotizacionDAO();

    @Override
    protected void get(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        List<String> ruta = segmentos(request);
        if (ruta.isEmpty()) {
            String estado = parametro(request, "estado");
            if (!estado.isEmpty() && !Cotizacion.ESTADOS.contains(estado)) {
                throw ApiExcepcion.solicitudInvalida("Parámetro con errores.",
                        Map.of("estado", "Use " + ESTADOS_TEXTO + "."));
            }
            ok(response, cotizacionDAO.listar(estado).stream().map(CotizacionDTO::resumen).toList());
        } else if (ruta.size() == 1) {
            ok(response, CotizacionDTO.detalle(existente(id(ruta.get(0)))));
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
        int idCotizacion = id(ruta.get(0));
        existente(idCotizacion);

        JsonObject cuerpo = leerJson(request);
        String estado = CamposJson.textoOpcional(cuerpo, "estado");
        if (estado == null || !Cotizacion.ESTADOS.contains(estado)) {
            throw ApiExcepcion.solicitudInvalida("Hay campos con errores.",
                    Map.of("estado", "Es obligatorio. Use " + ESTADOS_TEXTO + "."));
        }
        boolean cambiarNota = cuerpo.has("notaInterna");
        String nota = CamposJson.textoOpcional(cuerpo, "notaInterna");
        if (nota != null && nota.length() > MAXIMO_NOTA) {
            throw ApiExcepcion.solicitudInvalida("Hay campos con errores.",
                    Map.of("notaInterna", "Máximo " + MAXIMO_NOTA + " caracteres."));
        }

        cotizacionDAO.cambiarEstado(idCotizacion, estado, cambiarNota, nota);
        exito(response, HttpServletResponse.SC_OK,
                "Cotización N.º " + idCotizacion + " marcada como " + estado + ".",
                CotizacionDTO.detalle(existente(idCotizacion)));
    }

    /** @throws ApiExcepcion 404 si la cotización no existe */
    private Cotizacion existente(int idCotizacion) throws SQLException {
        Cotizacion cotizacion = cotizacionDAO.buscarPorId(idCotizacion);
        if (cotizacion == null) {
            throw ApiExcepcion.noEncontrado("La cotización no existe.");
        }
        return cotizacion;
    }
}
