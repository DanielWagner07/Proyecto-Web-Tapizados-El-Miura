package co.tapizadosmiura.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Validación del cuerpo de {@code POST /api/cotizaciones} (RF03 / CU04).
 *
 * <p>Formato esperado:</p>
 * <pre>{"items": [{"idProducto": 1, "cantidad": 2}, {"idProducto": 4, "cantidad": 1}]}</pre>
 *
 * <p>Reglas:</p>
 * <ul>
 *   <li>Al menos un producto (CU05-2a: "Carrito vacío") y máximo
 *       {@value #MAXIMO_PRODUCTOS} productos distintos.</li>
 *   <li>Cantidad entera entre 1 y {@value #MAXIMO_UNIDADES} por producto (igual que el
 *       selector de cantidad de la página de detalle).</li>
 *   <li>Si un producto llega repetido se suman sus cantidades.</li>
 *   <li>Cualquier otro campo (por ejemplo un precio) se ignora: el precio siempre
 *       se toma de la base de datos.</li>
 * </ul>
 *
 * <p>Que cada producto exista y esté visible lo revisa el servicio contra la BD.</p>
 */
public final class CotizacionValidador {

    public static final int MAXIMO_PRODUCTOS = 20;
    public static final int MAXIMO_UNIDADES = 10;

    private CotizacionValidador() {
    }

    /**
     * @param cuerpo objeto JSON recibido
     * @return mapa idProducto → cantidad total (en el orden en que llegaron) y errores
     *         con claves como {@code items} o {@code items[0].cantidad}
     */
    public static ResultadoValidacion<Map<Integer, Integer>> validar(JsonObject cuerpo) {
        Map<String, String> errores = new LinkedHashMap<>();
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();

        JsonElement items = cuerpo.get("items");
        if (items == null || !items.isJsonArray()) {
            errores.put("items", "Envíe la lista de productos en el campo \"items\".");
            return new ResultadoValidacion<>(cantidades, errores);
        }
        if (items.getAsJsonArray().isEmpty()) {
            errores.put("items", "Agregue al menos un producto a la cotización.");
            return new ResultadoValidacion<>(cantidades, errores);
        }

        int posicion = 0;
        for (JsonElement elemento : items.getAsJsonArray()) {
            String prefijo = "items[" + posicion + "]";
            posicion++;
            if (!elemento.isJsonObject()) {
                errores.put(prefijo, "Cada producto debe ser un objeto con idProducto y cantidad.");
                continue;
            }
            JsonObject item = elemento.getAsJsonObject();
            Integer idProducto = entero(item.get("idProducto"));
            Integer cantidad = entero(item.get("cantidad"));
            if (idProducto == null || idProducto <= 0) {
                errores.put(prefijo + ".idProducto", "Debe ser el id numérico de un producto.");
            }
            if (cantidad == null || cantidad < 1 || cantidad > MAXIMO_UNIDADES) {
                errores.put(prefijo + ".cantidad", "Debe ser un número entero entre 1 y " + MAXIMO_UNIDADES + ".");
            }
            if (idProducto != null && idProducto > 0 && cantidad != null && cantidad >= 1) {
                int acumulada = cantidades.merge(idProducto, cantidad, Integer::sum);
                if (acumulada > MAXIMO_UNIDADES) {
                    errores.putIfAbsent(prefijo + ".cantidad",
                            "Máximo " + MAXIMO_UNIDADES + " unidades por producto (llegó repetido y suma " + acumulada + ").");
                }
            }
        }
        if (cantidades.size() > MAXIMO_PRODUCTOS) {
            errores.put("items", "Máximo " + MAXIMO_PRODUCTOS + " productos distintos por cotización.");
        }
        return new ResultadoValidacion<>(cantidades, errores);
    }

    /** Acepta un número entero o un texto con dígitos (el valor de un input llega como texto). */
    private static Integer entero(JsonElement valor) {
        if (valor == null || !valor.isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive primitivo = valor.getAsJsonPrimitive();
        if (!primitivo.isNumber() && !primitivo.isString()) {
            return null;
        }
        try {
            BigDecimal numero = new BigDecimal(primitivo.getAsString().trim());
            return numero.intValueExact();
        } catch (NumberFormatException | ArithmeticException e) {
            return null;
        }
    }
}
