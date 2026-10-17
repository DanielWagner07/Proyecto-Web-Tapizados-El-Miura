package co.tapizadosmiura.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Entrega conexiones JDBC a la base de datos {@code tapizados_miura}.
 *
 * <p>Los datos de acceso se leen una sola vez del archivo
 * {@code src/main/resources/db.properties}, de modo que cambiar de usuario o
 * contraseña no exige tocar el código.</p>
 *
 * <p>Cada llamada a {@link #obtener()} abre una conexión nueva; quien la pide
 * debe cerrarla, idealmente con <em>try-with-resources</em>.</p>
 */
public final class Conexion {

    private static final String ARCHIVO_CONFIGURACION = "/db.properties";
    private static final Properties CONFIG = cargarConfiguracion();

    private Conexion() {
        // Clase utilitaria: no se instancia.
    }

    /**
     * Abre una conexión nueva con la base de datos.
     *
     * @return conexión lista para usar (debe cerrarse después)
     * @throws SQLException si la base de datos no está disponible o las credenciales son incorrectas
     */
    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(
                CONFIG.getProperty("db.url"),
                CONFIG.getProperty("db.usuario"),
                CONFIG.getProperty("db.contrasena", ""));
    }

    private static Properties cargarConfiguracion() {
        Properties propiedades = new Properties();
        try (InputStream entrada = Conexion.class.getResourceAsStream(ARCHIVO_CONFIGURACION)) {
            if (entrada == null) {
                throw new IllegalStateException("No se encontró " + ARCHIVO_CONFIGURACION + " en el classpath");
            }
            propiedades.load(entrada);
            // Tomcat no siempre registra solo el driver de WEB-INF/lib; se carga explícitamente.
            Class.forName(propiedades.getProperty("db.driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("No fue posible configurar la conexión a la base de datos", e);
        }
        return propiedades;
    }
}
