package co.tapizadosmiura.api.dto;

import java.util.Map;

/**
 * Formato único de error de la API.
 *
 * <pre>{"mensaje": "Hay campos con errores.", "errores": {"precio": "El precio debe ser mayor que cero."}}</pre>
 *
 * @param mensaje explicación general para el usuario
 * @param errores detalle por campo; se omite si no aplica
 */
public record ErrorDTO(String mensaje, Map<String, String> errores) {
}
