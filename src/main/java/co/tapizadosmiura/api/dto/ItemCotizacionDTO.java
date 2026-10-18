package co.tapizadosmiura.api.dto;

import co.tapizadosmiura.modelo.DetalleCotizacion;

import java.math.BigDecimal;

/**
 * Producto dentro de una cotización.
 *
 * @param agotado            solo al crear: true si no había unidades en stock
 * @param sujetoAFabricacion solo al crear: true si la cantidad supera el stock (el resto se fabrica)
 */
public record ItemCotizacionDTO(int idProducto, String nombre, int cantidad, BigDecimal precioUnitario,
                                BigDecimal subtotal, Boolean agotado, Boolean sujetoAFabricacion) {

    public static ItemCotizacionDTO de(DetalleCotizacion d) {
        return new ItemCotizacionDTO(d.getIdProducto(), d.getNombreProducto(), d.getCantidad(),
                d.getPrecioUnitario(), d.getSubtotal(), d.getAgotado(), d.getSujetoAFabricacion());
    }
}
