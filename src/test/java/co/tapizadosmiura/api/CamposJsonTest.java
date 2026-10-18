package co.tapizadosmiura.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la conversión de un cuerpo JSON al formato de campos que usan los validadores.
 */
class CamposJsonTest {

    private static final Set<String> TEXTOS = Set.of("nombre", "precio");
    private static final Set<String> BOOLEANOS = Set.of("estado");

    private static JsonObject json(String texto) {
        return JsonParser.parseString(texto).getAsJsonObject();
    }

    @Test
    @DisplayName("Textos y números se convierten a texto; true queda como casilla marcada")
    void conversionBasica() {
        Map<String, String> campos = CamposJson.aCampos(
                json("{\"nombre\": \"Sofá\", \"precio\": 1999999, \"estado\": true}"), TEXTOS, BOOLEANOS);
        assertEquals("Sofá", campos.get("nombre"));
        assertEquals("1999999", campos.get("precio"));
        assertTrue(campos.containsKey("estado"));
    }

    @Test
    @DisplayName("false y null se omiten, igual que una casilla sin marcar o un campo vacío")
    void falseYNullSeOmiten() {
        Map<String, String> campos = CamposJson.aCampos(
                json("{\"nombre\": null, \"estado\": false}"), TEXTOS, BOOLEANOS);
        assertTrue(campos.isEmpty());
    }

    @Test
    @DisplayName("Un campo desconocido o con tipo incorrecto responde 400 con el detalle")
    void camposInvalidos() {
        ApiExcepcion error = assertThrows(ApiExcepcion.class, () -> CamposJson.aCampos(
                json("{\"inventado\": 1, \"estado\": \"si\", \"nombre\": [1]}"), TEXTOS, BOOLEANOS));
        assertEquals(400, error.getEstadoHttp());
        assertEquals(Set.of("inventado", "estado", "nombre"), error.getErrores().keySet());
    }

    @Test
    @DisplayName("El booleano obligatorio exige true o false")
    void booleanoObligatorio() {
        assertTrue(CamposJson.booleanoObligatorio(json("{\"estado\": true}"), "estado"));
        assertFalse(CamposJson.booleanoObligatorio(json("{\"estado\": false}"), "estado"));
        assertThrows(ApiExcepcion.class, () -> CamposJson.booleanoObligatorio(json("{\"estado\": \"true\"}"), "estado"));
        assertThrows(ApiExcepcion.class, () -> CamposJson.booleanoObligatorio(json("{}"), "estado"));
    }

    @Test
    @DisplayName("El texto opcional se recorta y admite ausencia")
    void textoOpcional() {
        assertEquals("hola", CamposJson.textoOpcional(json("{\"nota\": \"  hola \"}"), "nota"));
        assertNull(CamposJson.textoOpcional(json("{}"), "nota"));
        assertThrows(ApiExcepcion.class, () -> CamposJson.textoOpcional(json("{\"nota\": 5}"), "nota"));
    }

    @Test
    @DisplayName("Las rutas de imagen se convierten en direcciones dentro de la aplicación")
    void urlDeImagenes() {
        assertEquals("/tapizados-miura/uploads/productos/x.webp",
                UrlPublica.imagen("/tapizados-miura", "uploads/productos/x.webp"));
        assertEquals("/tapizados-miura/img/sin-imagen.svg", UrlPublica.imagen("/tapizados-miura", null));
        assertNull(UrlPublica.imagenOpcional("/tapizados-miura", ""));
    }
}
