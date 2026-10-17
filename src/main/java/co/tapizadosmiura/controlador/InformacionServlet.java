package co.tapizadosmiura.controlador;

import co.tapizadosmiura.dao.InformacionNegocioDAO;
import co.tapizadosmiura.modelo.InformacionNegocio;
import co.tapizadosmiura.util.InformacionValidador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static co.tapizadosmiura.util.ServletUtil.redirigirConAviso;

/**
 * CU09 - Editar la información comercial del negocio (teléfonos, WhatsApp,
 * horario, cobertura, textos de "Nosotros" y políticas, redes sociales).
 *
 * <ul>
 *   <li><b>GET /admin/informacion</b>: formulario con todos los datos, agrupados.</li>
 *   <li><b>POST /admin/informacion</b>: valida y guarda todos los datos en una transacción.
 *       Cada input se llama {@code valor_<clave>}, por ejemplo {@code valor_whatsapp}.</li>
 * </ul>
 */
@WebServlet(name = "InformacionServlet", urlPatterns = {"/admin/informacion"})
public class InformacionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(InformacionServlet.class.getName());
    private static final String RUTA = "/admin/informacion";
    private static final String VISTA = "/WEB-INF/vistas/admin/informacion.jsp";
    private static final String PREFIJO_CAMPO = "valor_";

    private final transient InformacionNegocioDAO informacionDAO = new InformacionNegocioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            mostrarFormulario(request, response, informacionDAO.listar(), new LinkedHashMap<>());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error al consultar la información del negocio", e);
            request.setAttribute("error", "No fue posible cargar la información. Intente de nuevo.");
            mostrarFormulario(request, response, List.of(), new LinkedHashMap<>());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Se recorren los datos de la BD: solo se aceptan claves que existen
            List<InformacionNegocio> datos = informacionDAO.listar();
            Map<String, String> nuevosValores = new LinkedHashMap<>();
            Map<String, String> errores = new LinkedHashMap<>();

            for (InformacionNegocio dato : datos) {
                String enviado = request.getParameter(PREFIJO_CAMPO + dato.getClave());
                if (enviado == null) {
                    continue; // campo no enviado: se conserva el valor actual
                }
                String valor = InformacionValidador.normalizar(dato.getClave(), enviado);
                String error = InformacionValidador.validar(dato.getClave(), dato.getGrupo(), valor);
                if (error != null) {
                    errores.put(dato.getClave(), error);
                }
                dato.setValor(valor); // para volver a mostrar lo escrito si hay errores
                nuevosValores.put(dato.getClave(), valor);
            }

            if (!errores.isEmpty()) {
                mostrarFormulario(request, response, datos, errores);
                return;
            }

            informacionDAO.actualizarValores(nuevosValores);
            redirigirConAviso(request, response, RUTA, "exito",
                    "Información del negocio actualizada.");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error al guardar la información del negocio", e);
            redirigirConAviso(request, response, RUTA, "error",
                    "No fue posible guardar los cambios. Intente de nuevo.");
        }
    }

    /** Envía los datos agrupados por sección (Contacto, Horario...) a la vista. */
    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   List<InformacionNegocio> datos, Map<String, String> errores)
            throws ServletException, IOException {
        Map<String, List<InformacionNegocio>> porGrupo = new LinkedHashMap<>();
        for (InformacionNegocio dato : datos) {
            porGrupo.computeIfAbsent(dato.getNombreGrupo(), g -> new ArrayList<>()).add(dato);
        }
        request.setAttribute("grupos", porGrupo);
        request.setAttribute("errores", errores);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}
