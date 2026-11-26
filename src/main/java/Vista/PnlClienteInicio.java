package Vista;

import Conexion.SesionSistema;
import Controlador.AtencionControlador;
import Controlador.ClienteControlador;
import Modelo.Cuenta;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/** Resumen de cuenta, últimos movimientos y alertas de vencimiento. */
public class PnlClienteInicio extends JPanel implements Refrescable {

    private final ClienteControlador controlador = new ClienteControlador();
    private final AtencionControlador atencion = new AtencionControlador();

    private final JLabel lblCuenta = new JLabel("-");
    private final JLabel lblTipo = new JLabel("-");
    private final JLabel lblSaldo = new JLabel("S/. 0.00");
    private final JLabel lblLimite = new JLabel("-");
    private final JTable tblMovimientos = new JTable();
    private final DefaultListModel<String> modeloAlertas = new DefaultListModel<>();

    public PnlClienteInicio() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 360));
        setLayout(new BorderLayout(0, 8));

        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 8));
        cabecera.setBackground(EstiloUI.AZUL_OSCURO);
        cabecera.add(datoClaro("Cuenta", lblCuenta));
        cabecera.add(datoClaro("Tipo", lblTipo));
        cabecera.add(datoClaro("Saldo disponible", lblSaldo));
        cabecera.add(datoClaro("Límite diario", lblLimite));
        lblSaldo.setForeground(new Color(255, 215, 90));
        add(cabecera, BorderLayout.NORTH);

        ClienteUI.tabla(tblMovimientos);
        JScrollPane scrollMov = new JScrollPane(tblMovimientos);
        scrollMov.setBorder(BorderFactory.createTitledBorder("Últimos movimientos"));
        scrollMov.setBackground(Color.WHITE);

        JList<String> lstAlertas = new JList<>(modeloAlertas);
        lstAlertas.setForeground(EstiloUI.ROJO_SALIR);
        lstAlertas.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane scrollAl = new JScrollPane(lstAlertas);
        scrollAl.setBorder(BorderFactory.createTitledBorder("Alertas de vencimiento"));
        scrollAl.setPreferredSize(new Dimension(800, 90));
        scrollAl.setBackground(Color.WHITE);

        add(scrollMov, BorderLayout.CENTER);
        add(scrollAl, BorderLayout.SOUTH);
    }

    private JPanel datoClaro(String titulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel t = new JLabel(titulo);
        t.setForeground(new Color(190, 200, 220));
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        valor.setForeground(Color.WHITE);
        valor.setFont(new Font("Segoe UI", Font.BOLD, 16));
        p.add(t, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    @Override
    public void refrescar() {
        Cuenta c = controlador.obtenerCuentaActual();
        if (c != null) {
            lblCuenta.setText(c.getNumeroCuenta());
            lblTipo.setText(c.getTipoCuenta());
            lblSaldo.setText(ProcesoVista.formatearSoles(c.getSaldo()));
            lblLimite.setText(ProcesoVista.formatearSoles(c.getLimiteDiario()));
        }
        tblMovimientos.setModel(ProcesoVista.construirModeloTablaMovimientos(
                controlador.obtenerMovimientosActuales()));

        modeloAlertas.clear();
        SesionSistema s = SesionSistema.getInstancia();
        List<String> alertas = atencion.obtenerAlertasReloj(s.getFechaSimulada(), s.getIdUsuario());
        if (alertas.isEmpty()) {
            modeloAlertas.addElement("Sin alertas pendientes.");
        } else {
            alertas.forEach(modeloAlertas::addElement);
        }
    }
}
