package co.tapizadosmiura.api.dto;

import co.tapizadosmiura.api.UrlPublica;
import co.tapizadosmiura.modelo.Trabajo;

import java.util.List;

/**
 * Trabajo del portafolio (RF05 / CU06).
 *
 * <ul>
 *   <li>Restauración: trae {@code imagenAntes} e {@code imagenDespues} (comparativo).</li>
 *   <li>Colaboración: trae sus {@code fotos}; antes/después pueden faltar y se omiten.</li>
 * </ul>
 *
 * @param imagenPortada mejor foto para una miniatura: el "después" o la primera foto adicional
 * @param estado        solo administración: true = visible en la galería
 */
public record TrabajoDTO(int idTrabajo, String tipo, String tipoLegible, String titulo, String descripcion,
                         String cliente, Integer idServicio, String servicio, String imagenAntes,
                         String imagenDespues, String imagenPortada, List<ImagenDTO> fotos, Boolean estado) {

    /** Variante pública. */
    public static TrabajoDTO publico(Trabajo t, String contexto) {
        return armar(t, contexto, null);
    }

    /** Variante para el administrador (incluye el estado de visibilidad). */
    public static TrabajoDTO admin(Trabajo t, String contexto) {
        return armar(t, contexto, t.isEstado());
    }

    private static TrabajoDTO armar(Trabajo t, String contexto, Boolean estado) {
        List<ImagenDTO> fotos = t.getFotos().stream()
                .map(f -> new ImagenDTO(UrlPublica.imagen(contexto, f.getRuta()), f.getTextoAlternativo()))
                .toList();
        return new TrabajoDTO(t.getIdTrabajo(), t.getTipo(), t.getTipoLegible(), t.getTitulo(),
                t.getDescripcion(), t.getCliente(), t.getIdServicio(), t.getNombreServicio(),
                UrlPublica.imagenOpcional(contexto, t.getImagenAntes()),
                UrlPublica.imagenOpcional(contexto, t.getImagenDespues()),
                UrlPublica.imagen(contexto, t.getImagenPortada()), fotos, estado);
    }
}
