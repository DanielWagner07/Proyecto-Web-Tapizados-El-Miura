package co.tapizadosmiura.controlador;

import co.tapizadosmiura.dao.CategoriaDAO;
import co.tapizadosmiura.dao.ProductoDAO;
import co.tapizadosmiura.modelo.Producto;
import co.tapizadosmiura.util.AlmacenImagenes;
import co.tapizadosmiura.util.ProductoValidador;
import co.tapizadosmiura.util.ResultadoValidacion;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static co.tapizadosmiura.util.ServletUtil.enteroPositivo;
import static co.tapizadosmiura.util.ServletUtil.esMultipart;
import static co.tapizadosmiura.util.ServletUtil.leerCampos;
import static co.tapizadosmiura.util.ServletUtil.parametro;
import static co.tapizadosmiura.util.ServletUtil.redirigirConAviso;

/**
 * CU07 - Gestor de catálogo del panel administrador.
 *
 * <p><b>GET /admin/productos</b> (parámetro {@code accion}):</p>
 * <ul>
 *   <li>{@code listar} (por defecto): tabla de productos, con búsqueda opcional {@code q}.</li>
 *   <li>{@code nuevo}: formulario vacío para crear un producto.</li>
 *   <li>{@code editar&id=N}: formulario con los datos del producto N.</li>
 * </ul>
 *
 * <p><b>POST /admin/productos</b> (parámetro {@code accion}):</p>
 * <ul>
 *   <li>{@code guardar}: crea (sin {@code idProducto}) o actualiza (con {@code idProducto}).</li>
 *   <li>{@code estado}: activa o desactiva un producto ({@code id}, {@code activo}).</li>
 * </ul>
 *
 * <p>Regla: un producto sin foto no puede quedar visible. Solo ocurre con los
 * productos creados por la API ({@code POST /api/admin/productos}), que nacen
 * ocultos y sin foto hasta que se les sube una aquí.</p>
 *
 * <p>Después de cada POST exitoso se redirige al listado (patrón
 * Post/Redirect/Get) para que recargar la página no repita la operación.</p>
 */
