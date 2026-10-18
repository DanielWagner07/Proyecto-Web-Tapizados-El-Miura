package co.tapizadosmiura.api;

import co.tapizadosmiura.api.dto.ErrorDTO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Responde 404 en JSON a cualquier ruta {@code /api/...} que no corresponda a
 * un servicio. Sin este servlet, Tomcat respondería con su página de error en
 * HTML, que el front-end no puede interpretar.
 *
 * <p>Los servicios reales tienen rutas más específicas ({@code /api/productos/*}),
 * que Tomcat elige antes que esta.</p>
 */
@WebServlet(name = "ApiRespaldoServlet", urlPatterns = {"/api/*"})
public class ApiRespaldoServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        responder(response, HttpServletResponse.SC_NOT_FOUND, new ErrorDTO(
                "No existe el servicio " + request.getMethod() + " " + ruta + ". Consulte docs/API.md.", null));
    }
}
