package Vista;

import Controlador.ClienteControlador;
import Modelo.Credito;
import Modelo.Cuota;
import Modelo.OfertaCredito;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;

/** Oferta de crédito preaprobada, cronograma de cuotas y pago de cuotas. */
public class PnlClienteCredito extends JPanel implements Refrescable {

    private final ClienteControlador controlador = new ClienteControlador();
    private final Runnable alCambiarSaldo;

    private final JLabel lblOferta = new JLabel("-");
    private final JSpinner spnCuotas = new JSpinner(new SpinnerNumberModel(6, 1, 60, 1));
    private final JButton btnAceptar = ClienteUI.boton("ACEPTAR OFERTA", EstiloUI.DORADO);
    private final JLabel lblCredito = new JLabel("-");
    private final JTable tblCuotas = new JTable();
    private final JButton btnPagar = ClienteUI.boton("PAGAR CUOTA SELECCIONADA", EstiloUI.AZUL_MEDIO);

    private OfertaCredito ofertaActual;
    private List<Cuota> cuotasActuales = new ArrayList<>();

    public PnlClienteCredito(Runnable alCambiarSaldo) {
        this.alCambiarSaldo = alCambiarSaldo;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 360));
        setLayout(new BorderLayout(0, 6));

        // --- Oferta ---
        JPanel pnlOferta = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlOferta.setBackground(EstiloUI.AZUL_OSCURO);
        lblOferta.setForeground(Color.WHITE);
        lblOferta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel lblN = new JLabel("N° de cuotas:");
        lblN.setForeground(Color.WHITE);
        lblN.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAceptar.addActionListener(e -> aceptarOferta());
        pnlOferta.add(lblOferta);
        pnlOferta.add(lblN);
        pnlOferta.add(spnCuotas);
        pnlOferta.add(btnAceptar);
        add(pnlOferta, BorderLayout.NORTH);

        // --- Cronograma ---
        JPanel centro = new JPanel(new BorderLayout(0, 4));
        centro.setBackground(Color.WHITE);
        lblCredito.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCredito.setForeground(EstiloUI.AZUL_OSCURO);
        lblCredito.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        centro.add(lblCredito, BorderLayout.NORTH);
        ClienteUI.tabla(tblCuotas);
        centro.add(new JScrollPane(tblCuotas), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        sur.setBackground(Color.WHITE);
        btnPagar.addActionListener(e -> pagarCuota());
        sur.add(btnPagar);
        add(sur, BorderLayout.SOUTH);
    }

    private void aceptarOferta() {
        if (ofertaActual == null) return;
        int cuotas = (Integer) spnCuotas.getValue();
        int r = JOptionPane.showConfirmDialog(this,
                "¿Aceptar el crédito de " + ProcesoVista.formatearSoles(ofertaActual.getMontoOfertado())
                + " a " + cuotas + " cuotas?", "Confirmar crédito", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;
        if (controlador.aceptarOfertaCredito(ofertaActual, cuotas)) {
            JOptionPane.showMessageDialog(this, "Crédito desembolsado en su cuenta.",
                    "Operación exitosa", JOptionPane.INFORMATION_MESSAGE);
            refrescar();
            alCambiarSaldo.run();
        }
    }

    private void pagarCuota() {
        int fila = tblCuotas.getSelectedRow();
        if (fila < 0 || fila >= cuotasActuales.size()) {
            JOptionPane.showMessageDialog(this, "Seleccione una cuota de la tabla.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Cuota cuota = cuotasActuales.get(fila);
        if (cuota.getFechaPago() != null) {
            JOptionPane.showMessageDialog(this, "Esa cuota ya fue pagada.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (controlador.pagarCuotaCredito(cuota)) {
            JOptionPane.showMessageDialog(this, "Cuota pagada con éxito.",
                    "Operación exitosa", JOptionPane.INFORMATION_MESSAGE);
            refrescar();
            alCambiarSaldo.run();
        }
    }

    @Override
    public void refrescar() {
        ofertaActual = controlador.obtenerOfertaDisponible();
        boolean hayOferta = ofertaActual != null;
        btnAceptar.setEnabled(hayOferta);
        spnCuotas.setEnabled(hayOferta);
        if (hayOferta) {
            lblOferta.setText(ofertaActual.getTituloAtractivo() + " | "
                    + ProcesoVista.formatearSoles(ofertaActual.getMontoOfertado())
                    + " | TEA " + ofertaActual.getTasaInteres() + "%");
            spnCuotas.setValue(Math.max(1, ofertaActual.getCuotasSugeridas()));
        } else {
            lblOferta.setText("No tiene ofertas de crédito disponibles.");
        }

        Credito credito = controlador.obtenerCreditoActivo();
        if (credito == null) {
            cuotasActuales = new ArrayList<>();
            lblCredito.setText("No tiene un crédito activo.");
            btnPagar.setEnabled(false);
        } else {
            cuotasActuales = controlador.obtenerCuotasConActualizacionMora(credito.getIdCredito());
            lblCredito.setText("Crédito activo: " + ProcesoVista.formatearSoles(credito.getMontoCapital())
                    + " en " + credito.getTotalCuotas() + " cuotas");
            btnPagar.setEnabled(true);
        }
        tblCuotas.setModel(ProcesoVista.construirModeloTablaCuotas(cuotasActuales));
    }
}
