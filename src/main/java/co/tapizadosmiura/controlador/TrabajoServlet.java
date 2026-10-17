package co.tapizadosmiura.controlador;

import co.tapizadosmiura.dao.ServicioDAO;
import co.tapizadosmiura.dao.TrabajoDAO;
import co.tapizadosmiura.modelo.Trabajo;
import co.tapizadosmiura.util.AlmacenImagenes;
import co.tapizadosmiura.util.ResultadoValidacion;
import co.tapizadosmiura.util.TrabajoValidador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static co.tapizadosmiura.util.ServletUtil.enteroPositivo;
import static co.tapizadosmiura.util.ServletUtil.esMultipart;
import static co.tapizadosmiura.util.ServletUtil.leerCampos;
import static co.tapizadosmiura.util.ServletUtil.parametro;
import static co.tapizadosmiura.util.ServletUtil.redirigirConAviso;

/**
 * CU08 - Galería de trabajos realizados (restauraciones y colaboraciones).
 *
 * <p><b>GET /admin/trabajos</b> (parámetro {@code accion}):</p>
 * <ul>
 *   <li>{@code listar} (por defecto): trabajos, con filtro opcional {@code tipo}.</li>
 *   <li>{@code nuevo}: formulario vacío ({@code tipo} opcional para preseleccionarlo).</li>
 *   <li>{@code editar&id=N}: formulario con los datos y fotos del trabajo N.</li>
 * </ul>
 *
 * <p><b>POST /admin/trabajos</b> (parámetro {@code accion}):</p>
 * <ul>
 *   <li>{@code guardar}: crea o actualiza; recibe fotos de antes/después y fotos adicionales.</li>
 *   <li>{@code estado}: muestra u oculta un trabajo en la galería pública.</li>
 *   <li>{@code eliminar}: borra el trabajo, sus fotos en la BD y los archivos.</li>
 *   <li>{@code eliminarFoto}: quita una foto adicional.</li>
 * </ul>
 */
