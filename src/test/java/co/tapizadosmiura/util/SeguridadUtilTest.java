package co.tapizadosmiura.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la verificación de contraseñas (CU01 - Iniciar sesión).
 */
class SeguridadUtilTest {

    /** Hash del administrador de 02_datos_prueba.sql (contraseña Miura2026*Cambiar). */
    private static final String HASH_DATOS_PRUEBA =
            "$2y$10$URZ6HrdCF5wVxkjxUpTn9u4wis3IxUtT6iJbA55Xc7kO.NIMQkmPe";

    @Test
    @DisplayName("Acepta la contraseña correcta aunque el hash use el prefijo $2y$")
    void contrasenaCorrectaConPrefijo2y() {
        assertTrue(SeguridadUtil.verificarContrasena("Miura2026*Cambiar", HASH_DATOS_PRUEBA));
    }

    @Test
    @DisplayName("Rechaza una contraseña incorrecta")
    void contrasenaIncorrecta() {
        assertFalse(SeguridadUtil.verificarContrasena("miura2026*cambiar", HASH_DATOS_PRUEBA));
        assertFalse(SeguridadUtil.verificarContrasena("", HASH_DATOS_PRUEBA));
    }

    @Test
    @DisplayName("Rechaza valores nulos o hashes mal formados sin lanzar excepciones")
    void entradasInvalidas() {
        assertFalse(SeguridadUtil.verificarContrasena(null, HASH_DATOS_PRUEBA));
        assertFalse(SeguridadUtil.verificarContrasena("x", null));
        assertFalse(SeguridadUtil.verificarContrasena("x", "texto-que-no-es-hash"));
    }

    @Test
    @DisplayName("Un hash generado se puede verificar")
    void generarYVerificar() {
        String hash = SeguridadUtil.generarHash("ClaveNueva#2026");
        assertTrue(SeguridadUtil.verificarContrasena("ClaveNueva#2026", hash));
    }
}
