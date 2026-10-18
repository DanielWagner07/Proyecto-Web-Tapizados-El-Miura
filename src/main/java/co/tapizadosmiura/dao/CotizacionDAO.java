package co.tapizadosmiura.dao;

import co.tapizadosmiura.config.Conexion;
import co.tapizadosmiura.modelo.Cotizacion;
import co.tapizadosmiura.modelo.DetalleCotizacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de las tablas {@code cotizacion} y {@code detalle_cotizacion}
 * (RF03 / RF04 - Generar cotización y enviarla por WhatsApp).
 *
 * <p>El total no se guarda: se suma desde el detalle, donde {@code subtotal}
 * es una columna generada (cantidad × precio unitario).</p>
 */
public class CotizacionDAO {

    private static final String SQL_INSERTAR = "INSERT INTO cotizacion (estado) VALUES ('pendiente')";

    private static final String SQL_INSERTAR_DETALLE =
            "INSERT INTO detalle_cotizacion (id_cotizacion, id_producto, nombre_producto, cantidad, precio_unitario) "
            + "VALUES (?, ?, ?, ?, ?)";

    /** Cotizaciones con sus totales calculados; el filtro por estado es opcional. */
    private static final String SQL_LISTAR =
            "SELECT co.id_cotizacion, co.estado, co.nota_interna, co.creado_en, co.actualizado_en, "
            + "COUNT(d.id_detalle_cotizacion) AS total_items, "
            + "COALESCE(SUM(d.cantidad), 0) AS total_unidades, "
            + "COALESCE(SUM(d.subtotal), 0) AS total "
            + "FROM cotizacion co LEFT JOIN detalle_cotizacion d ON d.id_cotizacion = co.id_cotizacion "
            + "WHERE (? = '' OR co.estado = ?) "
            + "GROUP BY co.id_cotizacion, co.estado, co.nota_interna, co.creado_en, co.actualizado_en "
            + "ORDER BY co.creado_en DESC, co.id_cotizacion DESC";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT co.id_cotizacion, co.estado, co.nota_interna, co.creado_en, co.actualizado_en, "
            + "COUNT(d.id_detalle_cotizacion) AS total_items, "
            + "COALESCE(SUM(d.cantidad), 0) AS total_unidades, "
            + "COALESCE(SUM(d.subtotal), 0) AS total "
            + "FROM cotizacion co LEFT JOIN detalle_cotizacion d ON d.id_cotizacion = co.id_cotizacion "
            + "WHERE co.id_cotizacion = ? "
            + "GROUP BY co.id_cotizacion, co.estado, co.nota_interna, co.creado_en, co.actualizado_en";

    private static final String SQL_DETALLES =
            "SELECT id_detalle_cotizacion, id_cotizacion, id_producto, nombre_producto, cantidad, precio_unitario "
            + "FROM detalle_cotizacion WHERE id_cotizacion = ? ORDER BY id_detalle_cotizacion";

    private static final String SQL_CAMBIAR_ESTADO =
            "UPDATE cotizacion SET estado = ? WHERE id_cotizacion = ?";

    private static final String SQL_CAMBIAR_ESTADO_Y_NOTA =
            "UPDATE cotizacion SET estado = ?, nota_interna = ? WHERE id_cotizacion = ?";

    /**
     * Registra una cotización con todos sus productos en una sola transacción:
     * o se guarda completa o no se guarda nada.
     *
     * @param detalles productos con nombre y precio ya tomados de la BD
     * @return id generado (es el número de cotización que ve el cliente)
     * @throws SQLException si falla la operación (se revierte todo)
     */
    public int crear(List<DetalleCotizacion> detalles) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
                    ps.executeUpdate();
                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        claves.next();
                        id = claves.getInt(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR_DETALLE)) {
                    for (DetalleCotizacion d : detalles) {
                        ps.setInt(1, id);
                        ps.setInt(2, d.getIdProducto());
                        ps.setString(3, d.getNombreProducto());
                        ps.setInt(4, d.getCantidad());
                        ps.setBigDecimal(5, d.getPrecioUnitario());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                con.commit();
                return id;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Cotizaciones para el administrador, las más recientes primero.
     *
     * @param estado estado a filtrar ("pendiente", "atendida"...) o vacío para todas
     * @return cotizaciones con sus totales (sin el detalle)
     * @throws SQLException si falla la consulta
     */
    public List<Cotizacion> listar(String estado) throws SQLException {
        String filtro = estado == null ? "" : estado;
        List<Cotizacion> cotizaciones = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR)) {
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cotizaciones.add(mapear(rs));
                }
            }
        }
        return cotizaciones;
    }

    /**
     * Cotización con todos sus productos.
     *
     * @param idCotizacion número de la cotización
     * @return la cotización, o {@code null} si no existe
     * @throws SQLException si falla la consulta
     */
    public Cotizacion buscarPorId(int idCotizacion) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            Cotizacion cotizacion;
            try (PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {
                ps.setInt(1, idCotizacion);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    cotizacion = mapear(rs);
                }
            }
            try (PreparedStatement ps = con.prepareStatement(SQL_DETALLES)) {
                ps.setInt(1, idCotizacion);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DetalleCotizacion d = new DetalleCotizacion();
                        d.setIdDetalleCotizacion(rs.getInt("id_detalle_cotizacion"));
                        d.setIdCotizacion(rs.getInt("id_cotizacion"));
                        d.setIdProducto(rs.getInt("id_producto"));
                        d.setNombreProducto(rs.getString("nombre_producto"));
                        d.setCantidad(rs.getInt("cantidad"));
                        d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                        cotizacion.getDetalles().add(d);
                    }
                }
            }
            return cotizacion;
        }
    }

    /**
     * Cambia el estado de atención y, si se indica, la nota interna.
     *
     * @param idCotizacion  cotización a modificar
     * @param estado        nuevo estado (ya validado contra {@link Cotizacion#ESTADOS})
     * @param cambiarNota   true para reemplazar la nota; false para conservarla
     * @param notaInterna   nueva nota (null o vacía la borra)
     * @return true si la cotización existe
     * @throws SQLException si falla la actualización
     */
    public boolean cambiarEstado(int idCotizacion, String estado, boolean cambiarNota, String notaInterna)
            throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(cambiarNota ? SQL_CAMBIAR_ESTADO_Y_NOTA : SQL_CAMBIAR_ESTADO)) {
            int i = 1;
            ps.setString(i++, estado);
            if (cambiarNota) {
                ps.setString(i++, notaInterna == null || notaInterna.isBlank() ? null : notaInterna);
            }
            ps.setInt(i, idCotizacion);
            return ps.executeUpdate() > 0;
        }
    }

    private Cotizacion mapear(ResultSet rs) throws SQLException {
        Cotizacion c = new Cotizacion();
        c.setIdCotizacion(rs.getInt("id_cotizacion"));
        c.setEstado(rs.getString("estado"));
        c.setNotaInterna(rs.getString("nota_interna"));
        c.setCreadoEn(fecha(rs.getTimestamp("creado_en")));
        c.setActualizadoEn(fecha(rs.getTimestamp("actualizado_en")));
        c.setTotalItems(rs.getInt("total_items"));
        c.setTotalUnidades(rs.getInt("total_unidades"));
        c.setTotal(rs.getBigDecimal("total"));
        return c;
    }

    private static LocalDateTime fecha(Timestamp marca) {
        return marca == null ? null : marca.toLocalDateTime();
    }
}
