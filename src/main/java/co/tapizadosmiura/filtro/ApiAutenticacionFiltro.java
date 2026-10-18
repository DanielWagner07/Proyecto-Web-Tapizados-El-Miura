package co.tapizadosmiura.filtro;

import co.tapizadosmiura.api.Json;
import co.tapizadosmiura.api.dto.ErrorDTO;
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
 * Protege los servicios de administración {@code /api/admin/*} (RF06).
 *
 * <p>Usa la misma sesión que el panel: quien inició sesión en
 * {@code /admin/login} o en {@code POST /api/auth/login} puede usarlos.</p>
 *
 * <p>A diferencia de {@link AutenticacionFiltro}, no redirige al formulario de
 * ingreso: responde <b>401</b> en JSON, que es lo que entiende un cliente de la
 * API (el front-end o Postman).</p>
 */
@WebFilter(filterName = "ApiAutenticacionFiltro", urlPatterns = {"/api/admin/*"})
public class ApiAutenticacionFiltro extends HttpFilter {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpSession sesion = request.getSession(false);
        boolean autenticado = sesion != null && sesion.getAttribute(LoginServlet.ATRIBUTO_SESION) != null;
        if (!autenticado) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-store");
            Json.GSON.toJson(new ErrorDTO(
                    "Debe iniciar sesión como administrador (POST /api/auth/login).", null), response.getWriter());
            return;
        }
        chain.doFilter(request, response);
    }
}
