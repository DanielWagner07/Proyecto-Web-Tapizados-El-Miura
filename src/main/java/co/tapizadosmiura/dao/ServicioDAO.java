package co.tapizadosmiura.dao;

import co.tapizadosmiura.config.Conexion;
import co.tapizadosmiura.modelo.Servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code servicio}.
 */
public class ServicioDAO {

    private static final String SQL_LISTAR_ACTIVOS =
            "SELECT id_servicio, nombre, descripcion FROM servicio WHERE estado = TRUE ORDER BY orden, nombre";

    /**
     * Servicios activos en el orden en que se muestran en la web.
     *
     * @return lista de servicios (vacía si no hay)
     * @throws SQLException si falla la consulta
     */
    public List<Servicio> listarActivos() throws SQLException {
        List<Servicio> servicios = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_ACTIVOS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Servicio servicio = new Servicio();
                servicio.setIdServicio(rs.getInt("id_servicio"));
                servicio.setNombre(rs.getString("nombre"));
                servicio.setDescripcion(rs.getString("descripcion"));
                servicios.add(servicio);
            }
        }
        return servicios;
    }
}
