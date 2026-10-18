package co.tapizadosmiura.api.dto;

/**
 * Respuesta de las operaciones que modifican datos (POST, PUT, DELETE).
 *
 * <pre>{"mensaje": "Producto actualizado.", "datos": {...}}</pre>
 *
 * @param mensaje confirmación para mostrar al usuario
 * @param datos   recurso creado o modificado; se omite si no aplica
 */
public record RespuestaDTO(String mensaje, Object datos) {
}
