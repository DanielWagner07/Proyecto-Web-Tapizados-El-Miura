package co.tapizadosmiura.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Cotización enviada por un cliente a WhatsApp (RF03 / RF04). Tabla {@code cotizacion}.
 *
 * <p>Se registra en el momento en que el cliente pulsa "Enviar por WhatsApp";
 * mientras la arma, vive en su navegador. El total no se guarda en la tabla:
 * se calcula a partir del detalle.</p>
 */
public class Cotizacion {

    /** Valores del ENUM {@code estado}, en el orden del flujo de atención. */
    public static final Set<String> ESTADOS = Set.of("pendiente", "atendida", "cerrada", "descartada");

    private int idCotizacion;
    private String estado;
    private String notaInterna;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    // Datos calculados (consultas con SUM/COUNT)
    private int totalItems;
    private int totalUnidades;
    private BigDecimal total = BigDecimal.ZERO;

    private List<DetalleCotizacion> detalles = new ArrayList<>();

    public int getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(int idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNotaInterna() {
        return notaInterna;
    }

    public void setNotaInterna(String notaInterna) {
        this.notaInterna = notaInterna;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalUnidades() {
        return totalUnidades;
    }

    public void setTotalUnidades(int totalUnidades) {
        this.totalUnidades = totalUnidades;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<DetalleCotizacion> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCotizacion> detalles) {
        this.detalles = detalles;
    }
}
