package Controlador;

import Modelo.Cuota;
import Modelo.Usuario;
import Vista.FrmDashboardAdmin;
import Vista.PnlAdminVacio;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class AdminVacioControlador extends BaseControlador {
    private final PnlAdminVacio vista;
    private final FrmDashboardAdmin dashboardVista;

    public AdminVacioControlador(PnlAdminVacio vista, FrmDashboardAdmin dashboardVista) {
        this.vista = vista;
        this.dashboardVista = dashboardVista;
        inicializar();
    }

    @Override
    public String obtenerNombreModulo() {
        return "Controlador Cartera General de Clientes";
    }

    private void inicializar() {
        configurarTablaEstetica();
        cargarCarteraClientes();

        vista.tblClientes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = vista.tblClientes.getSelectedRow();
                if (fila != -1 && dashboardVista != null) {
                    String dniSeleccionado = vista.tblClientes.getValueAt(fila, 0).toString();
                    dashboardVista.txtBuscarDni.setText(dniSeleccionado);
                    dashboardVista.btnBuscarDni.doClick();
                }
            }
        });
    }

    private void configurarTablaEstetica() {
        JTable t = vista.tblClientes;
        t.setRowHeight(28);
        t.setBackground(new Color(248, 249, 249));
        t.setGridColor(new Color(213, 216, 220));
        t.setShowGrid(true);
        t.getTableHeader().setBackground(new Color(27, 42, 74));
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    private void cargarCarteraClientes() {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"DNI", "Cliente", "N° Cuenta", "Saldo (S/.)", "Situación Crediticia"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        LocalDate fechaSim = (getSesion().getFechaSimulada() != null) ? getSesion().getFechaSimulada() : LocalDate.now();
        List<Usuario> clientes = usuarioDAO.listarTodosLosClientes();

        for (Usuario u : clientes) {
            String situacion = determinarSituacionCliente(u, fechaSim);
            modelo.addRow(new Object[]{
                u.getDni(),
                u.getNombreCompleto(),
                u.getNumeroCuenta() != null ? u.getNumeroCuenta() : "Sin cuenta",
                String.format("S/. %.2f", u.getSaldo()),
                situacion
            });
        }

        vista.tblClientes.setModel(modelo);
        aplicarColoresPorFila();
    }

    private String determinarSituacionCliente(Usuario u, LocalDate fechaSim) {
        if ("BLOQUEADO".equalsIgnoreCase(u.getEstado())) {
            return "INACTIVO";
        }
        List<Cuota> cuotas = creditoDAO.listarHistorialCuotasUsuario(u.getIdUsuario());
        boolean tieneCreditoActivo = false;
        boolean tieneAtraso = false;
        boolean vencePronto = false;

        for (Cuota c : cuotas) {
            if ("PENDIENTE".equals(c.getEstadoCuota())) {
                tieneCreditoActivo = true;
                long dias = ChronoUnit.DAYS.between(fechaSim, c.getFechaVencimiento());
                if (dias < 0) tieneAtraso = true;
                else if (dias <= 3) vencePronto = true;
            }
        }

        if (tieneAtraso) return "CON RETRASO";
        if (vencePronto) return "VENCE PRONTO (0-3d)";
        if (tieneCreditoActivo) return "CRÉDITO AL DÍA";
        return "SIN CRÉDITO";
    }

    private void aplicarColoresPorFila() {
        vista.tblClientes.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String situacion = table.getValueAt(row, 4).toString();
                c.setFont(new Font("Segoe UI", Font.BOLD, 12));

                switch (situacion) {
                    case "INACTIVO":
                        c.setBackground(new Color(44, 62, 80));
                        c.setForeground(Color.WHITE);
                        break;
                    case "CON RETRASO":
                        c.setBackground(new Color(250, 219, 216));
                        c.setForeground(new Color(120, 40, 31));
                        break;
                    case "VENCE PRONTO (0-3d)":
                        c.setBackground(new Color(252, 243, 207));
                        c.setForeground(new Color(126, 81, 9));
                        break;
                    case "CRÉDITO AL DÍA":
                        c.setBackground(new Color(235, 222, 240));
                        c.setForeground(new Color(74, 35, 90));
                        break;
                    default:
                        c.setBackground(new Color(213, 245, 227));
                        c.setForeground(new Color(20, 90, 50));
                        break;
                }
                return c;
            }
        });
    }
}
