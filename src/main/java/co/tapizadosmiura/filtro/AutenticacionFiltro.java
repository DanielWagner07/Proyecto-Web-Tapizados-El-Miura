package co.tapizadosmiura.filtro;

import co.tapizadosmiura.controlador.LoginServlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Protege todas las rutas {@code /admin/*}: si no hay un administrador en la
 * sesión, redirige al inicio de sesión (RF06 - acceso restringido al panel).
 *
 * <p>Quedan libres el formulario de login y la hoja de estilos del panel.</p>
 */
@WebFilter(filterName = "AutenticacionFiltro", urlPatterns = {"/admin/*"})
public class AutenticacionFiltro extends HttpFilter {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());

        boolean rutaPublica = ruta.equals("/admin/login") || ruta.startsWith("/admin/css/");
        if (rutaPublica) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession sesion = request.getSession(false);
        boolean autenticado = sesion != null && sesion.getAttribute(LoginServlet.ATRIBUTO_SESION) != null;
        if (!autenticado) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        // Las páginas del panel no se guardan en caché: tras cerrar sesión,
        // el botón "Atrás" del navegador no debe mostrar datos.
        response.setHeader("Cache-Control", "no-store");
        chain.doFilter(request, response);
    }
}
