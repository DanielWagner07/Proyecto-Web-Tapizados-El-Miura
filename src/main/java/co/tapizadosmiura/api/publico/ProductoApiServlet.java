package co.tapizadosmiura.api.publico;

import co.tapizadosmiura.api.ApiExcepcion;
import co.tapizadosmiura.api.ApiServlet;
import co.tapizadosmiura.api.dto.ProductoDTO;
import co.tapizadosmiura.dao.ProductoDAO;
import co.tapizadosmiura.modelo.Producto;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Catálogo público de muebles.
 *
 * <ul>
 *   <li><b>GET /api/productos</b> (RF01 / CU02): productos visibles. Parámetros opcionales:
 *       {@code categoria} (id) y {@code q} (texto en nombre, referencia o descripción).</li>
 *   <li><b>GET /api/productos/{id}</b> (RF02 / CU03): ficha técnica con todas las fotos.
 *       Responde 404 si el producto no existe o está oculto.</li>
 * </ul>
 */
@WebServlet(name = "ProductoApiServlet", urlPatterns = {"/api/productos/*"})
public class ProductoApiServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;
    private static final int MAXIMO_BUSQUEDA = 100;

    private final transient ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void get(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        List<String> ruta = segmentos(request);
        switch (ruta.size()) {
            case 0 -> listar(request, response);
            case 1 -> detalle(id(ruta.get(0)), request, response);
            default -> throw ApiExcepcion.noEncontrado("La ruta solicitada no existe.");
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        String categoria = parametro(request, "categoria");
        int idCategoria = 0;
        if (!categoria.isEmpty()) {
            try {
                idCategoria = Integer.parseInt(categoria);
            } catch (NumberFormatException e) {
                idCategoria = -1;
            }
            if (idCategoria <= 0) {
                throw ApiExcepcion.solicitudInvalida("Parámetro con errores.",
                        Map.of("categoria", "Debe ser el id numérico de una categoría."));
            }
        }
        String busqueda = parametro(request, "q");
        if (busqueda.length() > MAXIMO_BUSQUEDA) {
            throw ApiExcepcion.solicitudInvalida("Parámetro con errores.",
                    Map.of("q", "Máximo " + MAXIMO_BUSQUEDA + " caracteres."));
        }
        String ctx = contexto(request);
        ok(response, productoDAO.listarPublicos(idCategoria, busqueda).stream()
                .map(p -> ProductoDTO.resumen(p, ctx)).toList());
    }

    private void detalle(int idProducto, HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        Producto producto = productoDAO.buscarPublicoPorId(idProducto);
        if (producto == null) {
            throw ApiExcepcion.noEncontrado("El producto no existe o no está disponible en el catálogo.");
        }
        ok(response, ProductoDTO.detalle(producto, productoDAO.listarImagenes(idProducto), contexto(request)));
    }
}
