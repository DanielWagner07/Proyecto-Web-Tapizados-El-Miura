package co.tapizadosmiura.modelo;

import java.math.BigDecimal;

/**
 * Producto incluido en una cotización. Tabla {@code detalle_cotizacion}.
 *
 * <p>{@code nombreProducto} y {@code precioUnitario} son una copia del momento
 * del envío: si el producto cambia de precio después, la cotización conserva
 * lo que el cliente vio.</p>
 *
 * <p>{@code stockDisponible} no se guarda en la tabla: solo se conoce al crear la
 * cotización y sirve para marcar los muebles "sujetos a fabricación".</p>
 */
public class DetalleCotizacion {

    private int idDetalleCotizacion;
    private int idCotizacion;
    private int idProducto;
    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioUnitario;

    /** Solo al crear la cotización: unidades en stock en ese momento. */
    private Integer stockDisponible;

    /** @return cantidad × precio unitario (en la BD es una columna generada) */
    public BigDecimal getSubtotal() {
        return precioUnitario == null ? BigDecimal.ZERO : precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    /** @return true si no había unidades en stock (null si no se conoce el stock) */
    public Boolean getAgotado() {
        return stockDisponible == null ? null : stockDisponible == 0;
    }

    /** @return true si la cantidad pedida supera el stock: el resto se fabrica (null si no se conoce) */
    public Boolean getSujetoAFabricacion() {
        return stockDisponible == null ? null : cantidad > stockDisponible;
    }

    public int getIdDetalleCotizacion() {
        return idDetalleCotizacion;
    }

    public void setIdDetalleCotizacion(int idDetalleCotizacion) {
        this.idDetalleCotizacion = idDetalleCotizacion;
    }

    public int getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(int idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Integer getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(Integer stockDisponible) {
        this.stockDisponible = stockDisponible;
    }
}
