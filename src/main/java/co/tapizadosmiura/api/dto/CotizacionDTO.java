package co.tapizadosmiura.api.dto;

import co.tapizadosmiura.modelo.Cotizacion;
import co.tapizadosmiura.modelo.DetalleCotizacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cotización tal como la entrega la API.
 *
 * <ul>
 *   <li>Al crearla ({@code POST /api/cotizaciones}) incluye {@code mensaje} y
 *       {@code enlaceWhatsapp}, y cada producto indica si está agotado.</li>
 *   <li>En el administrador incluye estado, nota interna y fechas.</li>
 *   <li>En los listados del administrador no incluye {@code items}.</li>
 * </ul>
 */
public record CotizacionDTO(int idCotizacion, String estado, String notaInterna, LocalDateTime creadoEn,
                            LocalDateTime actualizadoEn, int totalItems, int totalUnidades, BigDecimal total,
                            List<ItemCotizacionDTO> items, String mensaje, String enlaceWhatsapp) {

    /** Respuesta al cliente que acaba de enviar su cotización. */
    public static CotizacionDTO creada(int idCotizacion, List<DetalleCotizacion> detalles, String mensaje,
                                       String enlaceWhatsapp) {
        int unidades = detalles.stream().mapToInt(DetalleCotizacion::getCantidad).sum();
        BigDecimal total = detalles.stream().map(DetalleCotizacion::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CotizacionDTO(idCotizacion, "pendiente", null, null, null, detalles.size(), unidades, total,
                items(detalles), mensaje, enlaceWhatsapp);
    }

    /** Fila del listado del administrador (sin productos). */
    public static CotizacionDTO resumen(Cotizacion c) {
        return new CotizacionDTO(c.getIdCotizacion(), c.getEstado(), c.getNotaInterna(), c.getCreadoEn(),
                c.getActualizadoEn(), c.getTotalItems(), c.getTotalUnidades(), c.getTotal(), null, null, null);
    }

    /** Cotización completa para el administrador. */
    public static CotizacionDTO detalle(Cotizacion c) {
        return new CotizacionDTO(c.getIdCotizacion(), c.getEstado(), c.getNotaInterna(), c.getCreadoEn(),
                c.getActualizadoEn(), c.getTotalItems(), c.getTotalUnidades(), c.getTotal(),
                items(c.getDetalles()), null, null);
    }

    private static List<ItemCotizacionDTO> items(List<DetalleCotizacion> detalles) {
        return detalles.stream().map(ItemCotizacionDTO::de).toList();
    }
}
