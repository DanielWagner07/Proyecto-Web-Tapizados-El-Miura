package co.tapizadosmiura.modelo;

/**
 * Usuario autorizado para entrar al panel administrador. Tabla {@code administrador}.
 *
 * <p>La contraseña nunca se guarda en texto plano: {@code contrasenaHash}
 * contiene el hash BCrypt. Al guardar el administrador en la sesión se usa
 * {@link #sinContrasena()} para que el hash no quede en memoria de sesión.</p>
 */
public class Administrador {

    private int idAdministrador;
    private String nombre;
    private String correo;
    private String contrasenaHash;
    private boolean estado;

    /**
     * Copia del administrador sin el hash de la contraseña, apta para la sesión.
     *
     * @return nuevo objeto con id, nombre, correo y estado
     */
    public Administrador sinContrasena() {
        Administrador copia = new Administrador();
        copia.setIdAdministrador(idAdministrador);
        copia.setNombre(nombre);
        copia.setCorreo(correo);
        copia.setEstado(estado);
        return copia;
    }

    public int getIdAdministrador() {
        return idAdministrador;
    }

    public void setIdAdministrador(int idAdministrador) {
        this.idAdministrador = idAdministrador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
