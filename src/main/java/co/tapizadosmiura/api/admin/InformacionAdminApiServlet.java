package co.tapizadosmiura.api.admin;

import co.tapizadosmiura.api.ApiExcepcion;
import co.tapizadosmiura.api.ApiServlet;
import co.tapizadosmiura.api.publico.InformacionApiServlet;
import co.tapizadosmiura.dao.InformacionNegocioDAO;
import co.tapizadosmiura.modelo.InformacionNegocio;
import co.tapizadosmiura.util.InformacionValidador;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestión de la información del negocio por la API (CU09 / RF09). Requiere sesión.
 *
 * <ul>
 *   <li><b>GET /api/admin/informacion</b>: todos los datos, igual que el servicio público.</li>
 *   <li><b>PUT /api/admin/informacion</b>: actualiza solo los datos enviados, por ejemplo
 *       {@code {"whatsapp": "573120000000", "horario": "Lunes a viernes..."}}.</li>
 * </ul>
 *
 * <p>Usa las mismas reglas del panel ({@link InformacionValidador}): ningún dato vacío,
 * máximo 2000 caracteres, WhatsApp solo con dígitos, correo válido y redes con https://.
 * Si un dato tiene error no se guarda ninguno.</p>
 */
@WebServlet(name = "InformacionAdminApiServlet", urlPatterns = {"/api/admin/informacion"})
public class InformacionAdminApiServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;

    private final transient InformacionNegocioDAO informacionDAO = new InformacionNegocioDAO();

    @Override
    protected void get(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        ok(response, InformacionApiServlet.porClave(informacionDAO.listar()));
    }

    @Override
    protected void put(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        JsonObject cuerpo = leerJson(request);
        if (cuerpo.isEmpty()) {
            throw ApiExcepcion.solicitudInvalida("Envíe al menos un dato para actualizar, por ejemplo {\"horario\": \"...\"}.");
        }
        Map<String, InformacionNegocio> existentes = new LinkedHashMap<>();
        for (InformacionNegocio dato : informacionDAO.listar()) {
            existentes.put(dato.getClave(), dato);
        }

        Map<String, String> nuevosValores = new LinkedHashMap<>();
        Map<String, String> errores = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entrada : cuerpo.entrySet()) {
            String clave = entrada.getKey();
            InformacionNegocio dato = existentes.get(clave);
            JsonElement valor = entrada.getValue();
            if (dato == null) {
                errores.put(clave, "No existe un dato con esta clave.");
            } else if (!valor.isJsonPrimitive() || !valor.getAsJsonPrimitive().isString()) {
                errores.put(clave, "Debe ser un texto.");
            } else {
                String normalizado = InformacionValidador.normalizar(clave, valor.getAsString());
                String error = InformacionValidador.validar(clave, dato.getGrupo(), normalizado);
                if (error != null) {
                    errores.put(clave, error);
                }
                nuevosValores.put(clave, normalizado);
            }
        }
        if (!errores.isEmpty()) {
            throw ApiExcepcion.solicitudInvalida("Hay datos con errores; no se guardó ningún cambio.", errores);
        }

        informacionDAO.actualizarValores(nuevosValores);
        List<InformacionNegocio> actualizados = informacionDAO.listar();
        exito(response, HttpServletResponse.SC_OK,
                "Información del negocio actualizada (" + nuevosValores.size() + " dato(s)).",
                InformacionApiServlet.porClave(actualizados));
    }
}
