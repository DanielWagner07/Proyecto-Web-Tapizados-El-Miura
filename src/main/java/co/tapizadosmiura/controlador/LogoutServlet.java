package co.tapizadosmiura.controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Cierra la sesión del administrador.
 *
 * <p>Solo atiende POST (botón "Cerrar sesión") para que un enlace externo no
 * pueda cerrar la sesión sin que el usuario lo pida.</p>
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/admin/logout"})
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/admin/login?salida=1");
    }

    /** Un GET a esta ruta solo regresa al panel. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin/productos");
    }
}
