package co.tapizadosmiura.util;

import co.tapizadosmiura.modelo.Producto;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

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

    /** Resultado de la validación: el producto armado y los errores por campo. */
    public static final class Resultado {
        private final Producto producto;
        private final Map<String, String> errores;

        Resultado(Producto producto, Map<String, String> errores) {
            this.producto = producto;
            this.errores = errores;
        }

        public Producto getProducto() {
            return producto;
        }

        /** @return mensajes de error indexados por nombre del campo (vacío si todo es válido) */
        public Map<String, String> getErrores() {
            return errores;
        }

        public boolean esValido() {
            return errores.isEmpty();
        }
    }

    /**
     * Valida los datos enviados desde el formulario.
     *
     * @param campos valores del formulario; las claves son los atributos {@code name} de los inputs
     * @return producto (aunque tenga errores, para volver a llenar el formulario) y errores
     */
    public static Resultado validar(Map<String, String> campos) {
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
        p.setDescripcion(texto(campos, "descripcion"));
        if (p.getDescripcion() != null && p.getDescripcion().length() > DESCRIPCION_MAXIMA) {
            errores.put("descripcion", "La descripción admite máximo " + DESCRIPCION_MAXIMA + " caracteres.");
        }
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

        return new Resultado(p, errores);
    }

    // ------------------------------------------------------------------
    // Conversión de texto a tipos
    // ------------------------------------------------------------------

    /** Texto sin espacios sobrantes; {@code null} si viene vacío. */
    private static String texto(Map<String, String> campos, String nombre) {
        String valor = campos.get(nombre);
        if (valor == null) {
            return null;
        }
        valor = valor.trim();
        return valor.isEmpty() ? null : valor;
    }

    private static String textoLimitado(Map<String, String> campos, String nombre, int maximo,
                                        Map<String, String> errores) {
        String valor = texto(campos, nombre);
        if (valor != null && valor.length() > maximo) {
            errores.put(nombre, "Máximo " + maximo + " caracteres.");
        }
        return valor;
    }

    private static Integer entero(Map<String, String> campos, String nombre) {
        String valor = texto(campos, nombre);
        if (valor == null) {
            return null;
        }
        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Acepta coma o punto como separador decimal (ej. 62,2 o 62.2). */
    private static BigDecimal decimal(Map<String, String> campos, String nombre) {
        String valor = texto(campos, nombre);
        if (valor == null) {
            return null;
        }
        try {
            return new BigDecimal(valor.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
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
