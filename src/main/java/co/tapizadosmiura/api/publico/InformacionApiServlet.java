package co.tapizadosmiura.api.publico;

import co.tapizadosmiura.api.ApiServlet;
import co.tapizadosmiura.api.dto.DatoNegocioDTO;
import co.tapizadosmiura.dao.InformacionNegocioDAO;
import co.tapizadosmiura.modelo.InformacionNegocio;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>GET /api/informacion</b>: datos del negocio editables desde el panel (CU09):
 * contacto, horario, cobertura, trayectoria, políticas y redes sociales.
 *
 * <p>Responde un objeto indexado por clave, por ejemplo
 * {@code {"whatsapp": {"etiqueta": "WhatsApp de ventas", "valor": "573128628147", "grupo": "contacto"}}}.</p>
 */
@WebServlet(name = "InformacionApiServlet", urlPatterns = {"/api/informacion"})
public class InformacionApiServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;

    private final transient InformacionNegocioDAO informacionDAO = new InformacionNegocioDAO();

    @Override
    protected void get(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        ok(response, porClave(informacionDAO.listar()));
    }

    /** Convierte la lista de la BD en el objeto indexado por clave (lo reutiliza el administrador). */
    public static Map<String, DatoNegocioDTO> porClave(List<InformacionNegocio> datos) {
        Map<String, DatoNegocioDTO> resultado = new LinkedHashMap<>();
        for (InformacionNegocio dato : datos) {
            resultado.put(dato.getClave(), DatoNegocioDTO.de(dato));
        }
        return resultado;
    }
}
