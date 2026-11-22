package Controlador;

import Conexion.BaseDatosConexion;
import Vista.FrmDashboardAdmin;
import Vista.PnlAdminVacio;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

        // Al hacer clic en cualquier fila, rellena el DNI arriba y abre su expediente
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
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        LocalDate fechaSim = (getSesion().getFechaSimulada() != null) ? getSesion().getFechaSimulada() : LocalDate.now();
        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();

        String sql = "SELECT u.id_usuario, u.dni, CONCAT(u.nombres, ' ', u.apellidos) AS cliente, u.estado, " +
                     "c.numero_cuenta, c.saldo " +
                     "FROM usuarios u " +
                     "LEFT JOIN cuentas c ON u.id_usuario = c.id_usuario " +
                     "WHERE u.rol = 'CLIENTE' ORDER BY u.id_usuario ASC";

        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idUsuario = rs.getInt("id_usuario");
                String estadoCuenta = rs.getString("estado");
                String situacion = determinarSituacionCliente(cn, idUsuario, estadoCuenta, fechaSim);

                modelo.addRow(new Object[]{
                    rs.getString("dni"),
                    rs.getString("cliente"),
                    rs.getString("numero_cuenta") != null ? rs.getString("numero_cuenta") : "Sin cuenta",
                    String.format("S/. %.2f", rs.getDouble("saldo")),
                    situacion
                });
            }
        } catch (SQLException e) {
            System.out.println("Error cargando cartera de clientes: " + e.getMessage());
        }

        vista.tblClientes.setModel(modelo);
        aplicarColoresPorFila();
    }

    private String determinarSituacionCliente(Connection cn, int idUsuario, String estadoCuenta, LocalDate fechaSim) {
        if ("BLOQUEADO".equalsIgnoreCase(estadoCuenta)) {
            return "INACTIVO";
        }

        String sqlCuotas = "SELECT c.fecha_vencimiento FROM cuotas c " +
                           "INNER JOIN creditos cr ON c.id_credito = cr.id_credito " +
                           "WHERE cr.id_usuario = ? AND c.estado = 'POR_PAGAR'";

        boolean tieneCreditoActivo = false;
        boolean tieneAtraso = false;
        boolean vencePronto = false;

        try (PreparedStatement ps = cn.prepareStatement(sqlCuotas)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tieneCreditoActivo = true;
                    LocalDate venc = rs.getDate("fecha_vencimiento").toLocalDate();
                    long dias = ChronoUnit.DAYS.between(fechaSim, venc);
                    if (dias < 0) {
                        tieneAtraso = true;
                    } else if (dias <= 3) {
                        vencePronto = true;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error evaluando cuotas cartera: " + e.getMessage());
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
                        c.setBackground(new Color(44, 62, 80));    // Negro carbón
                        c.setForeground(Color.WHITE);
                        break;
                    case "CON RETRASO":
                        c.setBackground(new Color(250, 219, 216)); // Rojo pastel
                        c.setForeground(new Color(120, 40, 31));
                        break;
                    case "VENCE PRONTO (0-3d)":
                        c.setBackground(new Color(252, 243, 207)); // Amarillo pastel
                        c.setForeground(new Color(126, 81, 9));
                        break;
                    case "CRÉDITO AL DÍA":
                        c.setBackground(new Color(235, 222, 240)); // Morado pastel
                        c.setForeground(new Color(74, 35, 90));
                        break;
                    default: // SIN CRÉDITO
                        c.setBackground(new Color(213, 245, 227)); // Verde pastel
                        c.setForeground(new Color(20, 90, 50));
                        break;
                }
                return c;
            }
        });
    }
}
