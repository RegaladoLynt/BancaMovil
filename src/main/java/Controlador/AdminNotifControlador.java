package Controlador;

import Conexion.BaseDatosConexion;
import Vista.PnlAdminNotificaciones;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class AdminNotifControlador extends BaseControlador {
    private final PnlAdminNotificaciones vista;

    public AdminNotifControlador(PnlAdminNotificaciones vista) {
        this.vista = vista;
        inicializar();
    }

    @Override
    public String obtenerNombreModulo() {
        return "Controlador Notificaciones y Reloj del Sistema";
    }

    private void inicializar() {
        LocalDate fechaSim = (getSesion().getFechaSimulada() != null) ? getSesion().getFechaSimulada() : LocalDate.now();
        vista.txtFecha.setText("Reloj del Sistema: " + fechaSim.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        cargarTabla();

        vista.btnMarcarLeidas.addActionListener(e -> marcarTodasLeidas());
        vista.btnReenvio.addActionListener(e -> reenviarAlerta());

        vista.tblNotif.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && vista.tblNotif.getSelectedRow() != -1) {
                int fila = vista.tblNotif.getSelectedRow();
                vista.txtReenvio.setText(vista.tblNotif.getValueAt(fila, 0).toString());
            }
        });
    }

    private void cargarTabla() {
        DefaultTableModel modelo = new DefaultTableModel(new Object[]{"ID", "Fecha", "Mensaje de Alerta / Notificación", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM notificaciones ORDER BY id_notificacion DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getInt("id_notificacion"),
                    rs.getDate("fecha_notificacion"),
                    rs.getString("mensaje"),
                    rs.getBoolean("leida") ? "Leída" : "Nueva"
                });
            }
        } catch (SQLException e) {
            System.out.println("Error cargando notificaciones: " + e.getMessage());
        }

        vista.tblNotif.setModel(modelo);
        vista.tblNotif.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        vista.tblNotif.getColumnModel().getColumn(0).setPreferredWidth(45);
        vista.tblNotif.getColumnModel().getColumn(1).setPreferredWidth(90);
        vista.tblNotif.getColumnModel().getColumn(2).setPreferredWidth(520);
        vista.tblNotif.getColumnModel().getColumn(3).setPreferredWidth(80);
    }

    private void marcarTodasLeidas() {
        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try (PreparedStatement ps = cn.prepareStatement("UPDATE notificaciones SET leida = TRUE WHERE leida = FALSE")) {
            ps.executeUpdate();
            cargarTabla();
            JOptionPane.showMessageDialog(vista, "Todas las notificaciones fueron marcadas como leídas.", "Notificaciones", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            System.out.println("Error marcando leídas: " + e.getMessage());
        }
    }

    private void reenviarAlerta() {
        String idTxt = vista.txtReenvio.getText().trim();
        if (idTxt.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Seleccione o escriba el ID de la notificación a reenviar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int id = Integer.parseInt(idTxt);
            LocalDate fechaSim = (getSesion().getFechaSimulada() != null) ? getSesion().getFechaSimulada() : LocalDate.now();
            Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
            try (PreparedStatement ps = cn.prepareStatement("UPDATE notificaciones SET leida = FALSE, fecha_notificacion = ? WHERE id_notificacion = ?")) {
                ps.setDate(1, Date.valueOf(fechaSim));
                ps.setInt(2, id);
                if (ps.executeUpdate() > 0) {
                    cargarTabla();
                    JOptionPane.showMessageDialog(vista, "Notificación #" + id + " reenviada con fecha " + fechaSim + ".", "Reenvío Exitoso", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(vista, "No se encontró una notificación con ID #" + id, "Sin resultados", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(vista, "El ID debe ser numérico.", "Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            System.out.println("Error reenviando notificación: " + e.getMessage());
        }
    }
}
