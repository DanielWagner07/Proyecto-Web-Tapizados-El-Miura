package co.tapizadosmiura.dao;

import co.tapizadosmiura.config.Conexion;
import co.tapizadosmiura.modelo.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de las tablas {@code producto} y {@code producto_imagen}
 * (CU07 - Gestionar catálogo).
 *
 * <p>Los productos no se eliminan físicamente: un producto que ya aparece en
 * una cotización no puede borrarse (FK RESTRICT), así que se desactiva con
 * {@link #cambiarEstado(int, boolean)} y deja de verse en el sitio público.</p>
 */
public class ProductoDAO {

    /** Columnas de la tabla en el orden usado por INSERT y UPDATE. */
    private static final String COLUMNAS_EDITABLES =
            "id_categoria, referencia, nombre, descripcion, precio, stock, estilo, color, "
            + "material_tapiz, material_estructura, tipo_relleno, tipo_pata, alto_cm, ancho_cm, "
            + "profundidad_cm, peso_kg, resistencia_kg_puesto, requiere_armado, estado";

    private static final String SQL_SELECT_BASE =
            "SELECT p.*, c.nombre AS nombre_categoria, "
            + "(SELECT pi.ruta FROM producto_imagen pi WHERE pi.id_producto = p.id_producto "
            + " ORDER BY pi.orden LIMIT 1) AS imagen_principal "
            + "FROM producto p JOIN categoria c ON c.id_categoria = p.id_categoria ";

    private static final String SQL_LISTAR =
            SQL_SELECT_BASE
            + "WHERE (? = '' OR p.nombre LIKE ? OR p.referencia LIKE ?) "
            + "ORDER BY p.estado DESC, p.nombre";

    private static final String SQL_BUSCAR_POR_ID = SQL_SELECT_BASE + "WHERE p.id_producto = ?";

    private static final String SQL_INSERTAR =
            "INSERT INTO producto (" + COLUMNAS_EDITABLES + ") "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE producto SET id_categoria = ?, referencia = ?, nombre = ?, descripcion = ?, "
            + "precio = ?, stock = ?, estilo = ?, color = ?, material_tapiz = ?, "
            + "material_estructura = ?, tipo_relleno = ?, tipo_pata = ?, alto_cm = ?, ancho_cm = ?, "
            + "profundidad_cm = ?, peso_kg = ?, resistencia_kg_puesto = ?, requiere_armado = ?, "
            + "estado = ? WHERE id_producto = ?";

    private static final String SQL_CAMBIAR_ESTADO =
            "UPDATE producto SET estado = ? WHERE id_producto = ?";

    private static final String SQL_EXISTE_REFERENCIA =
            "SELECT 1 FROM producto WHERE referencia = ? AND id_producto <> ?";

    private static final String SQL_INSERTAR_IMAGEN =
            "INSERT INTO producto_imagen (id_producto, ruta, texto_alternativo, orden) VALUES (?, ?, ?, 0)";

    private static final String SQL_ACTUALIZAR_IMAGEN_PRINCIPAL =
            "UPDATE producto_imagen SET ruta = ?, texto_alternativo = ? WHERE id_producto = ? AND orden = 0";

    /**
     * Lista todos los productos (activos e inactivos) para el panel.
     *
     * @param busqueda texto a buscar en nombre o referencia; vacío o null = todos
     * @return productos ordenados: primero los activos, luego por nombre
     * @throws SQLException si falla la consulta
     */
    public List<Producto> listar(String busqueda) throws SQLException {
        String texto = busqueda == null ? "" : busqueda.trim();
        String patron = "%" + texto + "%";
        List<Producto> productos = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR)) {
            ps.setString(1, texto);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapear(rs));
                }
            }
        }
        return productos;
    }

    /**
     * Busca un producto por su identificador.
     *
     * @param idProducto identificador del producto
     * @return el producto o {@code null} si no existe
     * @throws SQLException si falla la consulta
     */
    public Producto buscarPorId(int idProducto) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Indica si otra fila ya usa la referencia (la columna es UNIQUE).
     *
     * @param referencia     referencia a verificar
     * @param idProductoPropio id del producto que se edita (0 al crear) para no compararlo consigo mismo
     * @return true si la referencia ya está ocupada por otro producto
     * @throws SQLException si falla la consulta
     */
    public boolean existeReferencia(String referencia, int idProductoPropio) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(SQL_EXISTE_REFERENCIA)) {
            ps.setString(1, referencia);
            ps.setInt(2, idProductoPropio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Registra un producto con su imagen principal en una sola transacción:
     * si falla cualquiera de los dos INSERT no queda nada a medias.
     *
     * @param producto   datos validados del producto
     * @param rutaImagen ruta relativa de la imagen ya guardada (ej. uploads/productos/x.jpg)
     * @return id generado para el producto
     * @throws SQLException si falla la operación (se revierte todo)
     */
    public int crear(Producto producto, String rutaImagen) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                int idGenerado;
                try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
                    asignarCamposEditables(ps, producto);
                    ps.executeUpdate();
                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        claves.next();
                        idGenerado = claves.getInt(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR_IMAGEN)) {
                    ps.setInt(1, idGenerado);
                    ps.setString(2, rutaImagen);
                    ps.setString(3, producto.getNombre());
                    ps.executeUpdate();
                }
                con.commit();
                return idGenerado;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Actualiza los datos de un producto y, si se envió una foto nueva, reemplaza la principal.
     *
     * @param producto        datos validados (debe traer idProducto)
     * @param rutaImagenNueva ruta de la nueva imagen, o {@code null} para conservar la actual
     * @return true si el producto existía y se actualizó
     * @throws SQLException si falla la operación (se revierte todo)
     */
    public boolean actualizar(Producto producto, String rutaImagenNueva) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                int filas;
                try (PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
                    int siguiente = asignarCamposEditables(ps, producto);
                    ps.setInt(siguiente, producto.getIdProducto());
                    filas = ps.executeUpdate();
                }
                if (filas > 0 && rutaImagenNueva != null) {
                    reemplazarImagenPrincipal(con, producto, rutaImagenNueva);
                }
                con.commit();
                return filas > 0;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Activa o desactiva un producto (borrado lógico).
     *
     * @param idProducto producto a modificar
     * @param activo     true = visible en el sitio, false = oculto
     * @return true si el producto existe
     * @throws SQLException si falla la actualización
     */
    public boolean cambiarEstado(int idProducto, boolean activo) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(SQL_CAMBIAR_ESTADO)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idProducto);
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------------------
    // Métodos de apoyo
    // ------------------------------------------------------------------

    private void reemplazarImagenPrincipal(Connection con, Producto producto, String ruta) throws SQLException {
        int filas;
        try (PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR_IMAGEN_PRINCIPAL)) {
            ps.setString(1, ruta);
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getIdProducto());
            filas = ps.executeUpdate();
        }
        if (filas == 0) { // el producto no tenía imagen principal todavía
            try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR_IMAGEN)) {
                ps.setInt(1, producto.getIdProducto());
                ps.setString(2, ruta);
                ps.setString(3, producto.getNombre());
                ps.executeUpdate();
            }
        }
    }

    /**
     * Asigna los parámetros en el orden de {@link #COLUMNAS_EDITABLES}.
     *
     * @return índice del siguiente parámetro libre
     */
    private int asignarCamposEditables(PreparedStatement ps, Producto p) throws SQLException {
        int i = 1;
        ps.setInt(i++, p.getIdCategoria());
        ps.setString(i++, p.getReferencia());
        ps.setString(i++, p.getNombre());
        ps.setString(i++, p.getDescripcion());
        ps.setBigDecimal(i++, p.getPrecio());
        ps.setInt(i++, p.getStock());
        ps.setString(i++, p.getEstilo());
        ps.setString(i++, p.getColor());
        ps.setString(i++, p.getMaterialTapiz());
        ps.setString(i++, p.getMaterialEstructura());
        ps.setString(i++, p.getTipoRelleno());
        ps.setString(i++, p.getTipoPata());
        ps.setBigDecimal(i++, p.getAltoCm());
        ps.setBigDecimal(i++, p.getAnchoCm());
        ps.setBigDecimal(i++, p.getProfundidadCm());
        ps.setBigDecimal(i++, p.getPesoKg());
        if (p.getResistenciaKgPuesto() == null) {
            ps.setNull(i++, Types.SMALLINT);
        } else {
            ps.setInt(i++, p.getResistenciaKgPuesto());
        }
        ps.setBoolean(i++, p.isRequiereArmado());
        ps.setBoolean(i++, p.isEstado());
        return i;
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setIdCategoria(rs.getInt("id_categoria"));
        p.setReferencia(rs.getString("referencia"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setStock(rs.getInt("stock"));
        p.setEstilo(rs.getString("estilo"));
        p.setColor(rs.getString("color"));
        p.setMaterialTapiz(rs.getString("material_tapiz"));
        p.setMaterialEstructura(rs.getString("material_estructura"));
        p.setTipoRelleno(rs.getString("tipo_relleno"));
        p.setTipoPata(rs.getString("tipo_pata"));
        p.setAltoCm(rs.getBigDecimal("alto_cm"));
        p.setAnchoCm(rs.getBigDecimal("ancho_cm"));
        p.setProfundidadCm(rs.getBigDecimal("profundidad_cm"));
        p.setPesoKg(rs.getBigDecimal("peso_kg"));
        int resistencia = rs.getInt("resistencia_kg_puesto");
        p.setResistenciaKgPuesto(rs.wasNull() ? null : resistencia);
        p.setRequiereArmado(rs.getBoolean("requiere_armado"));
        p.setEstado(rs.getBoolean("estado"));
        p.setNombreCategoria(rs.getString("nombre_categoria"));
        p.setImagenPrincipal(rs.getString("imagen_principal"));
        return p;
    }
}
