package Controlador;

import Fabrica.OfertaDecorator;
import Modelo.Cuota;
import Modelo.Movimiento;
import Modelo.OfertaCredito;
import Modelo.Usuario;
import Vista.EstiloUI;
import Vista.PanelGraficoBarras;
import Vista.PanelGraficoPastel;
import Vista.PnlAdminCredito;
import java.awt.Color;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

        // 1. Calcular ingreso promedio mensual desde UsuarioDAO
        double totalIngresos = 0;
        Set<YearMonth> mesesActivos = new HashSet<>();
        List<Movimiento> movimientos = usuarioDAO.listarMovimientosUsuario(cliente.getIdUsuario());
        for (Movimiento m : movimientos) {
            if ("DEPOSITO".equals(m.getTipo()) || "TRANSFERENCIA_RECIBIDA".equals(m.getTipo())) {
                totalIngresos += m.getMonto();
                mesesActivos.add(YearMonth.from(m.getFechaOperacion()));
            }
        }
        double ingresoPromedioMensual = totalIngresos / Math.max(1, mesesActivos.size());

        // 2. Consultar historial de cuotas desde CreditoDAO
        List<Cuota> cuotas = creditoDAO.listarHistorialCuotasUsuario(cliente.getIdUsuario());
        LocalDate fechaRef = (getSesion().getFechaSimulada() != null) ? getSesion().getFechaSimulada() : LocalDate.now();

        for (Cuota c : cuotas) {
            String estado = c.getEstadoCuota();
            if ("PAGADA_A_TIEMPO".equals(estado)) {
                puntuales++;
            } else if ("ATRASADA".equals(estado)) {
                atrasadas++;
                LocalDate fPago = (c.getFechaPago() != null) ? c.getFechaPago() : fechaRef;
                long diasAtraso = Math.max(1, ChronoUnit.DAYS.between(c.getFechaVencimiento(), fPago));
                if (diasAtraso <= 2) dia1++;
                else if (diasAtraso <= 6) dia3++;
                else dia7Mas++;
            } else if ("PENDIENTE".equals(estado)) {
                porPagar++;
            }
        }

        int totalCuotas = puntuales + atrasadas + porPagar;

        // Panel izquierdo: PanelGraficoPastel | Panel derecho: PanelGraficoBarras
        EstiloUI.mostrarGrafico(vista.pnlGraficoPastel, new PanelGraficoPastel(puntuales, atrasadas, porPagar));
        EstiloUI.mostrarGrafico(vista.pnlGraficoBarras, new PanelGraficoBarras(dia1, dia3, dia7Mas));

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

        // 3. Evaluación de Riesgo y Crédito Sugerido
        double saldoActual = Math.max(0, cliente.getSaldo());
        String riesgo;
        double creditoSugerido;

        if (atrasadas >= 2 || dia7Mas > 0) {
            riesgo = "ALTO";
            creditoSugerido = Math.max(200.0, saldoActual * 0.40);
            vista.txtRiesgo.setForeground(new Color(192, 57, 43));
        } else if (atrasadas == 1 || ingresoPromedioMensual < 1300) {
            riesgo = "MEDIO";
            double base = Math.max(saldoActual, ingresoPromedioMensual);
            creditoSugerido = Math.max(500.0, base * 0.70);
            vista.txtRiesgo.setForeground(new Color(184, 134, 11));
        } else {
            riesgo = "BAJO";
            creditoSugerido = Math.max(1000.0, (ingresoPromedioMensual * 1.50) + (saldoActual * 0.30));
            vista.txtRiesgo.setForeground(new Color(30, 132, 73));
        }

        creditoSugerido = Math.round(creditoSugerido / 50.0) * 50.0;
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

        String riesgo = vista.txtRiesgo.getText().trim();
        OfertaCredito oferta = OfertaDecorator.decorarOfertaPorRiesgo(cliente.getIdUsuario(), riesgo, monto);
        LocalDate fecha = (getSesion().getFechaSimulada() != null) ? getSesion().getFechaSimulada() : LocalDate.now();

        if (creditoDAO.enviarOfertaCredito(oferta, fecha)) {
            JOptionPane.showMessageDialog(vista,
                    "Oferta de crédito por S/. " + String.format("%.2f", monto) + " enviada a " + cliente.getNombreCompleto() + ".\nEl cliente podrá elegir su plazo desde su panel.",
                    "Oferta Enviada", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(vista, "Error al enviar la oferta de crédito.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