@WebServlet(name = "TrabajoServlet", urlPatterns = {"/admin/trabajos"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 10L * 1024 * 1024,      // el límite real (5 MB por foto) se valida con un mensaje claro
        maxRequestSize = 60L * 1024 * 1024)   // antes + después + hasta 8 fotos adicionales
public class TrabajoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(TrabajoServlet.class.getName());
    private static final String RUTA = "/admin/trabajos";
    private static final String VISTA_LISTA = "/WEB-INF/vistas/admin/trabajos.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/vistas/admin/trabajo-form.jsp";
    private static final String ERROR_BD = "Ocurrió un error con la base de datos. Intente de nuevo.";
    private static final String CARPETA = "trabajos";

    private final transient TrabajoDAO trabajoDAO = new TrabajoDAO();
    private final transient ServicioDAO servicioDAO = new ServicioDAO();

    // =====================================================================
    // GET: consultas y formularios
    // =====================================================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            switch (parametro(request, "accion")) {
                case "nuevo" -> nuevo(request, response);
                case "editar" -> editar(request, response);
                default -> listar(request, response);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error al consultar trabajos", e);
            request.setAttribute("error", ERROR_BD);
            request.getRequestDispatcher(VISTA_LISTA).forward(request, response);
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String tipo = parametro(request, "tipo");
        if (!Trabajo.RESTAURACION.equals(tipo) && !Trabajo.COLABORACION.equals(tipo)) {
            tipo = "";
        }
        request.setAttribute("tipo", tipo);
        request.setAttribute("trabajos", trabajoDAO.listar(tipo));
        request.getRequestDispatcher(VISTA_LISTA).forward(request, response);
    }

    private void nuevo(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Trabajo trabajo = new Trabajo();
        String tipo = parametro(request, "tipo");
        trabajo.setTipo(Trabajo.COLABORACION.equals(tipo) ? Trabajo.COLABORACION : Trabajo.RESTAURACION);
        mostrarFormulario(request, response, trabajo, new HashMap<>());
    }

    private void editar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Trabajo trabajo = trabajoDAO.buscarPorId(enteroPositivo(parametro(request, "id")));
        if (trabajo == null) {
            redirigirConAviso(request, response, RUTA, "error", "El trabajo solicitado no existe.");
            return;
        }
        mostrarFormulario(request, response, trabajo, new HashMap<>());
    }

    // =====================================================================
    // POST: operaciones que modifican datos
    // =====================================================================

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (esMultipart(request)) {
            try {
                request.getParts();
            } catch (IllegalStateException e) {
                redirigirConAviso(request, response, RUTA, "error",
                        "Las fotos enviadas superan el tamaño permitido (máximo 5 MB cada una).");
                return;
            }
        }

        try {
            switch (parametro(request, "accion")) {
                case "guardar" -> guardar(request, response);
                case "estado" -> cambiarEstado(request, response);
                case "eliminar" -> eliminar(request, response);
                case "eliminarFoto" -> eliminarFoto(request, response);
                default -> response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no válida");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error al guardar el trabajo", e);
            redirigirConAviso(request, response, RUTA, "error", ERROR_BD);
        }
    }

    /** Crea o actualiza un trabajo con sus fotos. */
    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        ResultadoValidacion<Trabajo> resultado = TrabajoValidador.validar(
                leerCampos(request, "tipo", "titulo", "descripcion", "cliente", "idServicio", "estado"));
        Trabajo trabajo = resultado.getObjeto();
        Map<String, String> errores = new LinkedHashMap<>(resultado.getErrores());

        int idTrabajo = enteroPositivo(parametro(request, "idTrabajo"));
        Trabajo actual = idTrabajo == 0 ? null : trabajoDAO.buscarPorId(idTrabajo);
        if (idTrabajo != 0 && actual == null) {
            redirigirConAviso(request, response, RUTA, "error", "El trabajo ya no existe.");
            return;
        }
        trabajo.setIdTrabajo(idTrabajo);

        // --- Fotos enviadas ---
        Part antes = request.getPart("imagenAntes");
        Part despues = request.getPart("imagenDespues");
        boolean traeAntes = tieneArchivo(antes);
        boolean traeDespues = tieneArchivo(despues);
        validarImagen(antes, traeAntes, "imagenAntes", errores);
        validarImagen(despues, traeDespues, "imagenDespues", errores);

        List<Part> fotosNuevas = new ArrayList<>();
        for (Part parte : request.getParts()) {
            if ("fotos".equals(parte.getName()) && tieneArchivo(parte)) {
                fotosNuevas.add(parte);
                validarImagen(parte, true, "fotos", errores);
            }
        }
        int fotosActuales = actual == null ? 0 : actual.getFotos().size();
        TrabajoValidador.validarCantidadFotos(fotosActuales, fotosNuevas.size(), errores);

        // Regla RF08: la restauración necesita antes y después (nuevas o ya guardadas)
        boolean tieneAntes = traeAntes || (actual != null && actual.getImagenAntes() != null);
        boolean tieneDespues = traeDespues || (actual != null && actual.getImagenDespues() != null);
        TrabajoValidador.validarAntesDespues(trabajo.getTipo(), tieneAntes, tieneDespues, errores);

        if (!errores.isEmpty()) {
            if (actual != null) { // conserva las fotos actuales en la vista previa
                trabajo.setImagenAntes(actual.getImagenAntes());
                trabajo.setImagenDespues(actual.getImagenDespues());
                trabajo.setFotos(actual.getFotos());
            }
            mostrarFormulario(request, response, trabajo, errores);
            return;
        }

        // --- Guardar archivos y luego la BD ---
        List<String> archivosNuevos = new ArrayList<>();
        String rutaAntes = traeAntes ? guardarArchivo(antes, "antes", archivosNuevos)
                : (actual == null ? null : actual.getImagenAntes());
        String rutaDespues = traeDespues ? guardarArchivo(despues, "despues", archivosNuevos)
                : (actual == null ? null : actual.getImagenDespues());
        List<String> rutasFotos = new ArrayList<>();
        for (Part foto : fotosNuevas) {
            rutasFotos.add(guardarArchivo(foto, "foto", archivosNuevos));
        }
        trabajo.setImagenAntes(rutaAntes);
        trabajo.setImagenDespues(rutaDespues);

        try {
            if (actual == null) {
                trabajoDAO.crear(trabajo, rutasFotos);
            } else {
                trabajoDAO.actualizar(trabajo, rutasFotos);
                // Las fotos reemplazadas ya no se usan
                if (traeAntes) {
                    AlmacenImagenes.eliminar(getServletContext(), actual.getImagenAntes());
                }
                if (traeDespues) {
                    AlmacenImagenes.eliminar(getServletContext(), actual.getImagenDespues());
                }
            }
        } catch (SQLException e) {
            // Si la BD falla, se borran los archivos recién subidos para no dejar basura
            archivosNuevos.forEach(ruta -> AlmacenImagenes.eliminar(getServletContext(), ruta));
            throw e;
        }

        String texto = (actual == null ? "Trabajo \"" + trabajo.getTitulo() + "\" publicado en la galería."
                : "Trabajo \"" + trabajo.getTitulo() + "\" actualizado.");
        redirigirConAviso(request, response, RUTA, "exito", texto);
    }

    private void cambiarEstado(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int id = enteroPositivo(parametro(request, "id"));
        boolean mostrar = "true".equals(parametro(request, "activo"));
        if (id > 0 && trabajoDAO.cambiarEstado(id, mostrar)) {
            redirigirConAviso(request, response, RUTA, "exito",
                    mostrar ? "Trabajo visible en la galería." : "Trabajo oculto de la galería.");
        } else {
            redirigirConAviso(request, response, RUTA, "error", "El trabajo solicitado no existe.");
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int id = enteroPositivo(parametro(request, "id"));
        List<String> rutas = id > 0 ? trabajoDAO.eliminar(id) : null;
        if (rutas == null) {
            redirigirConAviso(request, response, RUTA, "error", "El trabajo solicitado no existe.");
            return;
        }
        rutas.forEach(ruta -> AlmacenImagenes.eliminar(getServletContext(), ruta));
        redirigirConAviso(request, response, RUTA, "exito", "Trabajo eliminado de la galería.");
    }

    private void eliminarFoto(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int idTrabajo = enteroPositivo(parametro(request, "id"));
        int idFoto = enteroPositivo(parametro(request, "idFoto"));
        String ruta = trabajoDAO.eliminarFoto(idTrabajo, idFoto);
        String volver = RUTA + "?accion=editar&id=" + idTrabajo;
        if (ruta == null) {
            redirigirConAviso(request, response, volver, "error", "La foto ya no existe.");
            return;
        }
        AlmacenImagenes.eliminar(getServletContext(), ruta);
        redirigirConAviso(request, response, volver, "exito", "Foto eliminada.");
    }

    // =====================================================================
    // Apoyo
    // =====================================================================

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   Trabajo trabajo, Map<String, String> errores)
            throws SQLException, ServletException, IOException {
        request.setAttribute("trabajo", trabajo);
        request.setAttribute("errores", errores);
        request.setAttribute("servicios", servicioDAO.listarActivos());
        request.setAttribute("maximoFotos", TrabajoValidador.MAXIMO_FOTOS_ADICIONALES);
        request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
    }

    private static boolean tieneArchivo(Part parte) {
        return parte != null && parte.getSize() > 0;
    }

    /** Valida una foto enviada y registra el primer error del campo. */
    private static void validarImagen(Part parte, boolean enviada, String campo, Map<String, String> errores) {
        if (!enviada) {
            return;
        }
        String error = AlmacenImagenes.validar(parte);
        if (error != null) {
            errores.putIfAbsent(campo, error);
        }
    }

    /** Guarda la foto y anota su ruta por si hay que deshacer. */
    private String guardarArchivo(Part parte, String prefijo, List<String> archivosNuevos) throws IOException {
        String ruta = AlmacenImagenes.guardar(getServletContext(), parte, CARPETA, prefijo);
        archivosNuevos.add(ruta);
        return ruta;
    }
}
