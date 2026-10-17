package co.tapizadosmiura.controlador;

import co.tapizadosmiura.dao.AdministradorDAO;
import co.tapizadosmiura.modelo.Administrador;
import co.tapizadosmiura.util.SeguridadUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CU01 - Iniciar sesión en el panel administrador.
 *
 * <ul>
 *   <li><b>GET /admin/login</b>: muestra el formulario (o entra directo si ya hay sesión).</li>
 *   <li><b>POST /admin/login</b>: valida correo y contraseña contra la tabla {@code administrador}.</li>
 * </ul>
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/admin/login"})
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Nombre del atributo de sesión que identifica al administrador autenticado. */
    public static final String ATRIBUTO_SESION = "adminSesion";

    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());
    private static final String VISTA = "/WEB-INF/vistas/admin/login.jsp";

    private final transient AdministradorDAO administradorDAO = new AdministradorDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion != null && sesion.getAttribute(ATRIBUTO_SESION) != null) {
            response.sendRedirect(request.getContextPath() + "/admin/productos");
            return;
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String correo = valorLimpio(request.getParameter("correo"));
        String contrasena = request.getParameter("contrasena");

        // CU01 flujo alterno: campos vacíos
        if (correo.isEmpty() || contrasena == null || contrasena.isEmpty()) {
            mostrarError(request, response, correo, "Ingrese su correo y su contraseña.");
            return;
        }

        try {
            Administrador admin = administradorDAO.buscarActivoPorCorreo(correo);
            // Mismo mensaje si el correo no existe o la clave es incorrecta:
            // así no se revela qué correos están registrados.
            if (admin == null || !SeguridadUtil.verificarContrasena(contrasena, admin.getContrasenaHash())) {
                mostrarError(request, response, correo, "Correo o contraseña incorrectos.");
                return;
            }

            HttpSession sesion = request.getSession();
            request.changeSessionId(); // evita la fijación de sesión
            sesion.setAttribute(ATRIBUTO_SESION, admin.sinContrasena());
            administradorDAO.registrarAcceso(admin.getIdAdministrador());

            response.sendRedirect(request.getContextPath() + "/admin/productos");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos al iniciar sesión", e);
            mostrarError(request, response, correo,
                    "No fue posible conectar con la base de datos. Intente de nuevo en unos minutos.");
        }
    }

    private void mostrarError(HttpServletRequest request, HttpServletResponse response,
                              String correo, String mensaje) throws ServletException, IOException {
        request.setAttribute("error", mensaje);
        request.setAttribute("correo", correo);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    private static String valorLimpio(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
