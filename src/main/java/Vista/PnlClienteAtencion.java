package Vista;

import Conexion.SesionSistema;
import Controlador.AtencionControlador;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

/** Registro de quejas/sugerencias y consulta de las respuestas del administrador. */
public class PnlClienteAtencion extends JPanel implements Refrescable {

    private final AtencionControlador controlador = new AtencionControlador();
    private final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"QUEJA", "SUGERENCIA"});
    private final JTextArea txtMensaje = new JTextArea();
    private final JTable tblHistorial = new JTable();

    public PnlClienteAtencion() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 360));
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Formulario (izquierda) ---
        JPanel form = new JPanel(new BorderLayout(0, 8));
        form.setBackground(Color.WHITE);
        form.setPreferredSize(new Dimension(300, 340));
        form.add(ClienteUI.titulo("Nuevo registro"), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setBackground(Color.WHITE);
        ClienteUI.campo(cmbTipo, "Tipo");
        centro.add(cmbTipo, BorderLayout.NORTH);
        txtMensaje.setLineWrap(true);
        txtMensaje.setWrapStyleWord(true);
        txtMensaje.setBackground(ClienteUI.CAMPO);
        JScrollPane sp = new JScrollPane(txtMensaje);
        sp.setBorder(BorderFactory.createTitledBorder("Mensaje"));
        centro.add(sp, BorderLayout.CENTER);
        form.add(centro, BorderLayout.CENTER);

        JButton btnEnviar = ClienteUI.boton("ENVIAR", EstiloUI.DORADO);
        btnEnviar.setPreferredSize(new Dimension(300, 40));
        btnEnviar.addActionListener(e -> enviar());
        form.add(btnEnviar, BorderLayout.SOUTH);
        add(form, BorderLayout.WEST);

        // --- Historial (derecha) ---
        JPanel hist = new JPanel(new BorderLayout(0, 8));
        hist.setBackground(Color.WHITE);
        hist.add(ClienteUI.titulo("Mis registros y respuestas"), BorderLayout.NORTH);
        ClienteUI.tabla(tblHistorial);
        hist.add(new JScrollPane(tblHistorial), BorderLayout.CENTER);
        add(hist, BorderLayout.CENTER);
    }

    private void enviar() {
        SesionSistema s = SesionSistema.getInstancia();
        String res = controlador.enviarAtencion(s.getIdUsuario(),
                (String) cmbTipo.getSelectedItem(), txtMensaje.getText(), s.getFechaSimulada());
        if ("OK".equals(res)) {
            JOptionPane.showMessageDialog(this, "Tu mensaje fue registrado. ¡Gracias!",
                    "Atención al cliente", JOptionPane.INFORMATION_MESSAGE);
            txtMensaje.setText("");
            refrescar();
        } else {
            JOptionPane.showMessageDialog(this, res, "Atención al cliente", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void refrescar() {
        tblHistorial.setModel(controlador.obtenerTablaAtencion(SesionSistema.getInstancia().getIdUsuario()));
    }
}
