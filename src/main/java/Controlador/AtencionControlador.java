package Controlador;

import Conexion.BaseDatosConexion;
import Modelo.AtencionCliente;
import Modelo.Queja;
import Modelo.Sugerencia;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class AtencionControlador {

    public String enviarAtencion(int idUsuario, String tipo, String mensaje, LocalDate fecha) {
        if (mensaje == null || mensaje.trim().length() < 5) {
            return "Por favor describe tu mensaje con mayor detalle (mínimo 5 caracteres).";
        }
        AtencionCliente registro = "QUEJA".equalsIgnoreCase(tipo)
                ? new Queja(idUsuario, mensaje.trim(), fecha)
                : new Sugerencia(idUsuario, mensaje.trim(), fecha);

        return registro.guardar() ? "OK" : "No se pudo registrar en el sistema.";
    }

    public String responderAtencion(int idRegistro, String respuesta) {
        if (respuesta == null || respuesta.trim().isEmpty()) {
            return "Escribe una respuesta antes de guardar.";
        }
        return AtencionCliente.responder(idRegistro, respuesta.trim())
                ? "OK"
                : "Error al guardar la respuesta del administrador.";
    }

    public DefaultTableModel obtenerTablaAtencion(Integer idUsuarioFiltro) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Fecha", "Cliente", "Tipo", "Mensaje", "Respuesta Admin"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (AtencionCliente item : AtencionCliente.listar(idUsuarioFiltro)) {
            modelo.addRow(new Object[]{
                item.getIdRegistro(),
                item.getFechaRegistro(),
                item.getCliente(),
                item.getTipo(),
                item.getMensaje(),
                item.getRespuestaAdmin()
            });
        }
        return modelo;
    }

    public List<String> obtenerAlertasReloj(LocalDate fechaSimulada, Integer idUsuarioFiltro) {
        List<String> alertas = new ArrayList<>();
        String sql = "SELECT u.nombres, u.apellidos, c.numero_cuota, c.monto_cuota, c.fecha_vencimiento " +
                     "FROM cuotas c " +
                     "INNER JOIN creditos cr ON c.id_credito = cr.id_credito " +
                     "INNER JOIN usuarios u ON cr.id_usuario = u.id_usuario " +
                     "WHERE c.estado = 'POR_PAGAR' " +
                     (idUsuarioFiltro != null ? "AND u.id_usuario = ? " : "");

        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (idUsuarioFiltro != null) {
                ps.setInt(1, idUsuarioFiltro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate venc = rs.getDate("fecha_vencimiento").toLocalDate();
                    long diasRestantes = ChronoUnit.DAYS.between(fechaSimulada, venc);

                    if (diasRestantes == 0) {
                        alertas.add("[HOY VENCE] " + rs.getString("nombres") + " " + rs.getString("apellidos") +
                                " - Cuota #" + rs.getInt("numero_cuota") + " (S/. " + rs.getDouble("monto_cuota") + ") vence HOY (" + venc + ").");
                    } else if (diasRestantes == 1) {
                        alertas.add("[VENCE MAÑANA] " + rs.getString("nombres") + " " + rs.getString("apellidos") +
                                " - Cuota #" + rs.getInt("numero_cuota") + " (S/. " + rs.getDouble("monto_cuota") + ") vence MAÑANA (" + venc + ").");
                    } else if (diasRestantes < 0) {
                        alertas.add("[VENCIDA] " + rs.getString("nombres") + " " + rs.getString("apellidos") +
                                " - Cuota #" + rs.getInt("numero_cuota") + " tiene " + Math.abs(diasRestantes) + " día(s) de atraso.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en alertas por reloj: " + e.getMessage());
        }
        return alertas;
    }
}
