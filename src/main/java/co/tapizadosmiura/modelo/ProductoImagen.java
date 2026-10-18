package co.tapizadosmiura.modelo;

/**
 * Foto de un producto. Tabla {@code producto_imagen}.
 * La de menor {@code orden} es la imagen principal; el resto forman el carrusel del detalle.
 */
public class ProductoImagen {

    private int idProductoImagen;
    private int idProducto;
    private String ruta;
    private String textoAlternativo;
    private int orden;

    public int getIdProductoImagen() {
        return idProductoImagen;
    }

    public void setIdProductoImagen(int idProductoImagen) {
        this.idProductoImagen = idProductoImagen;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getTextoAlternativo() {
        return textoAlternativo;
    }

    public void setTextoAlternativo(String textoAlternativo) {
        this.textoAlternativo = textoAlternativo;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }
}
