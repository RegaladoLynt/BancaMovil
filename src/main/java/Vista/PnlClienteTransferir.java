package Vista;

import Controlador.ClienteControlador;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Transferencias entre cuentas (protegidas con token por TransaccionProxy). */
public class PnlClienteTransferir extends JPanel implements Refrescable {

    private final ClienteControlador controlador = new ClienteControlador();
    private final Runnable alCambiarSaldo;
    private final JTextField txtCuentaDestino = new JTextField();
    private final JTextField txtMonto = new JTextField();

    public PnlClienteTransferir(Runnable alCambiarSaldo) {
        this.alCambiarSaldo = alCambiarSaldo;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 360));
        setLayout(null);

        JPanel barra = new JPanel(null);
        barra.setBackground(EstiloUI.AZUL_OSCURO);
        barra.setBounds(0, 0, 800, 40);
        JLabel t = new JLabel("TRANSFERIR DINERO");
        t.setForeground(Color.WHITE);
        t.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        t.setBounds(20, 8, 300, 24);
        barra.add(t);
        add(barra);

        ClienteUI.campo(txtCuentaDestino, "Número de cuenta destino");
        txtCuentaDestino.setBounds(200, 80, 400, 60);
        add(txtCuentaDestino);

        ClienteUI.campo(txtMonto, "Monto (S/.)");
        txtMonto.setBounds(200, 150, 400, 60);
        add(txtMonto);

        JButton btn = ClienteUI.boton("TRANSFERIR", EstiloUI.DORADO);
        btn.setBounds(300, 235, 200, 45);
        btn.addActionListener(e -> transferir());
        add(btn);

        JLabel nota = new JLabel("Se solicitará un token de seguridad enviado a su celular.");
        nota.setForeground(EstiloUI.AZUL_MEDIO);
        nota.setBounds(240, 290, 400, 20);
        add(nota);
    }

    private void transferir() {
        String destino = txtCuentaDestino.getText().trim();
        double monto = ClienteUI.leerMonto(txtMonto.getText());
        if (destino.isEmpty() || monto <= 0) {
            JOptionPane.showMessageDialog(this, "Ingrese una cuenta destino y un monto válido.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (controlador.realizarTransferencia(destino, monto)) {
            JOptionPane.showMessageDialog(this, "Transferencia realizada con éxito.",
                    "Operación exitosa", JOptionPane.INFORMATION_MESSAGE);
            txtCuentaDestino.setText("");
            txtMonto.setText("");
            alCambiarSaldo.run();
        }
    }

    @Override
    public void refrescar() { }
}
