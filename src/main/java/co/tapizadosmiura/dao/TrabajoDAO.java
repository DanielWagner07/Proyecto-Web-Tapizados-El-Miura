package co.tapizadosmiura.dao;

import co.tapizadosmiura.config.Conexion;
import co.tapizadosmiura.modelo.Trabajo;
import co.tapizadosmiura.modelo.TrabajoImagen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de las tablas {@code trabajo} y {@code trabajo_imagen}
 * (CU08 - Gestionar trabajos realizados).
 *
 * <p>A diferencia de los productos, un trabajo no tiene registros que dependan
 * de él (sus fotos se borran en cascada), así que se puede eliminar. También
 * se puede ocultar sin borrarlo con {@link #cambiarEstado(int, boolean)}.</p>
 */
public class TrabajoDAO {

    private static final String SQL_SELECT_BASE =
            "SELECT t.*, s.nombre AS nombre_servicio, "
            + "(SELECT COUNT(*) FROM trabajo_imagen ti WHERE ti.id_trabajo = t.id_trabajo) AS total_fotos, "
            + "(SELECT ti.ruta FROM trabajo_imagen ti WHERE ti.id_trabajo = t.id_trabajo "
            + " ORDER BY ti.orden LIMIT 1) AS primera_foto "
            + "FROM trabajo t LEFT JOIN servicio s ON s.id_servicio = t.id_servicio ";

    private static final String SQL_LISTAR =
            SQL_SELECT_BASE + "WHERE (? = '' OR t.tipo = ?) ORDER BY t.estado DESC, t.creado_en DESC, t.id_trabajo DESC";

    private static final String SQL_BUSCAR_POR_ID = SQL_SELECT_BASE + "WHERE t.id_trabajo = ?";

    private static final String SQL_FOTOS =
            "SELECT id_trabajo_imagen, id_trabajo, ruta, texto_alternativo, orden "
            + "FROM trabajo_imagen WHERE id_trabajo = ? ORDER BY orden";

    private static final String SQL_INSERTAR =
            "INSERT INTO trabajo (id_servicio, tipo, titulo, descripcion, cliente, imagen_antes, imagen_despues, estado) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE trabajo SET id_servicio = ?, tipo = ?, titulo = ?, descripcion = ?, cliente = ?, "
            + "imagen_antes = ?, imagen_despues = ?, estado = ? WHERE id_trabajo = ?";

    private static final String SQL_INSERTAR_FOTO =
            "INSERT INTO trabajo_imagen (id_trabajo, ruta, texto_alternativo, orden) "
            + "SELECT ?, ?, ?, COALESCE(MAX(orden) + 1, 0) FROM trabajo_imagen WHERE id_trabajo = ?";

    private static final String SQL_CAMBIAR_ESTADO = "UPDATE trabajo SET estado = ? WHERE id_trabajo = ?";

    private static final String SQL_RUTAS_TRABAJO =
            "SELECT imagen_antes AS ruta FROM trabajo WHERE id_trabajo = ? "
            + "UNION ALL SELECT imagen_despues FROM trabajo WHERE id_trabajo = ? "
            + "UNION ALL SELECT ruta FROM trabajo_imagen WHERE id_trabajo = ?";

    private static final String SQL_ELIMINAR = "DELETE FROM trabajo WHERE id_trabajo = ?";

    private static final String SQL_RUTA_FOTO =
            "SELECT ruta FROM trabajo_imagen WHERE id_trabajo_imagen = ? AND id_trabajo = ?";

    private static final String SQL_ELIMINAR_FOTO =
            "DELETE FROM trabajo_imagen WHERE id_trabajo_imagen = ? AND id_trabajo = ?";

    /**
     * Lista los trabajos para el panel.
     *
     * @param tipo "restauracion", "colaboracion" o vacío para todos
     * @return trabajos ordenados: primero los visibles, luego los más recientes
     * @throws SQLException si falla la consulta
     */
    public List<Trabajo> listar(String tipo) throws SQLException {
        String filtro = tipo == null ? "" : tipo;
        List<Trabajo> trabajos = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR)) {
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trabajos.add(mapear(rs));
                }
            }
        }
        return trabajos;
    }

    /**
     * Busca un trabajo con todas sus fotos adicionales.
     *
     * @param idTrabajo identificador del trabajo
     * @return el trabajo, o {@code null} si no existe
     * @throws SQLException si falla la consulta
     */
    public Trabajo buscarPorId(int idTrabajo) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            Trabajo trabajo;
            try (PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {
                ps.setInt(1, idTrabajo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    trabajo = mapear(rs);
                }
            }
            try (PreparedStatement ps = con.prepareStatement(SQL_FOTOS)) {
                ps.setInt(1, idTrabajo);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        TrabajoImagen foto = new TrabajoImagen();
                        foto.setIdTrabajoImagen(rs.getInt("id_trabajo_imagen"));
                        foto.setIdTrabajo(rs.getInt("id_trabajo"));
                        foto.setRuta(rs.getString("ruta"));
                        foto.setTextoAlternativo(rs.getString("texto_alternativo"));
                        foto.setOrden(rs.getInt("orden"));
                        trabajo.getFotos().add(foto);
                    }
                }
            }
            return trabajo;
        }
    }

    /**
     * Registra un trabajo y sus fotos adicionales en una sola transacción.
     *
     * @param trabajo   datos validados (con las rutas de antes/después ya guardadas)
     * @param rutasFotos rutas de las fotos adicionales (puede estar vacía)
     * @return id generado
     * @throws SQLException si falla la operación (se revierte todo)
     */
    public int crear(Trabajo trabajo, List<String> rutasFotos) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
                    asignarCampos(ps, trabajo);
                    ps.executeUpdate();
                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        claves.next();
                        id = claves.getInt(1);
                    }
                }
                insertarFotos(con, id, trabajo.getTitulo(), rutasFotos);
                con.commit();
                return id;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Actualiza un trabajo y agrega fotos adicionales nuevas (las existentes se conservan).
     *
     * @param trabajo         datos validados (debe traer idTrabajo y las rutas finales de antes/después)
     * @param rutasFotosNuevas fotos adicionales para agregar al final
     * @return true si el trabajo existía
     * @throws SQLException si falla la operación (se revierte todo)
     */
    public boolean actualizar(Trabajo trabajo, List<String> rutasFotosNuevas) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                int filas;
                try (PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
                    int siguiente = asignarCampos(ps, trabajo);
                    ps.setInt(siguiente, trabajo.getIdTrabajo());
                    filas = ps.executeUpdate();
                }
                if (filas > 0) {
                    insertarFotos(con, trabajo.getIdTrabajo(), trabajo.getTitulo(), rutasFotosNuevas);
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
     * Muestra u oculta un trabajo en la galería pública.
     *
     * @return true si el trabajo existe
     * @throws SQLException si falla la actualización
     */
    public boolean cambiarEstado(int idTrabajo, boolean visible) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(SQL_CAMBIAR_ESTADO)) {
            ps.setBoolean(1, visible);
            ps.setInt(2, idTrabajo);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Elimina un trabajo y sus fotos adicionales (ON DELETE CASCADE).
     *
     * @param idTrabajo trabajo a eliminar
     * @return rutas de todas sus imágenes (para borrar los archivos), o {@code null} si el trabajo no existía
     * @throws SQLException si falla la operación
     */
    public List<String> eliminar(int idTrabajo) throws SQLException {
        List<String> rutas = new ArrayList<>();
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(SQL_RUTAS_TRABAJO)) {
                    ps.setInt(1, idTrabajo);
                    ps.setInt(2, idTrabajo);
                    ps.setInt(3, idTrabajo);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            if (rs.getString("ruta") != null) {
                                rutas.add(rs.getString("ruta"));
                            }
                        }
                    }
                }
                int filas;
                try (PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
                    ps.setInt(1, idTrabajo);
                    filas = ps.executeUpdate();
                }
                con.commit();
                return filas > 0 ? rutas : null;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Quita una foto adicional de un trabajo.
     *
     * @param idTrabajo       trabajo dueño de la foto (evita borrar fotos de otro trabajo)
     * @param idTrabajoImagen foto a quitar
     * @return ruta de la foto eliminada, o {@code null} si no existía
     * @throws SQLException si falla la operación
     */
    public String eliminarFoto(int idTrabajo, int idTrabajoImagen) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            String ruta = null;
            try (PreparedStatement ps = con.prepareStatement(SQL_RUTA_FOTO)) {
                ps.setInt(1, idTrabajoImagen);
                ps.setInt(2, idTrabajo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ruta = rs.getString("ruta");
                    }
                }
            }
            if (ruta != null) {
                try (PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR_FOTO)) {
                    ps.setInt(1, idTrabajoImagen);
                    ps.setInt(2, idTrabajo);
                    ps.executeUpdate();
                }
            }
            return ruta;
        }
    }

    // ------------------------------------------------------------------
    // Métodos de apoyo
    // ------------------------------------------------------------------

    /** Inserta las fotos al final del orden actual del trabajo. */
    private void insertarFotos(Connection con, int idTrabajo, String titulo, List<String> rutas) throws SQLException {
        if (rutas == null || rutas.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR_FOTO)) {
            for (String ruta : rutas) {
                ps.setInt(1, idTrabajo);
                ps.setString(2, ruta);
                ps.setString(3, titulo);
                ps.setInt(4, idTrabajo);
                ps.executeUpdate(); // una por una: cada INSERT calcula el siguiente "orden"
            }
        }
    }

    /** @return índice del siguiente parámetro libre */
    private int asignarCampos(PreparedStatement ps, Trabajo t) throws SQLException {
        int i = 1;
        if (t.getIdServicio() == null) {
            ps.setNull(i++, Types.INTEGER);
        } else {
            ps.setInt(i++, t.getIdServicio());
        }
        ps.setString(i++, t.getTipo());
        ps.setString(i++, t.getTitulo());
        ps.setString(i++, t.getDescripcion());
        ps.setString(i++, t.getCliente());
        ps.setString(i++, t.getImagenAntes());
        ps.setString(i++, t.getImagenDespues());
        ps.setBoolean(i++, t.isEstado());
        return i;
    }

    private Trabajo mapear(ResultSet rs) throws SQLException {
        Trabajo t = new Trabajo();
        t.setIdTrabajo(rs.getInt("id_trabajo"));
        int idServicio = rs.getInt("id_servicio");
        t.setIdServicio(rs.wasNull() ? null : idServicio);
        t.setTipo(rs.getString("tipo"));
        t.setTitulo(rs.getString("titulo"));
        t.setDescripcion(rs.getString("descripcion"));
        t.setCliente(rs.getString("cliente"));
        t.setImagenAntes(rs.getString("imagen_antes"));
        t.setImagenDespues(rs.getString("imagen_despues"));
        t.setEstado(rs.getBoolean("estado"));
        t.setNombreServicio(rs.getString("nombre_servicio"));
        t.setTotalFotos(rs.getInt("total_fotos"));
        t.setPrimeraFoto(rs.getString("primera_foto"));
        return t;
    }
}
