package Controlador;

import Modelo.Movimiento;
import Modelo.Usuario;
import Vista.PnlTransacciones;
import java.awt.Color;
import java.awt.Component;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class AdminTransaccionesControlador extends BaseControlador {
    private final PnlTransacciones pnl;
    private final Usuario cliente;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

        List<Movimiento> lista = usuarioDAO.listarMovimientosUsuario(cliente.getIdUsuario());
        for (Movimiento m : lista) {
            String tipo = m.getTipo();
            boolean esIngreso = tipo.contains("DEPOSITO") || tipo.contains("RECIBIDA") || tipo.contains("DESEMBOLSO");
            String signo = esIngreso ? "+ " : "- ";

            modelo.addRow(new Object[]{
                m.getIdRegistro(),
                m.getFechaOperacion().format(fmt),
                tipo,
                m.getDetalle(),
                signo + String.format("%.2f", m.getMonto())
            });
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
            Movimiento m = usuarioDAO.buscarTransaccionPorId(idBuscado, cliente.getIdUsuario());
            if (m != null) {
                pnl.txtFechaTrans.setText(m.getFechaOperacion().format(fmt));
                pnl.txtOp.setText(m.getTipo());
                pnl.txtDetail.setText(m.getDetalle());
                pnl.txtMonto.setText(String.format("S/. %.2f", m.getMonto()));
            } else {
                JOptionPane.showMessageDialog(pnl, "No se encontró el registro #" + idBuscado, "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(pnl, "El N° de registro debe ser numérico.", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }
}
