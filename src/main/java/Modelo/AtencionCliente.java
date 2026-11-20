package Modelo;

import Conexion.BaseDatosConexion;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class AtencionCliente {
    protected int idRegistro;
    protected int idUsuario;
    protected String cliente;
    protected String mensaje;
    protected String respuestaAdmin;
    protected LocalDate fechaRegistro;

    public AtencionCliente(int idUsuario, String mensaje, LocalDate fechaRegistro) {
        this.idUsuario = idUsuario;
        this.mensaje = mensaje;
        this.fechaRegistro = fechaRegistro;
    }

    protected AtencionCliente() {}

    public abstract String getTipo();
    public abstract String generarMensajeNotificacion();

    public int getIdRegistro() { return idRegistro; }
    public String getCliente() { return cliente; }
    public String getMensaje() { return mensaje; }
    public String getRespuestaAdmin() { return respuestaAdmin; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }

    public boolean guardar() {
        String sql = "INSERT INTO quejas_sugerencias (id_usuario, tipo, mensaje, fecha_registro) VALUES (?, ?, ?, ?)";
        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, getTipo());
            ps.setString(3, mensaje);
            ps.setDate(4, Date.valueOf(fechaRegistro));
            if (ps.executeUpdate() > 0) {
                String alertaAdmin = generarMensajeNotificacion();
                if (alertaAdmin != null) {
                    String sqlNotif = "INSERT INTO notificaciones (id_usuario_origen, mensaje, fecha_notificacion, leida) VALUES (?, ?, ?, FALSE)";
                    try (PreparedStatement psNotif = cn.prepareStatement(sqlNotif)) {
                        psNotif.setInt(1, idUsuario);
                        psNotif.setString(2, alertaAdmin);
                        psNotif.setDate(3, Date.valueOf(fechaRegistro));
                        psNotif.executeUpdate();
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error al guardar atención: " + e.getMessage());
        }
        return false;
    }

    public static List<AtencionCliente> listar(Integer idUsuarioFiltro) {
        List<AtencionCliente> lista = new ArrayList<>();
        String sql = "SELECT q.*, CONCAT(u.nombres, ' ', u.apellidos) AS cliente " +
                     "FROM quejas_sugerencias q INNER JOIN usuarios u ON q.id_usuario = u.id_usuario " +
                     (idUsuarioFiltro != null ? "WHERE q.id_usuario = ? " : "") +
                     "ORDER BY q.id_queja DESC";
        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (idUsuarioFiltro != null) {
                ps.setInt(1, idUsuarioFiltro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    AtencionCliente item = "QUEJA".equals(tipo) ? new Queja() : new Sugerencia();
                    item.idRegistro = rs.getInt("id_queja");
                    item.idUsuario = rs.getInt("id_usuario");
                    item.cliente = rs.getString("cliente");
                    item.mensaje = rs.getString("mensaje");
                    item.respuestaAdmin = rs.getString("respuesta_admin");
                    item.fechaRegistro = rs.getDate("fecha_registro").toLocalDate();
                    lista.add(item);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al listar atención: " + e.getMessage());
        }
        return lista;
    }

    public static boolean responder(int idRegistro, String respuesta) {
        String sql = "UPDATE quejas_sugerencias SET respuesta_admin = ? WHERE id_queja = ?";
        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, respuesta);
            ps.setInt(2, idRegistro);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al responder atención: " + e.getMessage());
            return false;
        }
    }
}
