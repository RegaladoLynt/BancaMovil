package Controlador;

import Conexion.BaseDatosConexion;
import Modelo.Usuario;
import Vista.PnlTransacciones;
import java.awt.Color;
import java.awt.Component;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class AdminTransaccionesControlador extends BaseControlador {
    private final PnlTransacciones pnl;
    private final Usuario cliente;

    public AdminTransaccionesControlador(PnlTransacciones pnl, Usuario cliente) {
        this.pnl = pnl;
        this.cliente = cliente;
        inicializar();
    }

    @Override
    public String obtenerNombreModulo() {
        return "Controlador de Transacciones";
    }

    private void inicializar() {
        cargarTabla();
        pnl.btnConsultar.addActionListener(e -> consultarRegistro());
    }

    private void cargarTabla() {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Reg #", "Fecha", "Operación", "Detalle", "Monto (S/.)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM transacciones WHERE id_usuario = ? ORDER BY id_transaccion DESC")) {
            ps.setInt(1, cliente.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    boolean esIngreso = tipo.contains("DEPOSITO") || tipo.contains("RECIBIDA") || tipo.contains("DESEMBOLSO");
                    String signo = esIngreso ? "+ " : "- ";
                    String fechaStr = rs.getDate("fecha_transaccion").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    modelo.addRow(new Object[]{
                        rs.getInt("id_transaccion"),
                        fechaStr,
                        tipo,
                        rs.getString("detalle"),
                        signo + String.format("%.2f", rs.getDouble("monto"))
                    });
                }
            }
        } catch (SQLException e) {
            System.out.println("Error tabla transacciones: " + e.getMessage());
        }

        pnl.tblTransferencias.setModel(modelo);
        pnl.tblTransferencias.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String val = value != null ? value.toString() : "";
                c.setForeground(val.startsWith("+") ? new Color(30, 132, 73) : new Color(192, 57, 43));
                return c;
            }
        });
    }

    private void consultarRegistro() {
        String idTxt = pnl.txtIdRegistro.getText().trim();
        if (idTxt.isEmpty()) {
            JOptionPane.showMessageDialog(pnl, "Ingrese el N° de registro a consultar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int idBuscado = Integer.parseInt(idTxt);
            Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
            try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM transacciones WHERE id_transaccion = ? AND id_usuario = ?")) {
                ps.setInt(1, idBuscado);
                ps.setInt(2, cliente.getIdUsuario());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        pnl.txtFechaTrans.setText(rs.getDate("fecha_transaccion").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        pnl.txtOp.setText(rs.getString("tipo"));
                        pnl.txtDetail.setText(rs.getString("detalle"));
                        pnl.txtMonto.setText(String.format("S/. %.2f", rs.getDouble("monto")));
                    } else {
                        JOptionPane.showMessageDialog(pnl, "No se encontró el registro #" + idBuscado, "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(pnl, "El N° de registro debe ser numérico.", "Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            System.out.println("Error consultando registro: " + ex.getMessage());
        }
    }
}
