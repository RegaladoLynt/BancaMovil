package Controlador;

import DAO.NotificacionDAO.NotificacionItem;
import Vista.PnlAdminNotificaciones;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

        List<NotificacionItem> lista = notificacionDAO.listarNotificacionesAdmin();
        for (NotificacionItem n : lista) {
            modelo.addRow(new Object[]{
                n.id,
                n.fecha,
                n.mensaje,
                n.leida ? "Leída" : "Nueva"
            });
        }

        vista.tblNotif.setModel(modelo);
        vista.tblNotif.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        vista.tblNotif.getColumnModel().getColumn(0).setPreferredWidth(45);
        vista.tblNotif.getColumnModel().getColumn(1).setPreferredWidth(90);
        vista.tblNotif.getColumnModel().getColumn(2).setPreferredWidth(520);
        vista.tblNotif.getColumnModel().getColumn(3).setPreferredWidth(80);
    }

    private void marcarTodasLeidas() {
        if (notificacionDAO.marcarTodasLeidas()) {
            cargarTabla();
            JOptionPane.showMessageDialog(vista, "Todas las notificaciones fueron marcadas como leídas.", "Notificaciones", JOptionPane.INFORMATION_MESSAGE);
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
            if (notificacionDAO.reenviarNotificacion(id, fechaSim)) {
                cargarTabla();
                JOptionPane.showMessageDialog(vista, "Notificación #" + id + " reenviada con fecha " + fechaSim + ".", "Reenvío Exitoso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(vista, "No se encontró una notificación con ID #" + id, "Sin resultados", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(vista, "El ID debe ser numérico.", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }
}
