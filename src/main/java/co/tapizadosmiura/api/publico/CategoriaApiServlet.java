package co.tapizadosmiura.api.publico;

import co.tapizadosmiura.api.ApiServlet;
import co.tapizadosmiura.api.dto.CategoriaDTO;
import co.tapizadosmiura.dao.CategoriaDAO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * <b>GET /api/categorias</b>: categorías visibles del catálogo con su número de
 * productos visibles (RF01 / CU02). Alimenta el menú de categorías del sitio.
 */
@WebServlet(name = "CategoriaApiServlet", urlPatterns = {"/api/categorias"})
public class CategoriaApiServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;

    private final transient CategoriaDAO categoriaDAO = new CategoriaDAO();

    @Override
    protected void get(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        ok(response, categoriaDAO.listarPublicas().stream().map(CategoriaDTO::de).toList());
    }
}
