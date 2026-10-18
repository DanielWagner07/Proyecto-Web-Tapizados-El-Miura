package co.tapizadosmiura.api.publico;

import co.tapizadosmiura.api.ApiExcepcion;
import co.tapizadosmiura.api.ApiServlet;
import co.tapizadosmiura.api.dto.CotizacionDTO;
import co.tapizadosmiura.dao.CotizacionDAO;
import co.tapizadosmiura.dao.InformacionNegocioDAO;
import co.tapizadosmiura.dao.ProductoDAO;
import co.tapizadosmiura.modelo.DetalleCotizacion;
import co.tapizadosmiura.modelo.Producto;
import co.tapizadosmiura.util.CotizacionValidador;
import co.tapizadosmiura.util.MensajeWhatsApp;
import co.tapizadosmiura.util.ResultadoValidacion;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <b>POST /api/cotizaciones</b>: registra la cotización que el cliente armó en
 * su navegador y devuelve el enlace de WhatsApp para enviarla (RF03 / RF04,
 * CU04 / CU05).
 *
 * <p>Pasos:</p>
 * <ol>
 *   <li>Valida la lista de productos y cantidades ({@link CotizacionValidador}).</li>
 *   <li>Busca cada producto en la BD: si alguno no existe o está oculto, rechaza
 *       la cotización completa (400) indicando cuál, para que el carrito lo retire.</li>
 *   <li>Toma nombre y precio <b>de la BD</b>; el navegador nunca define el precio.</li>
 *   <li>Guarda la cotización y su detalle en una sola transacción.</li>
 *   <li>Arma el mensaje y el enlace {@code wa.me} con el WhatsApp del negocio.</li>
 * </ol>
 *
 * <p>Cotizar no descuenta stock. Los muebles agotados, o pedidos en mayor
 * cantidad que el stock, se aceptan marcados como "sujeto a fabricación".</p>
 */
@WebServlet(name = "CotizacionApiServlet", urlPatterns = {"/api/cotizaciones"})
public class CotizacionApiServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(CotizacionApiServlet.class.getName());

    private final transient ProductoDAO productoDAO = new ProductoDAO();
    private final transient CotizacionDAO cotizacionDAO = new CotizacionDAO();
    private final transient InformacionNegocioDAO informacionDAO = new InformacionNegocioDAO();

    @Override
    protected void post(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        ResultadoValidacion<Map<Integer, Integer>> resultado = CotizacionValidador.validar(leerJson(request));
        if (!resultado.esValido()) {
            throw ApiExcepcion.solicitudInvalida("La cotización tiene errores.", resultado.getErrores());
        }
        Map<Integer, Integer> cantidades = resultado.getObjeto();

        // El número del negocio se revisa antes de guardar: sin él no hay a dónde enviar.
        String whatsapp = informacionDAO.buscarValor("whatsapp");
        if (!MensajeWhatsApp.numeroValido(whatsapp)) {
            LOG.log(Level.SEVERE, "El WhatsApp del negocio no está configurado o es inválido: {0}", whatsapp);
            throw new ApiExcepcion(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "El negocio no tiene un número de WhatsApp configurado. Intente más tarde.", null);
        }

        List<DetalleCotizacion> detalles = armarDetalles(cantidades);
        int idCotizacion = cotizacionDAO.crear(detalles);

        BigDecimal total = detalles.stream().map(DetalleCotizacion::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String mensaje = MensajeWhatsApp.texto(idCotizacion, detalles, total);
        String enlace = MensajeWhatsApp.enlace(whatsapp, mensaje);

        exito(response, HttpServletResponse.SC_CREATED,
                "Cotización N.º " + idCotizacion + " registrada. Abra el enlace para enviarla por WhatsApp.",
                CotizacionDTO.creada(idCotizacion, detalles, mensaje, enlace));
    }

    /**
     * Cruza las cantidades pedidas con los productos reales de la BD.
     *
     * @throws ApiExcepcion 400 si algún producto no existe o no está disponible
     */
    private List<DetalleCotizacion> armarDetalles(Map<Integer, Integer> cantidades) throws SQLException {
        Map<Integer, Producto> productos = productoDAO.buscarPublicosPorIds(cantidades.keySet());
        Map<String, String> errores = new LinkedHashMap<>();
        List<DetalleCotizacion> detalles = new ArrayList<>();
        for (Map.Entry<Integer, Integer> pedido : cantidades.entrySet()) {
            Producto producto = productos.get(pedido.getKey());
            if (producto == null) {
                errores.put("producto " + pedido.getKey(), "El producto no existe o ya no está disponible.");
                continue;
            }
            DetalleCotizacion detalle = new DetalleCotizacion();
            detalle.setIdProducto(producto.getIdProducto());
            detalle.setNombreProducto(producto.getNombre());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setCantidad(pedido.getValue());
            detalle.setStockDisponible(producto.getStock());
            detalles.add(detalle);
        }
        if (!errores.isEmpty()) {
            throw ApiExcepcion.solicitudInvalida(
                    "Algunos productos de la cotización ya no están disponibles. Retírelos e intente de nuevo.",
                    errores);
        }
        return detalles;
    }
}
