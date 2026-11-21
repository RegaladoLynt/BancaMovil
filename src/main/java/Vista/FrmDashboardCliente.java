package Vista;

import Conexion.SesionSistema;
import Controlador.AuthControlador;
import Controlador.ClienteControlador;
import Modelo.Cuenta;
import Procesos.ProcesoVista;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;

/** Portal del cliente: cabecera con saldo, pestañas de navegación y paneles de contenido. */
public class FrmDashboardCliente extends JFrame {

    private static final String INICIO = "INICIO", TRANSFERIR = "TRANSFERIR",
            SERVICIOS = "SERVICIOS", CREDITO = "CREDITO", ATENCION = "ATENCION";

    private final ClienteControlador controlador = new ClienteControlador();
    private final AuthControlador auth = new AuthControlador();

    private final JLabel lblNombre = new JLabel();
    private final JLabel lblFecha = new JLabel();
    private final JLabel lblSaldo = new JLabel();

    private final CardLayout cards = new CardLayout();
    private final JPanel pnlContenido = new JPanel(cards);

    private final PnlClienteInicio pnlInicio = new PnlClienteInicio();
    private final PnlClienteTransferir pnlTransferir = new PnlClienteTransferir(this::actualizarSaldo);
    private final PnlClienteServicios pnlServicios = new PnlClienteServicios(this::actualizarSaldo);
    private final PnlClienteCredito pnlCredito = new PnlClienteCredito(this::actualizarSaldo);
    private final PnlClienteAtencion pnlAtencion = new PnlClienteAtencion();

    private final ButtonGroup navegacion = new ButtonGroup();
    private final JToggleButton btnInicio = pestana("Inicio", INICIO, pnlInicio);
    private final JToggleButton btnTransferir = pestana("Transferir", TRANSFERIR, pnlTransferir);
    private final JToggleButton btnServicios = pestana("Servicios", SERVICIOS, pnlServicios);
    private final JToggleButton btnCredito = pestana("Crédito", CREDITO, pnlCredito);
    private final JToggleButton btnAtencion = pestana("Atención al cliente", ATENCION, pnlAtencion);

    public FrmDashboardCliente() {
        setTitle(controlador.obtenerNombreModulo());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel raiz = new JPanel(null);
        raiz.setBackground(EstiloUI.BLANCO_HUMO);
        raiz.setPreferredSize(new Dimension(800, 500));

        // --- Cabecera ---
        SesionSistema s = SesionSistema.getInstancia();
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblNombre.setForeground(EstiloUI.AZUL_OSCURO);
        lblNombre.setText("Bienvenido, " + (s.getNombreCompleto() != null ? s.getNombreCompleto() : "Cliente"));
        lblNombre.setBounds(20, 10, 450, 24);
        raiz.add(lblNombre);

        LocalDate hoy = s.getFechaSimulada();
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblFecha.setForeground(EstiloUI.AZUL_OSCURO);
        lblFecha.setText("Fecha: " + (hoy != null ? hoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-"));
        lblFecha.setBounds(20, 40, 300, 20);
        raiz.add(lblFecha);

        lblSaldo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblSaldo.setForeground(EstiloUI.DORADO);
        lblSaldo.setBounds(20, 68, 400, 30);
        raiz.add(lblSaldo);

        JButton btnCerrar = ClienteUI.boton("CERRAR SESION", EstiloUI.ROJO_SALIR);
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrar.setBounds(670, 10, 120, 30);
        btnCerrar.addActionListener(e -> cerrarSesion());
        raiz.add(btnCerrar);

        // --- Pestañas ---
        JToggleButton[] tabs = {btnInicio, btnTransferir, btnServicios, btnCredito, btnAtencion};
        int x = 0;
        for (JToggleButton t : tabs) {
            t.setBounds(x, 110, 160, 30);
            raiz.add(t);
            x += 160;
        }

        // --- Contenido ---
        pnlContenido.setBackground(Color.WHITE);
        pnlContenido.add(pnlInicio, INICIO);
        pnlContenido.add(pnlTransferir, TRANSFERIR);
        pnlContenido.add(pnlServicios, SERVICIOS);
        pnlContenido.add(pnlCredito, CREDITO);
        pnlContenido.add(pnlAtencion, ATENCION);
        pnlContenido.setBounds(0, 140, 800, 360);
        raiz.add(pnlContenido);

        setContentPane(raiz);
        pack();
        setLocationRelativeTo(null);

        btnInicio.setSelected(true);
        mostrar(INICIO, pnlInicio);
        actualizarSaldo();
    }

    private JToggleButton pestana(String texto, String tarjeta, Refrescable panel) {
        JToggleButton b = new JToggleButton(texto);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        navegacion.add(b);
        b.addActionListener(e -> mostrar(tarjeta, panel));
        return b;
    }

    private void mostrar(String tarjeta, Refrescable panel) {
        cards.show(pnlContenido, tarjeta);
        panel.refrescar();
        EstiloUI.aplicarEstiloPestanas(seleccionada(), btnInicio, btnTransferir, btnServicios, btnCredito, btnAtencion);
    }

    private JToggleButton seleccionada() {
        for (JToggleButton b : new JToggleButton[]{btnInicio, btnTransferir, btnServicios, btnCredito, btnAtencion}) {
            if (b.isSelected()) return b;
        }
        return btnInicio;
    }

    /** Recarga el saldo de la cabecera y el resumen del panel de inicio. */
    private void actualizarSaldo() {
        Cuenta c = controlador.obtenerCuentaActual();
        lblSaldo.setText(c != null ? "Saldo: " + ProcesoVista.formatearSoles(c.getSaldo()) : "Saldo: -");
        pnlInicio.refrescar();
    }

    private void cerrarSesion() {
        auth.cerrarSesion();
        dispose();
        new FrmLogin().setVisible(true);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new FrmDashboardCliente().setVisible(true));
    }
}
