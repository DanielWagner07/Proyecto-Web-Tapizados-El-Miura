package co.tapizadosmiura.api.auth;

import co.tapizadosmiura.api.ApiExcepcion;
import co.tapizadosmiura.api.ApiServlet;
import co.tapizadosmiura.api.CamposJson;
import co.tapizadosmiura.api.dto.AdministradorDTO;
import co.tapizadosmiura.api.dto.SesionDTO;
import co.tapizadosmiura.controlador.LoginServlet;
import co.tapizadosmiura.dao.AdministradorDAO;
import co.tapizadosmiura.modelo.Administrador;
import co.tapizadosmiura.util.SeguridadUtil;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Autenticación del administrador por la API (RF06 / CU01, RNF02).
 *
 * <ul>
 *   <li><b>POST /api/auth/login</b> {@code {"correo": "...", "contrasena": "..."}}:
 *       valida con BCrypt e inicia la sesión. La cookie {@code JSESSIONID} que
 *       devuelve el servidor identifica la sesión en las siguientes peticiones.</li>
 *   <li><b>POST /api/auth/logout</b>: cierra la sesión.</li>
 *   <li><b>GET /api/auth/sesion</b>: indica si hay una sesión iniciada (siempre 200).</li>
 * </ul>
 *
 * <p>La sesión es la misma del panel: iniciar sesión aquí también da acceso a
 * {@code /admin/...} y cerrarla aquí también cierra el panel.</p>
 */
@WebServlet(name = "AutenticacionApiServlet", urlPatterns = {"/api/auth/*"})
public class AutenticacionApiServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;

    private final transient AdministradorDAO administradorDAO = new AdministradorDAO();

    @Override
    protected void get(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        if (!List.of("sesion").equals(segmentos(request))) {
            throw ApiExcepcion.noEncontrado("La ruta solicitada no existe.");
        }
        ok(response, sesionActual(request));
    }

    @Override
    protected void post(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        List<String> ruta = segmentos(request);
        if (List.of("login").equals(ruta)) {
            login(request, response);
        } else if (List.of("logout").equals(ruta)) {
            HttpSession sesion = request.getSession(false);
            if (sesion != null) {
                sesion.invalidate();
            }
            exito(response, HttpServletResponse.SC_OK, "Sesión cerrada.", new SesionDTO(false, null));
        } else {
            throw ApiExcepcion.noEncontrado("La ruta solicitada no existe.");
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        JsonObject cuerpo = leerJson(request);
        String correo = CamposJson.textoOpcional(cuerpo, "correo");
        String contrasena = contrasena(cuerpo);

        // CU01 flujo alterno: campos vacíos
        Map<String, String> errores = new LinkedHashMap<>();
        if (correo == null || correo.isEmpty()) {
            errores.put("correo", "Ingrese su correo.");
        }
        if (contrasena == null || contrasena.isEmpty()) {
            errores.put("contrasena", "Ingrese su contraseña.");
        }
        if (!errores.isEmpty()) {
            throw ApiExcepcion.solicitudInvalida("Ingrese su correo y su contraseña.", errores);
        }

        Administrador admin = administradorDAO.buscarActivoPorCorreo(correo);
        // Mismo mensaje si el correo no existe o la clave es incorrecta: no revela qué correos existen.
        if (admin == null || !SeguridadUtil.verificarContrasena(contrasena, admin.getContrasenaHash())) {
            throw ApiExcepcion.noAutorizado("Correo o contraseña incorrectos.");
        }

        HttpSession sesion = request.getSession();
        request.changeSessionId(); // evita la fijación de sesión
        sesion.setAttribute(LoginServlet.ATRIBUTO_SESION, admin.sinContrasena());
        administradorDAO.registrarAcceso(admin.getIdAdministrador());

        exito(response, HttpServletResponse.SC_OK, "Sesión iniciada. Bienvenido, " + admin.getNombre() + ".",
                new SesionDTO(true, AdministradorDTO.de(admin)));
    }

    /** La contraseña se lee tal cual, sin recortar espacios (podrían ser parte de ella). */
    private static String contrasena(JsonObject cuerpo) {
        JsonElement valor = cuerpo.get("contrasena");
        boolean esTexto = valor != null && valor.isJsonPrimitive() && valor.getAsJsonPrimitive().isString();
        return esTexto ? valor.getAsString() : null;
    }

    private static SesionDTO sesionActual(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        Object admin = sesion == null ? null : sesion.getAttribute(LoginServlet.ATRIBUTO_SESION);
        if (admin instanceof Administrador a) {
            return new SesionDTO(true, AdministradorDTO.de(a));
        }
        return new SesionDTO(false, null);
    }
}
