package co.tapizadosmiura.api.admin;

import co.tapizadosmiura.api.ApiExcepcion;
import co.tapizadosmiura.api.ApiServlet;
import co.tapizadosmiura.api.CamposJson;
import co.tapizadosmiura.api.dto.ProductoDTO;
import co.tapizadosmiura.dao.CategoriaDAO;
import co.tapizadosmiura.dao.ProductoDAO;
import co.tapizadosmiura.modelo.Producto;
import co.tapizadosmiura.util.ProductoValidador;
import co.tapizadosmiura.util.ResultadoValidacion;

import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gestión del catálogo por la API (CU07 / RF07). Requiere sesión de administrador.
 *
 * <ul>
 *   <li><b>GET /api/admin/productos</b>: todos los productos, visibles y ocultos ({@code q} opcional).</li>
 *   <li><b>GET /api/admin/productos/{id}</b>: un producto con todas sus fotos.</li>
 *   <li><b>POST /api/admin/productos</b>: crea un producto. <b>Siempre queda oculto</b>, porque la
 *       foto se sube desde el panel; se publica después de subirla.</li>
 *   <li><b>PUT /api/admin/productos/{id}</b>: reemplaza todos los datos del producto (los campos
 *       que no se envían quedan vacíos, igual que en el formulario del panel).</li>
 *   <li><b>PUT /api/admin/productos/{id}/estado</b> {@code {"estado": true|false}}: lo muestra u oculta.</li>
 * </ul>
 *
 * <p>Regla del proyecto: un producto sin foto no puede quedar visible (responde 409).
 * Las validaciones son las mismas del panel ({@link ProductoValidador}).</p>
 */
