package Vista;

import Controlador.ClienteControlador;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Pago de servicios (luz, agua, telefonía...) vía pasarela externa. */
public class PnlClienteServicios extends JPanel implements Refrescable {

    private final ClienteControlador controlador = new ClienteControlador();
    private final Runnable alCambiarSaldo;
    private final JComboBox<String> cmbEmpresa = new JComboBox<>(new String[]{
        "Luz del Sur", "Sedapal", "Movistar", "Claro", "Entel"});
    private final JTextField txtSuministro = new JTextField();
    private final JTextField txtMonto = new JTextField();

    public PnlClienteServicios(Runnable alCambiarSaldo) {
        this.alCambiarSaldo = alCambiarSaldo;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 360));
        setLayout(null);

        JPanel barra = new JPanel(null);
        barra.setBackground(EstiloUI.AZUL_OSCURO);
        barra.setBounds(0, 0, 800, 40);
        JLabel t = new JLabel("PAGO DE SERVICIOS");
        t.setForeground(Color.WHITE);
        t.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        t.setBounds(20, 8, 300, 24);
        barra.add(t);
        add(barra);

        ClienteUI.campo(cmbEmpresa, "Empresa");
        cmbEmpresa.setBounds(200, 60, 400, 60);
        add(cmbEmpresa);

        ClienteUI.campo(txtSuministro, "N° de suministro / teléfono");
        txtSuministro.setBounds(200, 125, 400, 60);
        add(txtSuministro);

        ClienteUI.campo(txtMonto, "Monto a pagar (S/.)");
        txtMonto.setBounds(200, 190, 400, 60);
        add(txtMonto);

        JButton btn = ClienteUI.boton("PAGAR SERVICIO", EstiloUI.DORADO);
        btn.setBounds(300, 270, 200, 45);
        btn.addActionListener(e -> pagar());
        add(btn);
    }

    private void pagar() {
        String suministro = txtSuministro.getText().trim();
        double monto = ClienteUI.leerMonto(txtMonto.getText());
        if (suministro.isEmpty() || monto <= 0) {
            JOptionPane.showMessageDialog(this, "Ingrese el suministro y un monto válido.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (controlador.pagarServicio((String) cmbEmpresa.getSelectedItem(), suministro, monto)) {
            JOptionPane.showMessageDialog(this, "Pago de servicio realizado con éxito.",
                    "Operación exitosa", JOptionPane.INFORMATION_MESSAGE);
            txtSuministro.setText("");
            txtMonto.setText("");
            alCambiarSaldo.run();
        }
    }

    @Override
    public void refrescar() { }
}
