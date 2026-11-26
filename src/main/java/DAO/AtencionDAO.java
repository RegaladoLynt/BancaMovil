package DAO;

import Modelo.AtencionCliente;
import Modelo.Queja;
import Modelo.Sugerencia;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AtencionDAO extends BaseDAO {

    private final RowMapper<AtencionCliente> mapAtencion = rs -> {
        String tipo = rs.getString("tipo");
        AtencionCliente item = "QUEJA".equalsIgnoreCase(tipo) ? new Queja() : new Sugerencia();
        item.setIdRegistro(rs.getInt("id_queja"));
        item.setIdUsuario(rs.getInt("id_usuario"));
        item.setCliente(rs.getString("cliente"));
        item.setMensaje(rs.getString("mensaje"));
        item.setRespuestaAdmin(rs.getString("respuesta_admin"));
        item.setFechaRegistro(rs.getDate("fecha_registro").toLocalDate());
        return item;
    };

    public boolean registrarAtencionSP(AtencionCliente atencion) {
        try (CallableStatement cs = getCon().prepareCall("{CALL sp_registrar_atencion(?, ?, ?, ?)}")) {
            cs.setInt(1, atencion.getIdUsuario());
            cs.setString(2, atencion.getTipo());
            cs.setString(3, atencion.getMensaje());
            cs.setDate(4, Date.valueOf(atencion.getFechaRegistro()));
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("[Error SP Atencion]: " + e.getMessage());
            return false;
        }
    }

    public List<AtencionCliente> listarAtencion(Integer idUsuarioFiltro) {
        String sql = "SELECT q.*, CONCAT(u.nombres, ' ', u.apellidos) AS cliente " +
                     "FROM quejas_sugerencias q INNER JOIN usuarios u ON q.id_usuario = u.id_usuario " +
                     (idUsuarioFiltro != null ? "WHERE q.id_usuario = ? " : "") +
                     "ORDER BY q.id_queja DESC";
        return idUsuarioFiltro != null ? consultarLista(sql, mapAtencion, idUsuarioFiltro) : consultarLista(sql, mapAtencion);
    }

    public AtencionCliente buscarPorId(int idQueja) {
        String sql = "SELECT q.*, CONCAT(u.nombres, ' ', u.apellidos) AS cliente " +
                     "FROM quejas_sugerencias q INNER JOIN usuarios u ON q.id_usuario = u.id_usuario WHERE q.id_queja = ?";
        return consultarUno(sql, mapAtencion, idQueja);
    }

    public boolean responder(int idQueja, String respuesta) {
        return ejecutarCambio("UPDATE quejas_sugerencias SET respuesta_admin = ? WHERE id_queja = ?", respuesta, idQueja);
    }
}
