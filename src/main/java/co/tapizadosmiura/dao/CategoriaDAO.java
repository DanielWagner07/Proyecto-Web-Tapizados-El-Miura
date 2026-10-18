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

    /** Categorías visibles con la cantidad de productos visibles de cada una (RF01). */
    private static final String SQL_LISTAR_PUBLICAS =
            "SELECT c.id_categoria, c.nombre, c.descripcion, c.estado, "
            + "(SELECT COUNT(*) FROM producto p WHERE p.id_categoria = c.id_categoria AND p.estado = TRUE) "
            + "AS total_productos "
            + "FROM categoria c WHERE c.estado = TRUE ORDER BY c.nombre";

    private static final String SQL_EXISTE_ACTIVA =
            "SELECT 1 FROM categoria WHERE id_categoria = ? AND estado = TRUE";

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
                categorias.add(mapear(rs));
            }
        }
        return categorias;
    }

    /**
     * Categorías visibles para el sitio público, con su total de productos visibles.
     *
     * @return lista ordenada por nombre
     * @throws SQLException si falla la consulta
     */
    public List<Categoria> listarPublicas() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_PUBLICAS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria categoria = mapear(rs);
                categoria.setTotalProductos(rs.getInt("total_productos"));
                categorias.add(categoria);
            }
        }
        return categorias;
    }

    /**
     * Indica si existe una categoría activa con ese id (RF07: la categoría debe existir).
     *
     * @param idCategoria identificador a verificar
     * @return true si existe y está activa
     * @throws SQLException si falla la consulta
     */
    public boolean existeActiva(int idCategoria) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(SQL_EXISTE_ACTIVA)) {
            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(rs.getInt("id_categoria"));
        categoria.setNombre(rs.getString("nombre"));
        categoria.setDescripcion(rs.getString("descripcion"));
        categoria.setEstado(rs.getBoolean("estado"));
        return categoria;
    }
}
