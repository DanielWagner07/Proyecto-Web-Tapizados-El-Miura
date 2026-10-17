package co.tapizadosmiura.modelo;

import java.util.Map;

/**
 * Dato editable del negocio (CU09): teléfono, WhatsApp, horario, garantía...
 * Tabla {@code informacion_negocio}, organizada como clave/valor.
 *
 * <p>La {@code clave} la usa el código (ej. "whatsapp"); la {@code etiqueta}
 * es el nombre que ve el administrador (ej. "WhatsApp de ventas").</p>
 */
public class InformacionNegocio {

    /** Nombre legible de cada grupo del ENUM {@code grupo}. */
    private static final Map<String, String> NOMBRES_GRUPO = Map.of(
            "contacto", "Contacto",
            "horario", "Horario",
            "cobertura", "Cobertura",
            "nosotros", "Nosotros",
            "politicas", "Políticas",
            "redes", "Redes sociales");

    private int idInformacion;
    private String clave;
    private String etiqueta;
    private String valor;
    private String grupo;

    /** @return nombre del grupo para mostrar como título de sección */
    public String getNombreGrupo() {
        return NOMBRES_GRUPO.getOrDefault(grupo, grupo);
    }

    /** @return true si el valor suele ser un texto largo (se edita en un área de texto) */
    public boolean isTextoLargo() {
        return "nosotros".equals(grupo) || "politicas".equals(grupo) || "cobertura".equals(grupo);
    }

    public int getIdInformacion() {
        return idInformacion;
    }

    public void setIdInformacion(int idInformacion) {
        this.idInformacion = idInformacion;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }
}