@WebServlet(name = "ProductoServlet", urlPatterns = {"/admin/productos"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // a disco desde 1 MB
        maxFileSize = 10L * 1024 * 1024,      // el límite real (5 MB) se valida con un mensaje claro
        maxRequestSize = 12L * 1024 * 1024)
public class ProductoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(ProductoServlet.class.getName());
    private static final String RUTA = "/admin/productos";
    private static final String VISTA_LISTA = "/WEB-INF/vistas/admin/productos.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/vistas/admin/producto-form.jsp";
    private static final String ERROR_BD = "Ocurrió un error con la base de datos. Intente de nuevo.";

    /** Campos de texto del formulario (atributo name de cada input). */
    private static final String[] CAMPOS_FORMULARIO = {
        "referencia", "nombre", "idCategoria", "descripcion", "precio", "stock",
        "estilo", "color", "materialTapiz", "materialEstructura", "tipoRelleno", "tipoPata",
        "altoCm", "anchoCm", "profundidadCm", "pesoKg", "resistenciaKgPuesto",
        "requiereArmado", "estado"
    };

    private final transient ProductoDAO productoDAO = new ProductoDAO();
    private final transient CategoriaDAO categoriaDAO = new CategoriaDAO();

    // =====================================================================
    // GET: consultas y formularios
    // =====================================================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = parametro(request, "accion");
        try {
            switch (accion) {
                case "nuevo" -> mostrarFormulario(request, response, new Producto(), new HashMap<>());
                case "editar" -> editar(request, response);
                default -> listar(request, response);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error al consultar productos", e);
            request.setAttribute("error", ERROR_BD);
            request.getRequestDispatcher(VISTA_LISTA).forward(request, response);
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String busqueda = parametro(request, "q");
        request.setAttribute("busqueda", busqueda);
        request.setAttribute("productos", productoDAO.listar(busqueda));
        request.getRequestDispatcher(VISTA_LISTA).forward(request, response);
    }

    private void editar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Producto producto = productoDAO.buscarPorId(enteroPositivo(parametro(request, "id")));
        if (producto == null) {
            redirigirConAviso(request, response, RUTA, "error", "El producto solicitado no existe.");
            return;
        }
        mostrarFormulario(request, response, producto, new HashMap<>());
    }

    // =====================================================================
    // POST: operaciones que modifican datos
    // =====================================================================

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (esMultipart(request)) {
            try {
                request.getParts(); // fuerza la lectura del formulario con archivos
            } catch (IllegalStateException e) {
                // El envío superó maxRequestSize: no se pueden leer los demás campos.
                redirigirConAviso(request, response, RUTA, "error", "La imagen es demasiado grande (máximo 5 MB).");
                return;
            }
        }

        String accion = parametro(request, "accion");
        try {
            switch (accion) {
                case "guardar" -> guardar(request, response);
                case "estado" -> cambiarEstado(request, response);
                default -> response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no válida");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error al guardar el producto", e);
            redirigirConAviso(request, response, RUTA, "error", ERROR_BD);
        }
    }

    /** Crea o actualiza un producto con su imagen principal. */
    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        ResultadoValidacion<Producto> resultado =
                ProductoValidador.validar(leerCampos(request, CAMPOS_FORMULARIO));
        Producto producto = resultado.getObjeto();
        Map<String, String> errores = new LinkedHashMap<>(resultado.getErrores());

        int idProducto = enteroPositivo(parametro(request, "idProducto"));
        boolean esNuevo = idProducto == 0;
        producto.setIdProducto(idProducto);

        // La imagen es obligatoria al crear; al editar solo si se quiere cambiar.
        Part imagen = request.getPart("imagen");
        boolean traeImagen = imagen != null && imagen.getSize() > 0;
        if (esNuevo || traeImagen) {
            String errorImagen = AlmacenImagenes.validar(imagen);
            if (errorImagen != null) {
                errores.put("imagen", errorImagen);
            }
        }

        if (!errores.containsKey("referencia")
                && productoDAO.existeReferencia(producto.getReferencia(), idProducto)) {
            errores.put("referencia", "Ya existe otro producto con esta referencia.");
        }

        // Un producto sin foto no puede quedar visible (los creados por la API nacen sin foto)
        if (!esNuevo && !traeImagen && producto.isEstado()) {
            Producto actual = productoDAO.buscarPorId(idProducto);
            if (actual != null && actual.getImagenPrincipal() == null) {
                errores.putIfAbsent("imagen", "Suba una foto para mostrar el producto en el catálogo.");
            }
        }

        if (!errores.isEmpty()) {
            if (!esNuevo) {
                // Conserva la foto actual en la vista previa del formulario
                Producto actual = productoDAO.buscarPorId(idProducto);
                producto.setImagenPrincipal(actual == null ? null : actual.getImagenPrincipal());
            }
            mostrarFormulario(request, response, producto, errores);
            return;
        }

        String rutaImagen = traeImagen
                ? AlmacenImagenes.guardar(getServletContext(), imagen, "productos", producto.getReferencia())
                : null;

        if (esNuevo) {
            productoDAO.crear(producto, rutaImagen);
            redirigirConAviso(request, response, RUTA, "exito",
                    "Producto \"" + producto.getNombre() + "\" registrado en el catálogo.");
        } else if (productoDAO.actualizar(producto, rutaImagen)) {
            redirigirConAviso(request, response, RUTA, "exito",
                    "Producto \"" + producto.getNombre() + "\" actualizado.");
        } else {
            redirigirConAviso(request, response, RUTA, "error", "El producto ya no existe.");
        }
    }

    /** Activa o desactiva un producto (borrado lógico). */
    private void cambiarEstado(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int id = enteroPositivo(parametro(request, "id"));
        boolean activar = "true".equals(parametro(request, "activo"));
        if (activar && id > 0) {
            Producto actual = productoDAO.buscarPorId(id);
            if (actual != null && actual.getImagenPrincipal() == null) {
                redirigirConAviso(request, response, RUTA, "error",
                        "\"" + actual.getNombre() + "\" no tiene foto. Edítelo y súbale una antes de mostrarlo.");
                return;
            }
        }
        if (id > 0 && productoDAO.cambiarEstado(id, activar)) {
            redirigirConAviso(request, response, RUTA, "exito",
                    activar ? "Producto visible en el catálogo." : "Producto oculto del catálogo.");
        } else {
            redirigirConAviso(request, response, RUTA, "error", "El producto solicitado no existe.");
        }
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   Producto producto, Map<String, String> errores)
            throws SQLException, ServletException, IOException {
        request.setAttribute("producto", producto);
        request.setAttribute("errores", errores);
        request.setAttribute("categorias", categoriaDAO.listarActivas());
        request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
    }
}
