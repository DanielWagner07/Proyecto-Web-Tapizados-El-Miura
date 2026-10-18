package co.tapizadosmiura.api.dto;

/**
 * Foto de un producto o de un trabajo.
 *
 * @param url              dirección lista para usar en {@code <img src>}
 * @param textoAlternativo descripción para el atributo {@code alt} (accesibilidad)
 */
public record ImagenDTO(String url, String textoAlternativo) {
}
