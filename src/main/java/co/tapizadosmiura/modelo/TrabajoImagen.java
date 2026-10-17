package co.tapizadosmiura.modelo;

/**
 * Foto adicional de un trabajo del portafolio. Tabla {@code trabajo_imagen}.
 */
public class TrabajoImagen {

    private int idTrabajoImagen;
    private int idTrabajo;
    private String ruta;
    private String textoAlternativo;
    private int orden;

    public int getIdTrabajoImagen() {
        return idTrabajoImagen;
    }

    public void setIdTrabajoImagen(int idTrabajoImagen) {
        this.idTrabajoImagen = idTrabajoImagen;
    }

    public int getIdTrabajo() {
        return idTrabajo;
    }

    public void setIdTrabajo(int idTrabajo) {
        this.idTrabajo = idTrabajo;
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
