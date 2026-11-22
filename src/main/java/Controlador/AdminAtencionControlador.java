package Controlador;

import Conexion.BaseDatosConexion;
import Vista.PnlAdminAtencion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.JTable;

public class AdminAtencionControlador extends BaseControlador {
    private final PnlAdminAtencion vista;
    private final AtencionControlador atencionProceso = new AtencionControlador();

    public AdminAtencionControlador(PnlAdminAtencion vista) {
        this.vista = vista;
        inicializar();
    }

    @Override
    public String obtenerNombreModulo() {
        return "Controlador Bandeja de Atención al Cliente";
    }

    private void inicializar() {
        cargarTabla();
        vista.btnBuscar.addActionListener(e -> buscarPorCodigo());
        vista.btnRespuesta1.addActionListener(e -> guardarRespuesta());

        vista.tblQuejas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && vista.tblQuejas.getSelectedRow() != -1) {
                int fila = vista.tblQuejas.getSelectedRow();
                vista.txtRegistro.setText(vista.tblQuejas.getValueAt(fila, 0).toString());
                vista.txtTipo.setText(vista.tblQuejas.getValueAt(fila, 3).toString());
                vista.txtMensaje.setText(vista.tblQuejas.getValueAt(fila, 4).toString());
                vista.txtMensaje.setCaretPosition(0);
                vista.txtRespuesta.setText(vista.tblQuejas.getValueAt(fila, 5).toString());
                vista.txtRespuesta.setCaretPosition(0);
            }
        });
    }

    private void cargarTabla() {
        vista.tblQuejas.setModel(atencionProceso.obtenerTablaAtencion(null));
        vista.tblQuejas.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        vista.tblQuejas.getColumnModel().getColumn(0).setPreferredWidth(45);
        vista.tblQuejas.getColumnModel().getColumn(1).setPreferredWidth(85);
        vista.tblQuejas.getColumnModel().getColumn(2).setPreferredWidth(130);
        vista.tblQuejas.getColumnModel().getColumn(3).setPreferredWidth(95);
        vista.tblQuejas.getColumnModel().getColumn(4).setPreferredWidth(320);
        vista.tblQuejas.getColumnModel().getColumn(5).setPreferredWidth(280);
    }

    private void buscarPorCodigo() {
        String idTxt = vista.txtRegistro.getText().trim();
        if (idTxt.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Ingrese el código de registro a buscar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int id = Integer.parseInt(idTxt);
            Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
            try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM quejas_sugerencias WHERE id_queja = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        vista.txtTipo.setText(rs.getString("tipo"));
                        vista.txtMensaje.setText(rs.getString("mensaje"));
                        vista.txtMensaje.setCaretPosition(0);
                        vista.txtRespuesta.setText(rs.getString("respuesta_admin"));
                        vista.txtRespuesta.setCaretPosition(0);
                    } else {
                        JOptionPane.showMessageDialog(vista, "No se encontró el registro #" + id, "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(vista, "El código debe ser numérico.", "Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            System.out.println("Error buscando queja: " + ex.getMessage());
        }
    }

    private void guardarRespuesta() {
        String idTxt = vista.txtRegistro.getText().trim();
        if (idTxt.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Seleccione o busque primero un registro antes de responder.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int id = Integer.parseInt(idTxt);
            String res = atencionProceso.responderAtencion(id, vista.txtRespuesta.getText());
            if ("OK".equals(res)) {
                cargarTabla();
                JOptionPane.showMessageDialog(vista, "Respuesta guardada correctamente.", "Atención al Cliente", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(vista, res, "Validación", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(vista, "Código de registro inválido.", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }
}
