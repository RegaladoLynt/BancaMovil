package Controlador;

import Conexion.BaseDatosConexion;
import Modelo.Usuario;
import Vista.EstiloUI;
import Vista.PanelGraficoAtrasos;
import Vista.PanelGraficoPuntualidad;
import Vista.PnlAdminCredito;
import java.awt.Color;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.swing.JOptionPane;

public class AdminCreditoControlador extends BaseControlador {
    private final PnlAdminCredito vista;
    private final Usuario cliente;

    public AdminCreditoControlador(PnlAdminCredito vista, Usuario cliente) {
        this.vista = vista;
        this.cliente = cliente;
        inicializar();
    }

    @Override
    public String obtenerNombreModulo() {
        return "Controlador Evaluación de Riesgo y Crédito";
    }

    private void inicializar() {
        evaluarCliente();
        vista.btnOfrecerCredito.addActionListener(e -> enviarOfertaCredito());
    }

    private void evaluarCliente() {
        int puntuales = 0, atrasadas = 0, porPagar = 0;
        int dia1 = 0, dia3 = 0, dia7Mas = 0;
        double ingresosTotales = 0;

        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();

        String sqlIng = "SELECT COALESCE(SUM(monto), 0) FROM transacciones WHERE id_usuario = ? AND (tipo LIKE '%DEPOSITO%' OR tipo LIKE '%RECIBIDA%' OR tipo LIKE '%DESEMBOLSO%')";
        try (PreparedStatement ps = cn.prepareStatement(sqlIng)) {
            ps.setInt(1, cliente.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) ingresosTotales = rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.out.println("Error ingresos crédito: " + e.getMessage());
        }

        String sqlCuotas = "SELECT c.estado, c.fecha_vencimiento, c.fecha_pago FROM cuotas c " +
                           "INNER JOIN creditos cr ON c.id_credito = cr.id_credito WHERE cr.id_usuario = ?";
        try (PreparedStatement ps = cn.prepareStatement(sqlCuotas)) {
            ps.setInt(1, cliente.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String estado = rs.getString("estado");
                    LocalDate venc = rs.getDate("fecha_vencimiento").toLocalDate();
                    Date fPagoSql = rs.getDate("fecha_pago");

                    if ("PAGADA_A_TIEMPO".equals(estado)) {
                        puntuales++;
                    } else if ("PAGADA_CON_MORA".equals(estado) || "ATRASADA".equals(estado)) {
                        atrasadas++;
                        LocalDate ref = (fPagoSql != null) ? fPagoSql.toLocalDate()
                                : (getSesion().getFechaSimulada() != null ? getSesion().getFechaSimulada() : LocalDate.now());
                        long diasAtraso = Math.max(1, ChronoUnit.DAYS.between(venc, ref));
                        if (diasAtraso <= 2) dia1++;
                        else if (diasAtraso <= 6) dia3++;
                        else dia7Mas++;
                    } else if ("POR_PAGAR".equals(estado)) {
                        porPagar++;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error cuotas crédito: " + e.getMessage());
        }

        int totalCuotas = puntuales + atrasadas + porPagar;

        EstiloUI.cambiarVista(vista.pnlGraficoPastel, new PanelGraficoPuntualidad(puntuales, atrasadas, porPagar));
        EstiloUI.cambiarVista(vista.pnlGraficoBarras, new PanelGraficoAtrasos(dia1, dia3, dia7Mas));

        if (totalCuotas == 0) {
            vista.lblLeyendaPastel.setText("");
            vista.lblResultadoPastel.setText("No cuenta con deudas actuales");
            vista.lblLeyendaBarra.setText("");
            vista.lblResultadoBarra.setText("No cuenta con deudas actuales");
        } else {
            vista.lblLeyendaPastel.setText("<html><font color='#52BE80'>■</font> Puntual &nbsp; <font color='#EC7063'>■</font> Impuntual &nbsp; <font color='#AF7AC5'>■</font> Letras faltantes</html>");
            int evaluadas = puntuales + atrasadas;
            if (evaluadas == 0) {
                vista.lblResultadoPastel.setText("Aún no registra cuotas vencidas ni pagadas");
            } else if (puntuales >= atrasadas) {
                int pct = (int) Math.round((puntuales * 100.0) / evaluadas);
                vista.lblResultadoPastel.setText("El " + pct + "% ha sido puntual");
            } else {
                int pct = (int) Math.round((atrasadas * 100.0) / evaluadas);
                vista.lblResultadoPastel.setText("El " + pct + "% ha sido impuntual");
            }

            int totalAtrasos = dia1 + dia3 + dia7Mas;
            if (totalAtrasos == 0) {
                vista.lblLeyendaBarra.setText("");
                vista.lblResultadoBarra.setText("No cuenta con retrasos actuales");
            } else {
                vista.lblLeyendaBarra.setText("<html><font color='#F4D03F'>■</font> 1 día &nbsp; <font color='#EB984E'>■</font> 3 días &nbsp; <font color='#EC7063'>■</font> 7+ días</html>");
                if (dia7Mas >= dia3 && dia7Mas >= dia1) {
                    vista.lblResultadoBarra.setText("Por lo general se demora en pagar 7+ días");
                } else if (dia3 >= dia1) {
                    vista.lblResultadoBarra.setText("Por lo general se demora en pagar 3 días");
                } else {
                    vista.lblResultadoBarra.setText("Por lo general se demora en pagar 1 día");
                }
            }
        }

        double baseCapacidad = Math.max(ingresosTotales, cliente.getSaldo());
        String riesgo;
        double creditoSugerido;

        if (atrasadas >= 2 || dia7Mas > 0) {
            riesgo = "ALTO";
            creditoSugerido = Math.max(300.0, baseCapacidad * 0.40);
            vista.txtRiesgo.setForeground(new Color(192, 57, 43));
        } else if (atrasadas == 1 || baseCapacidad < 800) {
            riesgo = "MEDIO";
            creditoSugerido = Math.max(800.0, baseCapacidad * 0.80);
            vista.txtRiesgo.setForeground(new Color(184, 134, 11));
        } else {
            riesgo = "BAJO";
            creditoSugerido = Math.max(1500.0, baseCapacidad * 1.50);
            vista.txtRiesgo.setForeground(new Color(30, 132, 73));
        }

        vista.txtRiesgo.setText(riesgo);
        vista.txtCredito.setText(String.format("%.2f", creditoSugerido));
    }

    private void enviarOfertaCredito() {
        double monto;
        try {
            monto = Double.parseDouble(vista.txtCredito.getText().trim().replace(",", "."));
            if (monto <= 0) {
                JOptionPane.showMessageDialog(vista, "El monto del crédito debe ser mayor a S/. 0.00.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(vista, "Ingrese un monto numérico válido para ofrecer.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate fecha = (getSesion().getFechaSimulada() != null) ? getSesion().getFechaSimulada() : LocalDate.now();
        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try {
            try (PreparedStatement psOld = cn.prepareStatement("UPDATE ofertas_credito SET estado = 'REEMPLAZADA' WHERE id_usuario = ? AND estado = 'PENDIENTE'")) {
                psOld.setInt(1, cliente.getIdUsuario());
                psOld.executeUpdate();
            }

            String sql = "INSERT INTO ofertas_credito (id_usuario, monto_ofrecido, estado, fecha_oferta) VALUES (?, ?, 'PENDIENTE', ?)";
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, cliente.getIdUsuario());
                ps.setDouble(2, monto);
                ps.setDate(3, Date.valueOf(fecha));
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(vista,
                    "Oferta de crédito por S/. " + String.format("%.2f", monto) + " enviada a " + cliente.getNombreCompleto() + ".\nEl cliente podrá elegir su plazo e interés desde su panel.",
                    "Oferta Enviada", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(vista, "Error al enviar oferta de crédito: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }
}
