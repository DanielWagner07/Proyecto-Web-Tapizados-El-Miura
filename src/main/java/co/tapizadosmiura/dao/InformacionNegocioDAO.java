package co.tapizadosmiura.dao;

import co.tapizadosmiura.config.Conexion;
import co.tapizadosmiura.modelo.InformacionNegocio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de la tabla {@code informacion_negocio} (CU09 - Editar información comercial).
 */
public class InformacionNegocioDAO {

    /** El orden de los grupos sigue el orden en que se declaró el ENUM en la tabla. */
    private static final String SQL_LISTAR =
            "SELECT id_informacion, clave, etiqueta, valor, grupo FROM informacion_negocio "
            + "ORDER BY grupo, id_informacion";

    private static final String SQL_ACTUALIZAR_VALOR =
            "UPDATE informacion_negocio SET valor = ? WHERE clave = ?";

    private static final String SQL_BUSCAR_VALOR =
            "SELECT valor FROM informacion_negocio WHERE clave = ?";

    /**
     * Valor de un dato puntual, por ejemplo el número de WhatsApp para las cotizaciones.
     *
     * @param clave clave del dato (ej. "whatsapp")
     * @return el valor, o {@code null} si la clave no existe
     * @throws SQLException si falla la consulta
     */
    public String buscarValor(String clave) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_VALOR)) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("valor") : null;
            }
        }
    }

    /**
     * Todos los datos del negocio, agrupados (contacto, horario, cobertura...).
     *
     * @return lista ordenada por grupo
     * @throws SQLException si falla la consulta
     */
    public List<InformacionNegocio> listar() throws SQLException {
        List<InformacionNegocio> datos = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                InformacionNegocio dato = new InformacionNegocio();
                dato.setIdInformacion(rs.getInt("id_informacion"));
                dato.setClave(rs.getString("clave"));
                dato.setEtiqueta(rs.getString("etiqueta"));
                dato.setValor(rs.getString("valor"));
                dato.setGrupo(rs.getString("grupo"));
                datos.add(dato);
            }
        }
        return datos;
    }

    /**
     * Guarda varios valores en una sola transacción: o se guardan todos o ninguno.
     *
     * @param valoresPorClave clave → nuevo valor (ya validado)
     * @throws SQLException si falla la operación (se revierte todo)
     */
    public void actualizarValores(Map<String, String> valoresPorClave) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR_VALOR)) {
                for (Map.Entry<String, String> entrada : valoresPorClave.entrySet()) {
                    ps.setString(1, entrada.getValue());
                    ps.setString(2, entrada.getKey());
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