@WebServlet(name = "ProductoAdminApiServlet", urlPatterns = {"/api/admin/productos/*"})
public class ProductoAdminApiServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;

    /** Campos de texto o número que acepta el cuerpo JSON (mismos nombres del formulario del panel). */
    private static final Set<String> CAMPOS_TEXTO = Set.of(
            "referencia", "nombre", "idCategoria", "descripcion", "precio", "stock",
            "estilo", "color", "materialTapiz", "materialEstructura", "tipoRelleno", "tipoPata",
            "altoCm", "anchoCm", "profundidadCm", "pesoKg", "resistenciaKgPuesto");

    /** Campos booleanos que acepta el cuerpo JSON. */
    private static final Set<String> CAMPOS_BOOLEANOS = Set.of("requiereArmado", "estado");

    private static final String SIN_FOTO =
            "El producto no tiene foto. Súbala desde el panel (Gestor de catálogo) antes de publicarlo.";

    private final transient ProductoDAO productoDAO = new ProductoDAO();
    private final transient CategoriaDAO categoriaDAO = new CategoriaDAO();

    // =====================================================================
    // Consultas
    // =====================================================================

    @Override
    protected void get(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        List<String> ruta = segmentos(request);
        String ctx = contexto(request);
        if (ruta.isEmpty()) {
            ok(response, productoDAO.listar(parametro(request, "q")).stream()
                    .map(p -> ProductoDTO.admin(p, null, ctx)).toList());
        } else if (ruta.size() == 1) {
            Producto producto = existente(id(ruta.get(0)));
            ok(response, ProductoDTO.admin(producto, productoDAO.listarImagenes(producto.getIdProducto()), ctx));
        } else {
            throw ApiExcepcion.noEncontrado("La ruta solicitada no existe.");
        }
    }

    // =====================================================================
    // Crear
    // =====================================================================

    @Override
    protected void post(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        if (!segmentos(request).isEmpty()) {
            throw ApiExcepcion.noEncontrado("La ruta solicitada no existe.");
        }
        Map<String, String> campos = CamposJson.aCampos(leerJson(request), CAMPOS_TEXTO, CAMPOS_BOOLEANOS);
        boolean pidioPublicar = campos.remove("estado") != null; // se crea oculto: aún no tiene foto

        Producto producto = validar(campos, 0);
        producto.setEstado(false);
        int id = productoDAO.crear(producto, null);

        String mensaje = "Producto \"" + producto.getNombre() + "\" creado y oculto. "
                + "Súbale la foto desde el panel para publicarlo en el catálogo."
                + (pidioPublicar ? " (Se ignoró \"estado\": un producto sin foto no puede quedar visible.)" : "");
        exito(response, HttpServletResponse.SC_CREATED, mensaje,
                ProductoDTO.admin(productoDAO.buscarPorId(id), List.of(), contexto(request)));
    }

    // =====================================================================
    // Actualizar y cambiar estado
    // =====================================================================

    @Override
    protected void put(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        List<String> ruta = segmentos(request);
        if (ruta.size() == 1) {
            actualizar(id(ruta.get(0)), request, response);
        } else if (ruta.size() == 2 && "estado".equals(ruta.get(1))) {
            cambiarEstado(id(ruta.get(0)), request, response);
        } else {
            throw ApiExcepcion.noEncontrado("La ruta solicitada no existe.");
        }
    }

    private void actualizar(int idProducto, HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        Producto actual = existente(idProducto);
        Map<String, String> campos = CamposJson.aCampos(leerJson(request), CAMPOS_TEXTO, CAMPOS_BOOLEANOS);
        Producto producto = validar(campos, idProducto);
        producto.setIdProducto(idProducto);
        if (producto.isEstado() && actual.getImagenPrincipal() == null) {
            throw ApiExcepcion.conflicto(SIN_FOTO, Map.of("estado", "Requiere una foto."));
        }
        productoDAO.actualizar(producto, null); // la foto actual se conserva
        exito(response, HttpServletResponse.SC_OK, "Producto \"" + producto.getNombre() + "\" actualizado.",
                ProductoDTO.admin(productoDAO.buscarPorId(idProducto),
                        productoDAO.listarImagenes(idProducto), contexto(request)));
    }

    private void cambiarEstado(int idProducto, HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        Producto actual = existente(idProducto);
        boolean visible = CamposJson.booleanoObligatorio(leerJson(request), "estado");
        if (visible && actual.getImagenPrincipal() == null) {
            throw ApiExcepcion.conflicto(SIN_FOTO, Map.of("estado", "Requiere una foto."));
        }
        productoDAO.cambiarEstado(idProducto, visible);
        actual.setEstado(visible);
        exito(response, HttpServletResponse.SC_OK,
                visible ? "Producto visible en el catálogo." : "Producto oculto del catálogo.",
                ProductoDTO.admin(actual, null, contexto(request)));
    }

    // =====================================================================
    // Apoyo
    // =====================================================================

    /** @throws ApiExcepcion 404 si el producto no existe (visible u oculto) */
    private Producto existente(int idProducto) throws SQLException {
        Producto producto = productoDAO.buscarPorId(idProducto);
        if (producto == null) {
            throw ApiExcepcion.noEncontrado("El producto no existe.");
        }
        return producto;
    }

    /**
     * Aplica las reglas del panel y las que dependen de la BD.
     *
     * @throws ApiExcepcion 400 si hay datos inválidos; 409 si la referencia ya existe
     */
    private Producto validar(Map<String, String> campos, int idProducto) throws SQLException {
        ResultadoValidacion<Producto> resultado = ProductoValidador.validar(campos);
        Map<String, String> errores = new LinkedHashMap<>(resultado.getErrores());
        Producto producto = resultado.getObjeto();
        if (!errores.containsKey("idCategoria") && !categoriaDAO.existeActiva(producto.getIdCategoria())) {
            errores.put("idCategoria", "La categoría no existe o está desactivada.");
        }
        if (!errores.isEmpty()) {
            throw ApiExcepcion.solicitudInvalida("Hay campos con errores.", errores);
        }
        if (productoDAO.existeReferencia(producto.getReferencia(), idProducto)) {
            throw ApiExcepcion.conflicto("Ya existe otro producto con esta referencia.",
                    Map.of("referencia", "Ya existe otro producto con la referencia " + producto.getReferencia() + "."));
        }
        return producto;
    }
}
