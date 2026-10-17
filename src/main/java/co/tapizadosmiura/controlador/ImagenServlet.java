package co.tapizadosmiura.controlador;

import co.tapizadosmiura.util.AlmacenImagenes;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sirve las imágenes subidas desde el panel: {@code GET /uploads/productos/archivo.jpg}.
 *
 * <p>Las fotos viven fuera del WAR (ver {@link AlmacenImagenes}). Si el archivo
 * no existe —por ejemplo, las rutas de los datos de prueba— se responde con una
 * imagen genérica para que la página no muestre un ícono roto.</p>
 */
@WebServlet(name = "ImagenServlet", urlPatterns = {"/uploads/*"})
public class ImagenServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String IMAGEN_GENERICA = "/img/sin-imagen.svg";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String rutaPedida = request.getPathInfo(); // ej. "/productos/00014-1.webp"
        Path base = AlmacenImagenes.directorioBase(getServletContext()).toAbsolutePath().normalize();

        if (rutaPedida != null
                && AlmacenImagenes.EXTENSIONES_PERMITIDAS.contains(AlmacenImagenes.extension(rutaPedida))) {
            Path archivo = base.resolve(rutaPedida.substring(1)).normalize();
            // startsWith impide salir de la carpeta con rutas como "/../../secreto"
            if (archivo.startsWith(base) && Files.isRegularFile(archivo)) {
                response.setContentType(getServletContext().getMimeType(archivo.getFileName().toString()));
                response.setContentLengthLong(Files.size(archivo));
                response.setHeader("Cache-Control", "public, max-age=86400");
                Files.copy(archivo, response.getOutputStream());
                return;
            }
        }
        response.sendRedirect(request.getContextPath() + IMAGEN_GENERICA);
    }
}
