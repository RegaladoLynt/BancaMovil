package Controlador;

import Conexion.BaseDatosConexion;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class AtencionControlador extends BaseControlador {

    @Override
    public String obtenerNombreModulo() {
        return "Atención al Cliente y Alertas Inteligentes";
    }

    // Método llamado directamente desde PnlClienteAtencion (4 parámetros)
    public String enviarAtencion(int idUsuario, String tipo, String mensaje, LocalDate fecha) {
        String nombreCliente = getSesion().getNombreCompleto();
        if (nombreCliente == null || nombreCliente.isEmpty()) {
            nombreCliente = "ID #" + idUsuario;
        }
        return registrarAtencion(idUsuario, nombreCliente, tipo, mensaje, fecha);
    }

    public String registrarAtencion(int idUsuario, String nombreCliente, String tipo, String mensaje, LocalDate fecha) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return "Por favor, escribe el detalle de tu " + tipo.toLowerCase() + ".";
        }
        LocalDate fechaReg = (fecha != null) ? fecha : LocalDate.now();
        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        String sql = "INSERT INTO quejas_sugerencias (id_usuario, tipo, mensaje, fecha_registro) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, tipo);
            ps.setString(3, mensaje.trim());
            ps.setDate(4, Date.valueOf(fechaReg));
            ps.executeUpdate();

            if ("QUEJA".equalsIgnoreCase(tipo)) {
                String alerta = "[ALERTA QUEJA] Cliente " + nombreCliente + ": " + mensaje.trim();
                try (PreparedStatement psNotif = cn.prepareStatement(
                        "INSERT INTO notificaciones (id_usuario_origen, mensaje, fecha_notificacion, leida) VALUES (?, ?, ?, FALSE)")) {
                    psNotif.setInt(1, idUsuario);
                    psNotif.setString(2, alerta);
                    psNotif.setDate(3, Date.valueOf(fechaReg));
                    psNotif.executeUpdate();
                }
            }
            return "OK";
        } catch (SQLException e) {
            return "Error al registrar: " + e.getMessage();
        }
    }

    public DefaultTableModel obtenerTablaAtencion(Integer idUsuarioFiltro) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Fecha", "Cliente", "Tipo", "Mensaje", "Respuesta del Banco"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = "SELECT q.id_queja, q.fecha_registro, CONCAT(u.nombres, ' ', u.apellidos) AS cliente, " +
                     "q.tipo, q.mensaje, q.respuesta_admin " +
                     "FROM quejas_sugerencias q " +
                     "INNER JOIN usuarios u ON q.id_usuario = u.id_usuario " +
                     (idUsuarioFiltro != null ? "WHERE q.id_usuario = ? " : "") +
                     "ORDER BY q.id_queja DESC";

        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (idUsuarioFiltro != null) {
                ps.setInt(1, idUsuarioFiltro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelo.addRow(new Object[]{
                        rs.getInt("id_queja"),
                        rs.getDate("fecha_registro"),
                        rs.getString("cliente"),
                        rs.getString("tipo"),
                        rs.getString("mensaje"),
                        rs.getString("respuesta_admin")
                    });
                }
            }
        } catch (SQLException e) {
            System.out.println("Error cargando tabla de atención: " + e.getMessage());
        }
        return modelo;
    }

    public String responderAtencion(int idQueja, String respuesta) {
        if (respuesta == null || respuesta.trim().isEmpty()) {
            return "Escribe una respuesta antes de guardar.";
        }
        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try (PreparedStatement ps = cn.prepareStatement("UPDATE quejas_sugerencias SET respuesta_admin = ? WHERE id_queja = ?")) {
            ps.setString(1, respuesta.trim());
            ps.setInt(2, idQueja);
            ps.executeUpdate();
            return "OK";
        } catch (SQLException e) {
            return "Error al guardar respuesta: " + e.getMessage();
        }
    }

    public List<String> obtenerAlertasReloj(LocalDate fechaSimulada, Integer idUsuarioFiltro) {
        List<String> alertas = new ArrayList<>();
        if (fechaSimulada == null) return alertas;

        boolean esVistaCliente = (idUsuarioFiltro != null);

        String sql = "SELECT u.id_usuario, CONCAT(u.nombres, ' ', u.apellidos) AS cliente, " +
                     "c.numero_cuota, c.monto_cuota, c.fecha_vencimiento " +
                     "FROM cuotas c " +
                     "INNER JOIN creditos cr ON c.id_credito = cr.id_credito " +
                     "INNER JOIN usuarios u ON cr.id_usuario = u.id_usuario " +
                     "WHERE c.estado = 'POR_PAGAR' " +
                     (esVistaCliente ? "AND u.id_usuario = ? " : "");

        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (esVistaCliente) {
                ps.setInt(1, idUsuarioFiltro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate venc = rs.getDate("fecha_vencimiento").toLocalDate();
                    long dias = ChronoUnit.DAYS.between(fechaSimulada, venc);
                    String cliente = rs.getString("cliente");
                    int numCuota = rs.getInt("numero_cuota");
                    double monto = rs.getDouble("monto_cuota");

                    if (dias < 0) {
                        long atraso = Math.abs(dias);
                        if (esVistaCliente) {
                            alertas.add("[CUOTA VENCIDA] Tienes la cuota #" + numCuota + " por S/. " + String.format("%.2f", monto) + " con " + atraso + " día(s) de atraso. ¡Regularízala hoy!");
                        } else {
                            alertas.add("[MORA] Cliente " + cliente + " tiene la cuota #" + numCuota + " por S/. " + String.format("%.2f", monto) + " con " + atraso + " día(s) de atraso.");
                        }
                    } else if (dias == 0) {
                        if (esVistaCliente) {
                            alertas.add("[VENCE HOY] ¡Recuerda! Hoy vence tu cuota #" + numCuota + " por S/. " + String.format("%.2f", monto) + ".");
                        } else {
                            alertas.add("[VENCE HOY] Cliente " + cliente + " tiene cuota #" + numCuota + " por S/. " + String.format("%.2f", monto) + " que vence HOY.");
                        }
                    } else if (dias == 1) {
                        if (esVistaCliente) {
                            alertas.add("[VENCE MAÑANA] Tu cuota #" + numCuota + " por S/. " + String.format("%.2f", monto) + " vence mañana (" + venc + ").");
                        } else {
                            alertas.add("[VENCE MAÑANA] Cliente " + cliente + " tiene cuota #" + numCuota + " por S/. " + String.format("%.2f", monto) + " que vence mañana (" + venc + ").");
                        }
                    } else if (dias <= 3) {
                        if (esVistaCliente) {
                            alertas.add("[PRÓXIMO VENCIMIENTO] Tu cuota #" + numCuota + " por S/. " + String.format("%.2f", monto) + " vence en " + dias + " días (" + venc + ").");
                        } else {
                            alertas.add("[PRÓXIMO] Cliente " + cliente + " tiene cuota #" + numCuota + " por S/. " + String.format("%.2f", monto) + " que vence en " + dias + " días.");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error verificando alertas de reloj: " + e.getMessage());
        }
        return alertas;
    }
}
