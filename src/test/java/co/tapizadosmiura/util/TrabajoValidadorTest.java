package co.tapizadosmiura.util;

import co.tapizadosmiura.modelo.Trabajo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de las reglas de la galería de trabajos (CU08).
 */
class TrabajoValidadorTest {

    private Map<String, String> formularioValido() {
        Map<String, String> campos = new HashMap<>();
        campos.put("tipo", "restauracion");
        campos.put("titulo", "Sofá capitoné amarillo");
        campos.put("estado", "on");
        return campos;
    }

    @Test
    @DisplayName("Un formulario con tipo y título es válido")
    void formularioMinimoEsValido() {
        ResultadoValidacion<Trabajo> r = TrabajoValidador.validar(formularioValido());

        assertTrue(r.esValido(), () -> "Errores inesperados: " + r.getErrores());
        assertTrue(r.getObjeto().isRestauracion());
        assertTrue(r.getObjeto().isEstado());
        assertNull(r.getObjeto().getIdServicio());
    }

    @Test
    @DisplayName("El tipo debe ser restauración o colaboración")
    void tipoInvalido() {
        Map<String, String> campos = formularioValido();
        campos.put("tipo", "otro");
        assertTrue(TrabajoValidador.validar(campos).getErrores().containsKey("tipo"));

        campos.remove("tipo");
        assertTrue(TrabajoValidador.validar(campos).getErrores().containsKey("tipo"));
    }

    @Test
    @DisplayName("El título es obligatorio y admite máximo 150 caracteres")
    void tituloObligatorio() {
        Map<String, String> campos = formularioValido();
        campos.put("titulo", "   ");
        assertTrue(TrabajoValidador.validar(campos).getErrores().containsKey("titulo"));

        campos.put("titulo", "x".repeat(151));
        assertTrue(TrabajoValidador.validar(campos).getErrores().containsKey("titulo"));
    }

    @Test
    @DisplayName("El servicio es opcional pero, si se envía, debe ser un id válido")
    void servicioOpcional() {
        Map<String, String> campos = formularioValido();
        campos.put("idServicio", "2");
        assertEquals(2, TrabajoValidador.validar(campos).getObjeto().getIdServicio());

        campos.put("idServicio", "abc");
        assertTrue(TrabajoValidador.validar(campos).getErrores().containsKey("idServicio"));
    }

    @Test
    @DisplayName("Una restauración sin foto del antes o del después no es válida (RF08)")
    void restauracionExigeAntesYDespues() {
        Map<String, String> errores = new LinkedHashMap<>();
        TrabajoValidador.validarAntesDespues(Trabajo.RESTAURACION, false, true, errores);
        assertTrue(errores.containsKey("imagenAntes"));
        assertFalse(errores.containsKey("imagenDespues"));

        errores.clear();
        TrabajoValidador.validarAntesDespues(Trabajo.RESTAURACION, true, false, errores);
        assertTrue(errores.containsKey("imagenDespues"));

        errores.clear();
        TrabajoValidador.validarAntesDespues(Trabajo.RESTAURACION, true, true, errores);
        assertTrue(errores.isEmpty());
    }

    @Test
    @DisplayName("Una colaboración no exige fotos de antes y después")
    void colaboracionSinAntesDespues() {
        Map<String, String> errores = new LinkedHashMap<>();
        TrabajoValidador.validarAntesDespues(Trabajo.COLABORACION, false, false, errores);
        assertTrue(errores.isEmpty());
    }

    @Test
    @DisplayName("No se pueden superar las fotos adicionales permitidas")
    void maximoDeFotos() {
        Map<String, String> errores = new LinkedHashMap<>();
        TrabajoValidador.validarCantidadFotos(6, 2, errores);
        assertTrue(errores.isEmpty());

        TrabajoValidador.validarCantidadFotos(6, 3, errores);
        assertTrue(errores.get("fotos").contains("Puede agregar 2 más"));
    }
}
