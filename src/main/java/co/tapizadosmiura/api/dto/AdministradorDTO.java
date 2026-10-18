package co.tapizadosmiura.api.dto;

import co.tapizadosmiura.modelo.Administrador;

/** Datos públicos del administrador en sesión (nunca incluye el hash de la contraseña). */
public record AdministradorDTO(int idAdministrador, String nombre, String correo) {

    public static AdministradorDTO de(Administrador a) {
        return new AdministradorDTO(a.getIdAdministrador(), a.getNombre(), a.getCorreo());
    }
}
