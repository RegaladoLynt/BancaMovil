package Controlador;

import Modelo.Movimiento;
import Modelo.Usuario;
import Vista.EstiloUI;
import Vista.PanelGraficoBalance;
import Vista.PnlEstadistica;
import java.awt.Color;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

        List<Movimiento> movimientos = usuarioDAO.listarMovimientosUsuario(cliente.getIdUsuario());
        for (Movimiento m : movimientos) {
            LocalDate f = m.getFechaOperacion();
            String tipo = m.getTipo();
            double monto = m.getMonto();
            boolean esIngreso = tipo.contains("DEPOSITO") || tipo.contains("RECIBIDA") || tipo.contains("DESEMBOLSO");

            if (f.getMonthValue() == mes && f.getYear() == anio) {
                if (esIngreso) ingresos += monto;
                else egresos += monto;
            }
            if (!f.isAfter(finDeMes)) {
                if (esIngreso) saldoCierreMes += monto;
                else saldoCierreMes -= monto;
            }
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
            pnl.txtTexto.setText("Tenga cuidado con sus Gastos se ha detectado perdidas.");
            pnl.txtTexto.setForeground(new Color(192, 57, 43));
        } else if (diferencia < 200) {
            pnl.txtTexto.setText("Vas por buen camino pero aún podemos mejorar.");
            pnl.txtTexto.setForeground(new Color(184, 134, 11));
        } else {
            pnl.txtTexto.setText("Excelente!!");
            pnl.txtTexto.setForeground(new Color(30, 132, 73));
        }

        EstiloUI.mostrarGrafico(pnl.PnlRepresentacion, new PanelGraficoBalance(ingresos, egresos));
    }
}
