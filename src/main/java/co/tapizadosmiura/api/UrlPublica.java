package co.tapizadosmiura.api;

/**
 * Arma las direcciones de las imágenes que devuelve la API.
 *
 * <p>La BD guarda rutas relativas ({@code uploads/productos/x.webp}). La API las
 * entrega completas dentro de la aplicación ({@code /tapizados-miura/uploads/productos/x.webp})
 * para que el front-end las use directamente en un {@code <img src>} desde
 * cualquier página. Si no hay foto se entrega la imagen genérica.</p>
 */
public final class UrlPublica {

    /** Imagen que se muestra cuando un registro no tiene foto. */
    public static final String IMAGEN_GENERICA = "img/sin-imagen.svg";

    private UrlPublica() {
    }

    /**
     * @param contexto ruta de la aplicación, ej. "/tapizados-miura" (vacía si está en la raíz)
     * @param ruta     ruta guardada en la BD; null o vacía = sin foto
     * @return dirección lista para el navegador
     */
    public static String imagen(String contexto, String ruta) {
        String relativa = ruta == null || ruta.isBlank() ? IMAGEN_GENERICA : ruta;
        return contexto + "/" + (relativa.startsWith("/") ? relativa.substring(1) : relativa);
    }

    /** Igual que {@link #imagen}, pero devuelve null si no hay foto (campos opcionales como "antes"). */
    public static String imagenOpcional(String contexto, String ruta) {
        return ruta == null || ruta.isBlank() ? null : imagen(contexto, ruta);
    }
}
