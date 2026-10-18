package co.tapizadosmiura.util;

import co.tapizadosmiura.modelo.DetalleCotizacion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del mensaje y el enlace de WhatsApp de una cotización (RF04 / RNF08).
 */
class MensajeWhatsAppTest {

    private static DetalleCotizacion detalle(String nombre, int cantidad, String precio, int stock) {
        DetalleCotizacion d = new DetalleCotizacion();
        d.setNombreProducto(nombre);
        d.setCantidad(cantidad);
        d.setPrecioUnitario(new BigDecimal(precio));
        d.setStockDisponible(stock);
        return d;
    }

    @Test
    @DisplayName("Los precios se muestran en pesos con punto de miles y sin centavos")
    void formatoPesos() {
        assertEquals("$1.999.999", FormatoMoneda.pesos(new BigDecimal("1999999.00")));
        assertEquals("$350.000", FormatoMoneda.pesos(new BigDecimal("350000")));
        assertEquals("$0", FormatoMoneda.pesos(null));
    }

    @Test
    @DisplayName("El mensaje lista cada producto con su subtotal y el total")
    void textoDelMensaje() {
        List<DetalleCotizacion> detalles = List.of(
                detalle("Sofá Roman", 1, "1999999.00", 4),
                detalle("Poltrona Azul", 2, "1299999.00", 3));
        String texto = MensajeWhatsApp.texto(7, detalles, new BigDecimal("4599997.00"));
        assertTrue(texto.startsWith("Hola, Tapizados El Miura. Quiero esta cotización (N.º 7):"));
        assertTrue(texto.contains("- 1 x Sofá Roman: $1.999.999"));
        assertTrue(texto.contains("- 2 x Poltrona Azul ($1.299.999 c/u): $2.599.998"));
        assertTrue(texto.endsWith("Total estimado: $4.599.997"));
    }

    @Test
    @DisplayName("Los muebles agotados o sin stock suficiente salen como sujetos a fabricación")
    void notaDeDisponibilidad() {
        String agotado = MensajeWhatsApp.texto(1, List.of(detalle("Sofá Gris", 1, "100", 0)), new BigDecimal("100"));
        assertTrue(agotado.contains("(agotado, sujeto a fabricación)"));

        String parcial = MensajeWhatsApp.texto(1, List.of(detalle("Sofá Beige", 3, "100", 2)), new BigDecimal("300"));
        assertTrue(parcial.contains("(2 disponibles; el resto, sujeto a fabricación)"));

        String suficiente = MensajeWhatsApp.texto(1, List.of(detalle("Sofá Azul", 2, "100", 2)), new BigDecimal("200"));
        assertFalse(suficiente.contains("fabricación"));
    }

    @Test
    @DisplayName("El enlace codifica los espacios como %20 y no como + (RNF08)")
    void codificacionDelEnlace() {
        String texto = "Hola, Miura: 2 x Sofá + cojín\nTotal: $100";
        String enlace = MensajeWhatsApp.enlace("573128628147", texto);
        assertTrue(enlace.startsWith("https://wa.me/573128628147?text="));
        String parametro = enlace.substring(enlace.indexOf("text=") + 5);
        assertFalse(parametro.contains("+"), "Un espacio codificado como + se vería literal en WhatsApp");
        assertFalse(parametro.contains(" "));
        assertTrue(parametro.contains("%20"));
        assertTrue(parametro.contains("%2B"), "El signo + escrito en el texto se conserva");
        assertTrue(parametro.contains("%0A"), "Los saltos de línea se conservan");
        // Al decodificar (cambiando %20 por + como lo hace URLDecoder) se recupera el texto original
        assertEquals(texto, URLDecoder.decode(parametro.replace("%20", "+"), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("El número del negocio debe tener solo dígitos con indicativo")
    void numeroDelNegocio() {
        assertTrue(MensajeWhatsApp.numeroValido("573128628147"));
        assertFalse(MensajeWhatsApp.numeroValido("+57 312 862 8147"));
        assertFalse(MensajeWhatsApp.numeroValido(null));
    }
}
