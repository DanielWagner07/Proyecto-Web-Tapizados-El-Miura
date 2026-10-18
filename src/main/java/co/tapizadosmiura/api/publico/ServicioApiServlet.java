package co.tapizadosmiura.api.publico;

import co.tapizadosmiura.api.ApiServlet;
import co.tapizadosmiura.api.dto.ServicioDTO;
import co.tapizadosmiura.dao.ServicioDAO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * <b>GET /api/servicios</b>: servicios activos del taller en el orden en que se
 * muestran en la web (RF05).
 */
@WebServlet(name = "ServicioApiServlet", urlPatterns = {"/api/servicios"})
public class ServicioApiServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;

    private final transient ServicioDAO servicioDAO = new ServicioDAO();

    @Override
    protected void get(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        ok(response, servicioDAO.listarActivos().stream().map(ServicioDTO::de).toList());
    }
}
