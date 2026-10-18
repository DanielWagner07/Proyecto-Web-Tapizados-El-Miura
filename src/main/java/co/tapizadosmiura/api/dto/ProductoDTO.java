package co.tapizadosmiura.api.dto;

import co.tapizadosmiura.api.UrlPublica;
import co.tapizadosmiura.modelo.Producto;
import co.tapizadosmiura.modelo.ProductoImagen;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mueble del catálogo tal como lo entrega la API.
 *
 * <p>Se arma con tres variantes según el servicio:</p>
 * <ul>
 *   <li>{@link #resumen}: listado del catálogo (sin carrusel de fotos).</li>
 *   <li>{@link #detalle}: ficha del producto con todas sus fotos (RF02).</li>
 *   <li>{@link #admin}: igual que el detalle, más {@code estado} y {@code tieneFoto}.</li>
 * </ul>
 * Los campos que no aplican a la variante salen en {@code null} y Gson los omite.
 *
 * @param agotado   true si el stock es 0 (RF02: se indica visualmente)
 * @param imagen    URL de la imagen principal (o la genérica si no tiene)
 * @param estado    solo administración: true = visible en el catálogo
 * @param tieneFoto solo administración: false = no se puede publicar hasta subir la foto
 */
public record ProductoDTO(int idProducto, String referencia, String nombre, String descripcion,
                          BigDecimal precio, int stock, boolean agotado, int idCategoria, String categoria,
                          String imagen, FichaTecnicaDTO ficha, List<ImagenDTO> imagenes,
                          Boolean estado, Boolean tieneFoto) {

    /** Variante para el listado del catálogo. */
    public static ProductoDTO resumen(Producto p, String contexto) {
        return armar(p, contexto, null, null, null);
    }

    /** Variante para la ficha del producto, con el carrusel de fotos. */
    public static ProductoDTO detalle(Producto p, List<ProductoImagen> imagenes, String contexto) {
        return armar(p, contexto, imagenes(imagenes, contexto), null, null);
    }

    /** Variante para el administrador; {@code imagenes} puede ser null en los listados. */
    public static ProductoDTO admin(Producto p, List<ProductoImagen> imagenes, String contexto) {
        return armar(p, contexto, imagenes == null ? null : imagenes(imagenes, contexto),
                p.isEstado(), p.getImagenPrincipal() != null);
    }

    private static ProductoDTO armar(Producto p, String contexto, List<ImagenDTO> imagenes,
                                     Boolean estado, Boolean tieneFoto) {
        return new ProductoDTO(p.getIdProducto(), p.getReferencia(), p.getNombre(), p.getDescripcion(),
                p.getPrecio(), p.getStock(), p.isAgotado(), p.getIdCategoria(), p.getNombreCategoria(),
                UrlPublica.imagen(contexto, p.getImagenPrincipal()), FichaTecnicaDTO.de(p), imagenes,
                estado, tieneFoto);
    }

    private static List<ImagenDTO> imagenes(List<ProductoImagen> imagenes, String contexto) {
        return imagenes.stream()
                .map(i -> new ImagenDTO(UrlPublica.imagen(contexto, i.getRuta()), i.getTextoAlternativo()))
                .toList();
    }
}
