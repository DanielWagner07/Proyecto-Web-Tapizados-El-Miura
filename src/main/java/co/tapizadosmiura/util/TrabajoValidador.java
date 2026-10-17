package co.tapizadosmiura.util;

import co.tapizadosmiura.modelo.Trabajo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static co.tapizadosmiura.util.CamposFormulario.entero;
import static co.tapizadosmiura.util.CamposFormulario.texto;
import static co.tapizadosmiura.util.CamposFormulario.textoLimitado;

/**
 * Validación del formulario de trabajos del portafolio (CU08).
 *
 * <p>Reglas principales:</p>
 * <ul>
 *   <li>Toda restauración debe tener foto del antes y del después (RF08; la tabla
 *       lo garantiza también con el CHECK {@code ck_trabajo_restauracion_imagenes}).</li>
 *   <li>Un trabajo admite como máximo {@value #MAXIMO_FOTOS_ADICIONALES} fotos adicionales.</li>
 * </ul>
 */
public final class TrabajoValidador {

    /** Fotos adicionales permitidas por trabajo (el prototipo muestra 4; se deja margen). */
    public static final int MAXIMO_FOTOS_ADICIONALES = 8;

    private static final Set<String> TIPOS = Set.of(Trabajo.RESTAURACION, Trabajo.COLABORACION);

    private TrabajoValidador() {
    }

    /**
     * Valida los campos de texto del formulario.
     *
     * @param campos valores del formulario (atributo name → valor)
     * @return trabajo armado y errores por campo
     */
    public static ResultadoValidacion<Trabajo> validar(Map<String, String> campos) {
        Map<String, String> errores = new LinkedHashMap<>();
        Trabajo t = new Trabajo();

        t.setTipo(texto(campos, "tipo"));
        if (t.getTipo() == null || !TIPOS.contains(t.getTipo())) {
            errores.put("tipo", "Seleccione si es una restauración o una colaboración.");
        }

        t.setTitulo(texto(campos, "titulo"));
        if (t.getTitulo() == null) {
            errores.put("titulo", "El título es obligatorio.");
        } else if (t.getTitulo().length() > 150) {
            errores.put("titulo", "El título admite máximo 150 caracteres.");
        }

        t.setDescripcion(textoLimitado(campos, "descripcion", 2000, errores));
        t.setCliente(textoLimitado(campos, "cliente", 150, errores));

        if (texto(campos, "idServicio") != null) {
            Integer idServicio = entero(campos, "idServicio");
            if (idServicio == null || idServicio <= 0) {
                errores.put("idServicio", "Seleccione un servicio de la lista.");
            } else {
                t.setIdServicio(idServicio);
            }
        }

        t.setEstado(campos.containsKey("estado"));
        return new ResultadoValidacion<>(t, errores);
    }

    /**
     * Revisa la regla del antes/después: una restauración necesita ambas fotos,
     * ya sea porque se envían ahora o porque el trabajo ya las tenía.
     *
     * @param tipo         tipo de trabajo elegido
     * @param tieneAntes   true si habrá foto del antes (nueva o existente)
     * @param tieneDespues true si habrá foto del después (nueva o existente)
     * @param errores      mapa donde se agregan los errores
     */
    public static void validarAntesDespues(String tipo, boolean tieneAntes, boolean tieneDespues,
                                           Map<String, String> errores) {
        if (!Trabajo.RESTAURACION.equals(tipo)) {
            return;
        }
        if (!tieneAntes) {
            errores.putIfAbsent("imagenAntes", "Una restauración necesita la foto del antes.");
        }
        if (!tieneDespues) {
            errores.putIfAbsent("imagenDespues", "Una restauración necesita la foto del después.");
        }
    }

    /**
     * Revisa que no se supere el máximo de fotos adicionales.
     *
     * @param fotosActuales fotos que el trabajo ya tiene
     * @param fotosNuevas   fotos que se quieren agregar
     * @param errores       mapa donde se agrega el error
     */
    public static void validarCantidadFotos(int fotosActuales, int fotosNuevas, Map<String, String> errores) {
        if (fotosActuales + fotosNuevas > MAXIMO_FOTOS_ADICIONALES) {
            int disponibles = Math.max(0, MAXIMO_FOTOS_ADICIONALES - fotosActuales);
            errores.putIfAbsent("fotos", "Máximo " + MAXIMO_FOTOS_ADICIONALES
                    + " fotos adicionales por trabajo. Puede agregar " + disponibles + " más.");
        }
    }
}
