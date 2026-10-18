package co.tapizadosmiura.api.dto;

/**
 * Estado de la sesión del administrador.
 *
 * @param autenticado   true si hay una sesión iniciada
 * @param administrador quién inició sesión; se omite si no hay sesión
 */
public record SesionDTO(boolean autenticado, AdministradorDTO administrador) {
}
