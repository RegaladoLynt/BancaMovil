package Controlador;

import Conexion.BaseDatosConexion;
import Modelo.Usuario;
import Vista.EstiloUI;
import Vista.PanelGraficoBalance;
import Vista.PnlEstadistica;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;

public class AdminEstadisticaControlador extends BaseControlador {
    private final PnlEstadistica pnl;
    private final Usuario cliente;

    public AdminEstadisticaControlador(PnlEstadistica pnl, Usuario cliente) {
        this.pnl = pnl;
        this.cliente = cliente;
        inicializar();
    }

    @Override
    public String obtenerNombreModulo() {
        return "Controlador de Estadística Mensual";
    }

    private void inicializar() {
        if (getSesion().getFechaSimulada() != null) {
            pnl.txtMes.setText(getSesion().getFechaSimulada().format(DateTimeFormatter.ofPattern("MM/yyyy")));
        }
        pnl.btnConsultar.addActionListener(e -> calcularBalance());
    }

    private void calcularBalance() {
        String mesAnio = pnl.txtMes.getText().trim();
        if (!mesAnio.matches("\\d{2}/\\d{4}")) {
            JOptionPane.showMessageDialog(pnl, "Ingrese el mes en formato MM/yyyy (Ejemplo: 11/2026).", "Formato Inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringTokenizer st = new StringTokenizer(mesAnio, "/");
        int mes = Integer.parseInt(st.nextToken());
        int anio = Integer.parseInt(st.nextToken());
        LocalDate finDeMes = YearMonth.of(anio, mes).atEndOfMonth();

        double ingresos = 0;
        double egresos = 0;
        double saldoCierreMes = 0;

        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();

        // 1. Ingresos y egresos únicamente del mes consultado
        String sqlMes = "SELECT tipo, monto FROM transacciones WHERE id_usuario = ? AND MONTH(fecha_transaccion) = ? AND YEAR(fecha_transaccion) = ?";
        try (PreparedStatement ps = cn.prepareStatement(sqlMes)) {
            ps.setInt(1, cliente.getIdUsuario());
            ps.setInt(2, mes);
            ps.setInt(3, anio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    double monto = rs.getDouble("monto");
                    if (tipo.contains("DEPOSITO") || tipo.contains("RECIBIDA") || tipo.contains("DESEMBOLSO")) {
                        ingresos += monto;
                    } else {
                        egresos += monto;
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error balance mensual: " + ex.getMessage());
        }

        // 2. Saldo real acumulado con el que terminó ese mes (hasta el último día del mes)
        String sqlAcum = "SELECT tipo, monto FROM transacciones WHERE id_usuario = ? AND fecha_transaccion <= ?";
        try (PreparedStatement ps = cn.prepareStatement(sqlAcum)) {
            ps.setInt(1, cliente.getIdUsuario());
            ps.setDate(2, java.sql.Date.valueOf(finDeMes));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    double monto = rs.getDouble("monto");
                    if (tipo.contains("DEPOSITO") || tipo.contains("RECIBIDA") || tipo.contains("DESEMBOLSO")) {
                        saldoCierreMes += monto;
                    } else {
                        saldoCierreMes -= monto;
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error saldo cierre de mes: " + ex.getMessage());
        }

        double diferencia = ingresos - egresos;
        pnl.txtIngresos.setText(String.format("S/. %.2f", ingresos));
        pnl.txtEgresos.setText(String.format("S/. %.2f", egresos));
        pnl.txtSaldo.setText(String.format("S/. %.2f", saldoCierreMes));

        if (diferencia >= 0) {
            pnl.txtResultado.setText("Ganancia:");
            pnl.txtResultado.setForeground(new Color(30, 132, 73));
            pnl.txtFinal.setText(String.format("S/. %.2f", diferencia));
            pnl.txtFinal.setForeground(new Color(30, 132, 73));
        } else {
            pnl.txtResultado.setText("Déficit:");
            pnl.txtResultado.setForeground(new Color(192, 57, 43));
            pnl.txtFinal.setText(String.format("S/. %.2f", Math.abs(diferencia)));
            pnl.txtFinal.setForeground(new Color(192, 57, 43));
        }

        if (diferencia < 0) {
            pnl.txtMensaje.setText("Tenga cuidado con sus Gastos se ha detectado perdidas.");
            pnl.txtMensaje.setForeground(new Color(192, 57, 43));
        } else if (diferencia < 200) {
            pnl.txtMensaje.setText("Vas por buen camino pero aún podemos mejorar.");
            pnl.txtMensaje.setForeground(new Color(184, 134, 11));
        } else {
            pnl.txtMensaje.setText("Excelente!!");
            pnl.txtMensaje.setForeground(new Color(30, 132, 73));
        }

        EstiloUI.cambiarVista(pnl.PnlRepresentacion, new PanelGraficoBalance(ingresos, egresos));
    }
}
