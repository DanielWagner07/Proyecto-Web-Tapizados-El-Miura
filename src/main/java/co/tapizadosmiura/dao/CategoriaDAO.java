package co.tapizadosmiura.dao;

import co.tapizadosmiura.config.Conexion;
import co.tapizadosmiura.modelo.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code categoria}.
 */
public class CategoriaDAO {

    private static final String SQL_LISTAR_ACTIVAS =
            "SELECT id_categoria, nombre, descripcion, estado FROM categoria WHERE estado = TRUE ORDER BY nombre";

    /**
     * Categorías visibles, ordenadas por nombre. Alimenta las listas desplegables del panel.
     *
     * @return lista de categorías activas (vacía si no hay)
     * @throws SQLException si falla la consulta
     */
    public List<Categoria> listarActivas() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_ACTIVAS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(rs.getInt("id_categoria"));
                categoria.setNombre(rs.getString("nombre"));
                categoria.setDescripcion(rs.getString("descripcion"));
                categoria.setEstado(rs.getBoolean("estado"));
                categorias.add(categoria);
            }
        }
        return categorias;
    }
}
