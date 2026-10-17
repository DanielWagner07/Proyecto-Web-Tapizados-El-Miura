package co.tapizadosmiura.util;

import co.tapizadosmiura.modelo.Producto;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static co.tapizadosmiura.util.CamposFormulario.decimal;
import static co.tapizadosmiura.util.CamposFormulario.entero;
import static co.tapizadosmiura.util.CamposFormulario.texto;
import static co.tapizadosmiura.util.CamposFormulario.textoLimitado;

/**
 * Convierte los campos de texto del formulario de producto en un {@link Producto}
 * y valida las reglas de negocio de CU07 y las restricciones de la tabla.
 *
 * <p>No depende de la API de servlets: recibe un mapa nombre-del-campo → valor,
 * lo que permite probarla con JUnit sin un servidor.</p>
 */
public final class ProductoValidador {

    private static final Pattern PATRON_REFERENCIA = Pattern.compile("[A-Za-z0-9-]{1,20}");
    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("9999999999.99"); // DECIMAL(12,2)
    private static final BigDecimal MEDIDA_MAXIMA = new BigDecimal("99999.9");       // DECIMAL(6,1)
    private static final int RESISTENCIA_MAXIMA = 65535;                              // SMALLINT UNSIGNED
    private static final int DESCRIPCION_MAXIMA = 2000;

    private ProductoValidador() {
    }

    /**
     * Valida los datos enviados desde el formulario.
     *
     * @param campos valores del formulario; las claves son los atributos {@code name} de los inputs
     * @return producto armado (aunque tenga errores, para volver a llenar el formulario) y errores
     */
    public static ResultadoValidacion<Producto> validar(Map<String, String> campos) {
        Map<String, String> errores = new LinkedHashMap<>();
        Producto p = new Producto();

        // Obligatorios
        p.setReferencia(texto(campos, "referencia"));
        if (p.getReferencia() == null) {
            errores.put("referencia", "La referencia es obligatoria.");
        } else if (!PATRON_REFERENCIA.matcher(p.getReferencia()).matches()) {
            errores.put("referencia", "Use hasta 20 letras, números o guiones (ej. 00014).");
        }

        p.setNombre(texto(campos, "nombre"));
        if (p.getNombre() == null) {
            errores.put("nombre", "El nombre es obligatorio.");
        } else if (p.getNombre().length() > 150) {
            errores.put("nombre", "El nombre admite máximo 150 caracteres.");
        }

        Integer idCategoria = entero(campos, "idCategoria");
        if (idCategoria == null || idCategoria <= 0) {
            errores.put("idCategoria", "Seleccione una categoría.");
        } else {
            p.setIdCategoria(idCategoria);
        }

        BigDecimal precio = decimal(campos, "precio");
        if (precio == null) {
            errores.put("precio", "Ingrese un precio válido.");
        } else if (precio.signum() <= 0 || precio.compareTo(PRECIO_MAXIMO) > 0) {
            errores.put("precio", "El precio debe ser mayor que cero.");
        }
        p.setPrecio(precio);

        Integer stock = entero(campos, "stock");
        if (stock == null || stock < 0) {
            errores.put("stock", "El stock debe ser un número entero mayor o igual a cero.");
        } else {
            p.setStock(stock);
        }

        // Opcionales
        p.setDescripcion(textoLimitado(campos, "descripcion", DESCRIPCION_MAXIMA, errores));
        p.setEstilo(textoLimitado(campos, "estilo", 60, errores));
        p.setColor(textoLimitado(campos, "color", 60, errores));
        p.setMaterialTapiz(textoLimitado(campos, "materialTapiz", 80, errores));
        p.setMaterialEstructura(textoLimitado(campos, "materialEstructura", 80, errores));
        p.setTipoRelleno(textoLimitado(campos, "tipoRelleno", 80, errores));
        p.setTipoPata(textoLimitado(campos, "tipoPata", 80, errores));

        p.setAltoCm(medida(campos, "altoCm", errores));
        p.setAnchoCm(medida(campos, "anchoCm", errores));
        p.setProfundidadCm(medida(campos, "profundidadCm", errores));
        p.setPesoKg(medida(campos, "pesoKg", errores));

        if (texto(campos, "resistenciaKgPuesto") != null) {
            Integer resistencia = entero(campos, "resistenciaKgPuesto");
            if (resistencia == null || resistencia <= 0 || resistencia > RESISTENCIA_MAXIMA) {
                errores.put("resistenciaKgPuesto", "Ingrese un número entero mayor que cero.");
            } else {
                p.setResistenciaKgPuesto(resistencia);
            }
        }

        // Casillas de verificación: el navegador solo las envía si están marcadas
        p.setRequiereArmado(campos.containsKey("requiereArmado"));
        p.setEstado(campos.containsKey("estado"));

        return new ResultadoValidacion<>(p, errores);
    }

    /** Medida opcional: si se escribe, debe ser positiva (CHECK ck_producto_dimensiones). */
    private static BigDecimal medida(Map<String, String> campos, String nombre, Map<String, String> errores) {
        if (texto(campos, nombre) == null) {
            return null;
        }
        BigDecimal valor = decimal(campos, nombre);
        if (valor == null || valor.signum() <= 0 || valor.compareTo(MEDIDA_MAXIMA) > 0) {
            errores.put(nombre, "Debe ser un número mayor que cero.");
            return null;
        }
        return valor;
    }
}
