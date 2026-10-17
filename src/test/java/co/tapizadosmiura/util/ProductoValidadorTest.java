package co.tapizadosmiura.util;

import co.tapizadosmiura.modelo.Producto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de las reglas de validación del formulario de producto (CU07).
 */
class ProductoValidadorTest {

    /** Formulario mínimo válido: solo los campos obligatorios. */
    private Map<String, String> formularioValido() {
        Map<String, String> campos = new HashMap<>();
        campos.put("referencia", "00099");
        campos.put("nombre", "Sofá de prueba");
        campos.put("idCategoria", "1");
        campos.put("precio", "1500000");
        campos.put("stock", "3");
        campos.put("estado", "on");
        return campos;
    }

    @Test
    @DisplayName("Un formulario con los campos obligatorios es válido")
    void formularioMinimoEsValido() {
        ResultadoValidacion<Producto> r = ProductoValidador.validar(formularioValido());

        assertTrue(r.esValido(), () -> "Errores inesperados: " + r.getErrores());
        assertEquals("00099", r.getObjeto().getReferencia());
        assertEquals(new BigDecimal("1500000"), r.getObjeto().getPrecio());
        assertTrue(r.getObjeto().isEstado());
        assertFalse(r.getObjeto().isRequiereArmado());
        assertNull(r.getObjeto().getAltoCm());
    }

    @Test
    @DisplayName("Los campos obligatorios vacíos generan error")
    void camposObligatoriosVacios() {
        ResultadoValidacion<Producto> r = ProductoValidador.validar(new HashMap<>());

        assertFalse(r.esValido());
        assertTrue(r.getErrores().keySet().containsAll(
                java.util.List.of("referencia", "nombre", "idCategoria", "precio", "stock")));
    }

    @Test
    @DisplayName("El precio debe ser mayor que cero (regla RF07)")
    void precioCeroONegativo() {
        Map<String, String> campos = formularioValido();
        campos.put("precio", "0");
        assertTrue(ProductoValidador.validar(campos).getErrores().containsKey("precio"));

        campos.put("precio", "-5000");
        assertTrue(ProductoValidador.validar(campos).getErrores().containsKey("precio"));

        campos.put("precio", "abc");
        assertTrue(ProductoValidador.validar(campos).getErrores().containsKey("precio"));
    }

    @Test
    @DisplayName("El stock no puede ser negativo ni decimal")
    void stockInvalido() {
        Map<String, String> campos = formularioValido();
        campos.put("stock", "-1");
        assertTrue(ProductoValidador.validar(campos).getErrores().containsKey("stock"));

        campos.put("stock", "2.5");
        assertTrue(ProductoValidador.validar(campos).getErrores().containsKey("stock"));
    }

    @Test
    @DisplayName("La referencia solo admite letras, números y guiones")
    void referenciaConCaracteresInvalidos() {
        Map<String, String> campos = formularioValido();
        campos.put("referencia", "REF 001/A");
        assertTrue(ProductoValidador.validar(campos).getErrores().containsKey("referencia"));

        campos.put("referencia", "REF-001-A");
        assertTrue(ProductoValidador.validar(campos).esValido());
    }

    @Test
    @DisplayName("Las medidas opcionales aceptan coma decimal y rechazan valores no positivos")
    void medidasOpcionales() {
        Map<String, String> campos = formularioValido();
        campos.put("pesoKg", "62,2");
        campos.put("altoCm", "0");

        ResultadoValidacion<Producto> r = ProductoValidador.validar(campos);

        assertEquals(new BigDecimal("62.2"), r.getObjeto().getPesoKg());
        assertTrue(r.getErrores().containsKey("altoCm"));
    }

    @Test
    @DisplayName("Una casilla desmarcada no se envía y queda en false")
    void casillasDeVerificacion() {
        Map<String, String> campos = formularioValido();
        campos.remove("estado");
        campos.put("requiereArmado", "on");

        ResultadoValidacion<Producto> r = ProductoValidador.validar(campos);

        assertFalse(r.getObjeto().isEstado());
        assertTrue(r.getObjeto().isRequiereArmado());
    }

    @Test
    @DisplayName("Se eliminan los espacios sobrantes de los textos")
    void textosSinEspacios() {
        Map<String, String> campos = formularioValido();
        campos.put("nombre", "   Poltrona azul   ");
        campos.put("color", "   ");

        ResultadoValidacion<Producto> r = ProductoValidador.validar(campos);

        assertEquals("Poltrona azul", r.getObjeto().getNombre());
        assertNull(r.getObjeto().getColor());
    }
}
