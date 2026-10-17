package co.tapizadosmiura.dao;

import co.tapizadosmiura.config.Conexion;
import co.tapizadosmiura.modelo.Administrador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acceso a datos de la tabla {@code administrador} (CU01 - Iniciar sesión).
 */
public class AdministradorDAO {

    private static final String SQL_BUSCAR_ACTIVO_POR_CORREO =
            "SELECT id_administrador, nombre, correo, contrasena_hash, estado "
            + "FROM administrador WHERE correo = ? AND estado = TRUE";

    private static final String SQL_REGISTRAR_ACCESO =
            "UPDATE administrador SET ultimo_acceso = NOW() WHERE id_administrador = ?";

    /**
     * Busca un administrador habilitado por su correo.
     *
     * @param correo correo con el que intenta ingresar
     * @return el administrador, o {@code null} si no existe o está deshabilitado
     * @throws SQLException si falla la consulta
     */
    public Administrador buscarActivoPorCorreo(String correo) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_ACTIVO_POR_CORREO)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Administrador admin = new Administrador();
                admin.setIdAdministrador(rs.getInt("id_administrador"));
                admin.setNombre(rs.getString("nombre"));
                admin.setCorreo(rs.getString("correo"));
                admin.setContrasenaHash(rs.getString("contrasena_hash"));
                admin.setEstado(rs.getBoolean("estado"));
                return admin;
            }
        }
    }

    /**
     * Guarda la fecha y hora del último ingreso exitoso.
     *
     * @param idAdministrador administrador que acaba de ingresar
     * @throws SQLException si falla la actualización
     */
    public void registrarAcceso(int idAdministrador) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(SQL_REGISTRAR_ACCESO)) {
            ps.setInt(1, idAdministrador);
            ps.executeUpdate();
        }
    }
}
