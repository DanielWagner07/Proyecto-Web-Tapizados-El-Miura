package co.tapizadosmiura.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Guarda y ubica las imágenes subidas desde el panel administrador.
 *
 * <p>Las fotos se guardan fuera del WAR, en la carpeta configurada en
 * {@code web.xml} (parámetro {@code miura.uploads.dir}). En la base de datos
 * solo se registra la ruta relativa, por ejemplo {@code uploads/productos/00014-1712345.jpg},
 * y {@link co.tapizadosmiura.controlador.ImagenServlet} la sirve al navegador.</p>
 */
public final class AlmacenImagenes {

    /** Formatos permitidos por CU07 y por el CHECK ck_producto_imagen_ruta. */
    public static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "webp");

    /** Tamaño máximo por imagen: 5 MB. */
    public static final long TAMANO_MAXIMO_BYTES = 5L * 1024 * 1024;

    private static final String PREFIJO_RUTA = "uploads/";
    private static final Logger LOG = Logger.getLogger(AlmacenImagenes.class.getName());

    private AlmacenImagenes() {
    }

    /**
     * Carpeta raíz donde viven las imágenes subidas.
     *
     * @param contexto contexto de la aplicación (lee el parámetro de web.xml)
     * @return ruta absoluta de la carpeta
     */
    public static Path directorioBase(ServletContext contexto) {
        String configurado = contexto.getInitParameter("miura.uploads.dir");
        if (configurado == null || configurado.isBlank()) {
            return Paths.get(System.getProperty("user.home"), "miura-uploads");
        }
        return Paths.get(configurado.trim());
    }

    /**
     * Extensión en minúsculas de un nombre de archivo, o cadena vacía si no tiene.
     *
     * @param nombreArchivo nombre original del archivo
     * @return extensión sin el punto
     */
    public static String extension(String nombreArchivo) {
        if (nombreArchivo == null) {
            return "";
        }
        int punto = nombreArchivo.lastIndexOf('.');
        return punto < 0 ? "" : nombreArchivo.substring(punto + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * Revisa que el archivo enviado sea una imagen aceptada.
     *
     * @param parte archivo recibido en el formulario (puede ser null)
     * @return mensaje de error, o {@code null} si el archivo es válido
     */
    public static String validar(Part parte) {
        if (parte == null || parte.getSize() == 0) {
            return "Seleccione una imagen.";
        }
        if (!EXTENSIONES_PERMITIDAS.contains(extension(parte.getSubmittedFileName()))) {
            return "Formato no permitido. Use JPG, PNG o WEBP.";
        }
        String tipo = parte.getContentType();
        if (tipo == null || !tipo.startsWith("image/")) {
            return "El archivo no es una imagen.";
        }
        if (parte.getSize() > TAMANO_MAXIMO_BYTES) {
            return "La imagen supera el tamaño máximo de 5 MB.";
        }
        return null;
    }

    /**
     * Guarda una imagen ya validada con un nombre único.
     *
     * @param contexto   contexto de la aplicación
     * @param parte      archivo recibido
     * @param subcarpeta carpeta lógica, por ejemplo "productos" o "trabajos"
     * @param prefijo    texto inicial del nombre (ej. la referencia del producto)
     * @return ruta relativa para guardar en la BD, ej. {@code uploads/productos/00014-1712345.jpg}
     * @throws IOException si no se puede escribir el archivo
     */
    public static String guardar(ServletContext contexto, Part parte, String subcarpeta, String prefijo)
            throws IOException {
        // Sufijo aleatorio: varias fotos enviadas juntas pueden caer en el mismo milisegundo
        String nombreSeguro = prefijo.replaceAll("[^A-Za-z0-9-]", "")
                + "-" + System.currentTimeMillis()
                + "-" + UUID.randomUUID().toString().substring(0, 8)
                + "." + extension(parte.getSubmittedFileName());
        Path carpeta = directorioBase(contexto).resolve(subcarpeta);
        Files.createDirectories(carpeta);
        try (InputStream entrada = parte.getInputStream()) {
            Files.copy(entrada, carpeta.resolve(nombreSeguro), StandardCopyOption.REPLACE_EXISTING);
        }
        return PREFIJO_RUTA + subcarpeta + "/" + nombreSeguro;
    }

    /**
     * Borra del disco una imagen que ya no se usa. Si el archivo no existe o no
     * se puede borrar, solo se registra en el log: no debe impedir la operación.
     *
     * @param contexto contexto de la aplicación
     * @param ruta     ruta relativa guardada en la BD (ej. uploads/trabajos/x.jpg); se ignoran las demás
     */
    public static void eliminar(ServletContext contexto, String ruta) {
        if (ruta == null || !ruta.startsWith(PREFIJO_RUTA)) {
            return;
        }
        Path base = directorioBase(contexto).toAbsolutePath().normalize();
        Path archivo = base.resolve(ruta.substring(PREFIJO_RUTA.length())).normalize();
        if (!archivo.startsWith(base)) {
            return; // ruta manipulada: fuera de la carpeta de imágenes
        }
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "No se pudo borrar la imagen " + archivo, e);
        }
    }
}
