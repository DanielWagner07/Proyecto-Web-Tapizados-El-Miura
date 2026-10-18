package co.tapizadosmiura.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Lectura de campos de un cuerpo JSON.
 *
 * <p>Los validadores del panel ({@code ProductoValidador}, {@code InformacionValidador}...)
 * reciben un mapa nombre → texto, igual que llegan los campos de un formulario HTML.
 * {@link #aCampos} convierte el JSON a ese mismo formato para que la API y el
 * panel apliquen exactamente las mismas reglas:</p>
 * <ul>
 *   <li>Textos y números → su valor como texto ({@code 1999999} → {@code "1999999"}).</li>
 *   <li>Booleanos {@code true} → el campo queda presente, como una casilla marcada.</li>
 *   <li>Booleanos {@code false} y {@code null} → el campo se omite, como una casilla sin marcar
 *       o un campo vacío.</li>
 * </ul>
 */
public final class CamposJson {

    private CamposJson() {
    }

    /**
     * Convierte el cuerpo JSON en un mapa de campos de formulario.
     *
     * @param cuerpo    objeto recibido
     * @param textos    campos de texto o número admitidos
     * @param booleanos campos que deben ser {@code true} o {@code false}
     * @return mapa nombre → valor listo para el validador
     * @throws ApiExcepcion 400 si hay campos desconocidos o con un tipo de dato incorrecto
     */
    public static Map<String, String> aCampos(JsonObject cuerpo, Set<String> textos, Set<String> booleanos) {
        Map<String, String> campos = new LinkedHashMap<>();
        Map<String, String> errores = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entrada : cuerpo.entrySet()) {
            String nombre = entrada.getKey();
            JsonElement valor = entrada.getValue();
            if (booleanos.contains(nombre)) {
                if (valor.isJsonNull()) {
                    continue;
                }
                if (!esBooleano(valor)) {
                    errores.put(nombre, "Debe ser true o false.");
                } else if (valor.getAsBoolean()) {
                    campos.put(nombre, "on");
                }
            } else if (textos.contains(nombre)) {
                if (valor.isJsonNull()) {
                    continue;
                }
                if (!esTextoONumero(valor)) {
                    errores.put(nombre, "Debe ser un texto o un número.");
                } else {
                    campos.put(nombre, valor.getAsString());
                }
            } else {
                errores.put(nombre, "Campo no reconocido.");
            }
        }
        if (!errores.isEmpty()) {
            throw ApiExcepcion.solicitudInvalida("Hay campos con errores.", errores);
        }
        return campos;
    }

    /**
     * Lee un booleano obligatorio, por ejemplo {@code {"estado": true}}.
     *
     * @throws ApiExcepcion 400 si falta o no es booleano
     */
    public static boolean booleanoObligatorio(JsonObject cuerpo, String nombre) {
        JsonElement valor = cuerpo.get(nombre);
        if (valor == null || !esBooleano(valor)) {
            throw ApiExcepcion.solicitudInvalida("Hay campos con errores.",
                    Map.of(nombre, "Es obligatorio y debe ser true o false."));
        }
        return valor.getAsBoolean();
    }

    /**
     * Lee un texto opcional.
     *
     * @return el texto sin espacios sobrantes, {@code null} si no se envió o es null
     * @throws ApiExcepcion 400 si se envió con otro tipo de dato
     */
    public static String textoOpcional(JsonObject cuerpo, String nombre) {
        JsonElement valor = cuerpo.get(nombre);
        if (valor == null || valor.isJsonNull()) {
            return null;
        }
        if (!valor.isJsonPrimitive() || !valor.getAsJsonPrimitive().isString()) {
            throw ApiExcepcion.solicitudInvalida("Hay campos con errores.", Map.of(nombre, "Debe ser un texto."));
        }
        return valor.getAsString().trim();
    }

    private static boolean esBooleano(JsonElement valor) {
        return valor.isJsonPrimitive() && valor.getAsJsonPrimitive().isBoolean();
    }

    private static boolean esTextoONumero(JsonElement valor) {
        if (!valor.isJsonPrimitive()) {
            return false;
        }
        JsonPrimitive primitivo = valor.getAsJsonPrimitive();
        return primitivo.isString() || primitivo.isNumber();
    }
}
