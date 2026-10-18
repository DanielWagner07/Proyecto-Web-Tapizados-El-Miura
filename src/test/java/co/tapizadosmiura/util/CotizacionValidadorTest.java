package co.tapizadosmiura.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la validación del cuerpo de POST /api/cotizaciones (RF03 / CU04).
 */
class CotizacionValidadorTest {

    private static ResultadoValidacion<Map<Integer, Integer>> validar(String json) {
        JsonObject cuerpo = JsonParser.parseString(json).getAsJsonObject();
        return CotizacionValidador.validar(cuerpo);
    }

    @Test
    @DisplayName("Una cotización con productos y cantidades válidas se acepta")
    void cotizacionValida() {
        var resultado = validar("{\"items\": [{\"idProducto\": 1, \"cantidad\": 2}, {\"idProducto\": 4, \"cantidad\": 1}]}");
        assertTrue(resultado.esValido());
        assertEquals(Map.of(1, 2, 4, 1), resultado.getObjeto());
    }

    @Test
    @DisplayName("Sin productos la cotización se rechaza (CU05 2a: carrito vacío)")
    void carritoVacio() {
        assertTrue(validar("{\"items\": []}").getErrores().containsKey("items"));
        assertTrue(validar("{}").getErrores().containsKey("items"));
        assertTrue(validar("{\"items\": \"1,2\"}").getErrores().containsKey("items"));
    }

    @Test
    @DisplayName("La cantidad debe ser un entero entre 1 y 10")
    void rangoCantidad() {
        assertFalse(validar("{\"items\": [{\"idProducto\": 1, \"cantidad\": 0}]}").esValido());
        assertFalse(validar("{\"items\": [{\"idProducto\": 1, \"cantidad\": 11}]}").esValido());
        assertFalse(validar("{\"items\": [{\"idProducto\": 1, \"cantidad\": 2.5}]}").esValido());
        assertFalse(validar("{\"items\": [{\"idProducto\": 1}]}").esValido());
        assertTrue(validar("{\"items\": [{\"idProducto\": 1, \"cantidad\": 10}]}").esValido());
    }

    @Test
    @DisplayName("El id del producto debe ser numérico y positivo")
    void idProducto() {
        var resultado = validar("{\"items\": [{\"idProducto\": \"sofa\", \"cantidad\": 1}, {\"idProducto\": -3, \"cantidad\": 1}]}");
        assertTrue(resultado.getErrores().containsKey("items[0].idProducto"));
        assertTrue(resultado.getErrores().containsKey("items[1].idProducto"));
    }

    @Test
    @DisplayName("Un número enviado como texto (valor de un input) se acepta")
    void numerosComoTexto() {
        var resultado = validar("{\"items\": [{\"idProducto\": \"2\", \"cantidad\": \"3\"}]}");
        assertTrue(resultado.esValido());
        assertEquals(Map.of(2, 3), resultado.getObjeto());
    }

    @Test
    @DisplayName("Un producto repetido suma sus cantidades, sin pasar de 10")
    void repetidosSeSuman() {
        var suma = validar("{\"items\": [{\"idProducto\": 1, \"cantidad\": 2}, {\"idProducto\": 1, \"cantidad\": 3}]}");
        assertTrue(suma.esValido());
        assertEquals(Map.of(1, 5), suma.getObjeto());

        var excede = validar("{\"items\": [{\"idProducto\": 1, \"cantidad\": 6}, {\"idProducto\": 1, \"cantidad\": 6}]}");
        assertTrue(excede.getErrores().containsKey("items[1].cantidad"));
    }

    @Test
    @DisplayName("El precio enviado por el navegador se ignora")
    void precioIgnorado() {
        var resultado = validar("{\"items\": [{\"idProducto\": 1, \"cantidad\": 1, \"precio\": 1}]}");
        assertTrue(resultado.esValido());
        assertEquals(Map.of(1, 1), resultado.getObjeto());
    }

    @Test
    @DisplayName("Máximo 20 productos distintos por cotización")
    void maximoProductos() {
        StringBuilder json = new StringBuilder("{\"items\": [");
        for (int i = 1; i <= 21; i++) {
            json.append(i > 1 ? "," : "").append("{\"idProducto\": ").append(i).append(", \"cantidad\": 1}");
        }
        json.append("]}");
        assertTrue(validar(json.toString()).getErrores().containsKey("items"));
    }
}
