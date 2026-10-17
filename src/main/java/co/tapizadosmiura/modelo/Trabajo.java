package co.tapizadosmiura.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Trabajo del portafolio (CU08). Tabla {@code trabajo}.
 *
 * <p>Hay dos tipos que comparten tabla:</p>
 * <ul>
 *   <li>{@value #RESTAURACION}: comparativo antes/después; ambas fotos son obligatorias.</li>
 *   <li>{@value #COLABORACION}: proyecto para otro negocio, con varias fotos en
 *       {@code trabajo_imagen}; las fotos de antes/después son opcionales.</li>
 * </ul>
 */
public class Trabajo {

    public static final String RESTAURACION = "restauracion";
    public static final String COLABORACION = "colaboracion";

    private int idTrabajo;
    private Integer idServicio;
    private String tipo;
    private String titulo;
    private String descripcion;
    private String cliente;
    private String imagenAntes;
    private String imagenDespues;
    private boolean estado = true;

    // Datos de solo lectura (consultas con JOIN o subconsultas)
    private String nombreServicio;
    private int totalFotos;
    private String primeraFoto;
    private List<TrabajoImagen> fotos = new ArrayList<>();

    /** @return true si es un comparativo antes/después */
    public boolean isRestauracion() {
        return RESTAURACION.equals(tipo);
    }

    /** @return nombre del tipo para mostrar en pantalla */
    public String getTipoLegible() {
        if (RESTAURACION.equals(tipo)) {
            return "Restauración";
        }
        return COLABORACION.equals(tipo) ? "Colaboración" : "";
    }

    /** @return la mejor foto para miniaturas: el "después", o la primera foto adicional */
    public String getImagenPortada() {
        if (imagenDespues != null) {
            return imagenDespues;
        }
        return primeraFoto;
    }

    public int getIdTrabajo() {
        return idTrabajo;
    }

    public void setIdTrabajo(int idTrabajo) {
        this.idTrabajo = idTrabajo;
    }

    public Integer getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Integer idServicio) {
        this.idServicio = idServicio;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getImagenAntes() {
        return imagenAntes;
    }

    public void setImagenAntes(String imagenAntes) {
        this.imagenAntes = imagenAntes;
    }

    public String getImagenDespues() {
        return imagenDespues;
    }

    public void setImagenDespues(String imagenDespues) {
        this.imagenDespues = imagenDespues;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public String getNombreServicio() {
        return nombreServicio;
    }

    public void setNombreServicio(String nombreServicio) {
        this.nombreServicio = nombreServicio;
    }

    public int getTotalFotos() {
        return totalFotos;
    }

    public void setTotalFotos(int totalFotos) {
        this.totalFotos = totalFotos;
    }

    public String getPrimeraFoto() {
        return primeraFoto;
    }

    public void setPrimeraFoto(String primeraFoto) {
        this.primeraFoto = primeraFoto;
    }

    public List<TrabajoImagen> getFotos() {
        return fotos;
    }

    public void setFotos(List<TrabajoImagen> fotos) {
        this.fotos = fotos;
    }
}
