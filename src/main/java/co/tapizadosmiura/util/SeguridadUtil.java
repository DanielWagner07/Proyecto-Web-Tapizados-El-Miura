package co.tapizadosmiura.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Operaciones de seguridad sobre contraseñas.
 */
public final class SeguridadUtil {

    private SeguridadUtil() {
    }

    /**
     * Compara una contraseña escrita por el usuario con el hash BCrypt guardado.
     *
     * <p>Los hashes de la BD usan el prefijo {@code $2y$} (formato de PHP);
     * la librería jBCrypt solo reconoce {@code $2a$}. Ambos prefijos producen
     * exactamente el mismo cálculo, así que se normaliza antes de comparar.</p>
     *
     * @param contrasena     contraseña en texto plano
     * @param hashAlmacenado hash guardado en {@code administrador.contrasena_hash}
     * @return true si la contraseña es correcta; false si no coincide o el hash es inválido
     */
    public static boolean verificarContrasena(String contrasena, String hashAlmacenado) {
        if (contrasena == null || hashAlmacenado == null || hashAlmacenado.length() < 7) {
            return false;
        }
        String hash = hashAlmacenado.startsWith("$2y$") ? "$2a$" + hashAlmacenado.substring(4) : hashAlmacenado;
        try {
            return BCrypt.checkpw(contrasena, hash);
        } catch (IllegalArgumentException e) {
            return false; // hash con formato no válido
        }
    }

    /**
     * Genera el hash BCrypt de una contraseña (costo 10), útil para crear administradores.
     *
     * @param contrasena contraseña en texto plano
     * @return hash listo para guardar en la BD
     */
    public static String generarHash(String contrasena) {
        return BCrypt.hashpw(contrasena, BCrypt.gensalt(10));
    }
}
