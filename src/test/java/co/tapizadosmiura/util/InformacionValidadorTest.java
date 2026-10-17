package co.tapizadosmiura.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pruebas de la validación de la información del negocio (CU09).
 */
class InformacionValidadorTest {

    @Test
    @DisplayName("El WhatsApp se guarda solo con números")
    void normalizaWhatsapp() {
        assertEquals("573120000000", InformacionValidador.normalizar("whatsapp", " +57 312 000-0000 "));
        assertEquals("Caucasia", InformacionValidador.normalizar("direccion", "  Caucasia "));
        assertEquals("", InformacionValidador.normalizar("direccion", null));
    }

    @Test
    @DisplayName("Ningún dato puede quedar vacío ni superar 2000 caracteres (CU09 2a)")
    void obligatorioYLongitud() {
        assertNotNull(InformacionValidador.validar("horario", "horario", ""));
        assertNotNull(InformacionValidador.validar("trayectoria", "nosotros", "x".repeat(2001)));
        assertNull(InformacionValidador.validar("trayectoria", "nosotros", "x".repeat(2000)));
    }

    @Test
    @DisplayName("El WhatsApp debe tener entre 10 y 15 dígitos")
    void formatoWhatsapp() {
        assertNull(InformacionValidador.validar("whatsapp", "contacto", "573120000000"));
        assertNotNull(InformacionValidador.validar("whatsapp", "contacto", "312"));
        assertNotNull(InformacionValidador.validar("whatsapp", "contacto", "57312abc0000"));
    }

    @Test
    @DisplayName("El teléfono acepta números, espacios, guiones y el signo +")
    void formatoTelefono() {
        assertNull(InformacionValidador.validar("telefono", "contacto", "+57 333 000-0000"));
        assertNotNull(InformacionValidador.validar("telefono", "contacto", "llamar al 333"));
    }

    @Test
    @DisplayName("El correo debe tener un formato válido")
    void formatoCorreo() {
        assertNull(InformacionValidador.validar("correo", "contacto", "contacto@tapizadosmiura.com"));
        assertNotNull(InformacionValidador.validar("correo", "contacto", "contacto.tapizadosmiura.com"));
    }

    @Test
    @DisplayName("Las redes sociales deben ser direcciones web completas")
    void formatoRedes() {
        assertNull(InformacionValidador.validar("instagram", "redes", "https://instagram.com/tapizadoselmiura"));
        assertNotNull(InformacionValidador.validar("instagram", "redes", "@tapizadoselmiura"));
    }
}
