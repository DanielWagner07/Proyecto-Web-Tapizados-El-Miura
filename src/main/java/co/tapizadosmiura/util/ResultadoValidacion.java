package co.tapizadosmiura.util;

import java.util.Map;

/**
 * Resultado de validar un formulario: el objeto armado con lo que escribió el
 * usuario y los mensajes de error por campo.
 *
 * <p>El objeto se devuelve aunque tenga errores, para volver a llenar el
 * formulario sin que el usuario pierda lo que escribió.</p>
 *
 * @param <T> tipo del objeto validado (Producto, Trabajo...)
 */
public final class ResultadoValidacion<T> {

    private final T objeto;
    private final Map<String, String> errores;

    public ResultadoValidacion(T objeto, Map<String, String> errores) {
        this.objeto = objeto;
        this.errores = errores;
    }

    public T getObjeto() {
        return objeto;
    }

    /** @return mensajes de error indexados por nombre del campo (vacío si todo es válido) */
    public Map<String, String> getErrores() {
        return errores;
    }

    public boolean esValido() {
        return errores.isEmpty();
    }
}
