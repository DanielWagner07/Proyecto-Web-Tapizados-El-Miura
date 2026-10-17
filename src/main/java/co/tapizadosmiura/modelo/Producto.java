package co.tapizadosmiura.modelo;

import java.math.BigDecimal;

/**
 * Mueble del catálogo. Tabla {@code producto}.
 *
 * <p>Además de las columnas de la tabla incluye dos datos de solo lectura que
 * vienen de otras tablas al consultar: {@code nombreCategoria} e
 * {@code imagenPrincipal} (la foto de menor orden en {@code producto_imagen}).</p>
 *
 * <p>Las medidas y la ficha técnica son opcionales, por eso usan tipos objeto
 * ({@link BigDecimal}, {@link Integer}) que admiten {@code null}.</p>
 */
public class Producto {

    private int idProducto;
    private int idCategoria;
    private String referencia;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private int stock;

    // Ficha técnica
    private String estilo;
    private String color;
    private String materialTapiz;
    private String materialEstructura;
    private String tipoRelleno;
    private String tipoPata;
    private BigDecimal altoCm;
    private BigDecimal anchoCm;
    private BigDecimal profundidadCm;
    private BigDecimal pesoKg;
    private Integer resistenciaKgPuesto;
    private boolean requiereArmado;

    /** true = visible en el sitio público. */
    private boolean estado = true;

    // Datos de solo lectura (consultas con JOIN)
    private String nombreCategoria;
    private String imagenPrincipal;

    /** @return true si no quedan unidades disponibles */
    public boolean isAgotado() {
        return stock == 0;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getEstilo() {
        return estilo;
    }

    public void setEstilo(String estilo) {
        this.estilo = estilo;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMaterialTapiz() {
        return materialTapiz;
    }

    public void setMaterialTapiz(String materialTapiz) {
        this.materialTapiz = materialTapiz;
    }

    public String getMaterialEstructura() {
        return materialEstructura;
    }

    public void setMaterialEstructura(String materialEstructura) {
        this.materialEstructura = materialEstructura;
    }

    public String getTipoRelleno() {
        return tipoRelleno;
    }

    public void setTipoRelleno(String tipoRelleno) {
        this.tipoRelleno = tipoRelleno;
    }

    public String getTipoPata() {
        return tipoPata;
    }

    public void setTipoPata(String tipoPata) {
        this.tipoPata = tipoPata;
    }

    public BigDecimal getAltoCm() {
        return altoCm;
    }

    public void setAltoCm(BigDecimal altoCm) {
        this.altoCm = altoCm;
    }

    public BigDecimal getAnchoCm() {
        return anchoCm;
    }

    public void setAnchoCm(BigDecimal anchoCm) {
        this.anchoCm = anchoCm;
    }

    public BigDecimal getProfundidadCm() {
        return profundidadCm;
    }

    public void setProfundidadCm(BigDecimal profundidadCm) {
        this.profundidadCm = profundidadCm;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public Integer getResistenciaKgPuesto() {
        return resistenciaKgPuesto;
    }

    public void setResistenciaKgPuesto(Integer resistenciaKgPuesto) {
        this.resistenciaKgPuesto = resistenciaKgPuesto;
    }

    public boolean isRequiereArmado() {
        return requiereArmado;
    }

    public void setRequiereArmado(boolean requiereArmado) {
        this.requiereArmado = requiereArmado;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getImagenPrincipal() {
        return imagenPrincipal;
    }

    public void setImagenPrincipal(String imagenPrincipal) {
        this.imagenPrincipal = imagenPrincipal;
    }
}
