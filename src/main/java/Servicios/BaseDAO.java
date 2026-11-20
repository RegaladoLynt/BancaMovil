package Servicios;

import Conexion.BaseDatosConexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO {

    @FunctionalInterface
    protected interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    protected Connection getCon() {
        return BaseDatosConexion.GetInstancia().GetConectar();
    }

    protected boolean ejecutarCambio(String sql, Object... params) {
        try (PreparedStatement ps = getCon().prepareStatement(sql)) {
            asignarParametros(ps, params);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[SQL Error]: " + e.getMessage());
            return false;
        }
    }

    protected <T> List<T> consultarLista(String sql, RowMapper<T> mapper, Object... params) {
        List<T> lista = new ArrayList<>();
        try (PreparedStatement ps = getCon().prepareStatement(sql)) {
            asignarParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[SQL Query Error]: " + e.getMessage());
        }
        return lista;
    }

    protected <T> T consultarUno(String sql, RowMapper<T> mapper, Object... params) {
        List<T> lista = consultarLista(sql, mapper, params);
        return lista.isEmpty() ? null : lista.get(0);
    }

    private void asignarParametros(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
