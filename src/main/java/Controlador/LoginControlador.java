package Controlador;

import Vista.FrmDashboardAdmin;
import Vista.FrmDashboardCliente;
import Vista.FrmLogin;
import java.util.List;
import javax.swing.JOptionPane;

public class LoginControlador extends BaseControlador {
    private final FrmLogin vista;
    private final AuthControlador authProceso = new AuthControlador();

    public LoginControlador(FrmLogin vista) {
        this.vista = vista;
        inicializar();
    }

    @Override
    public String obtenerNombreModulo() {
        return "Controlador de Vista Login";
    }

    private void inicializar() {
        vista.setLocationRelativeTo(null);
        String fechaMin = authProceso.obtenerFechaMinimaTexto();
        vista.txtFechamin.setText(fechaMin);
        vista.txtFecha.setText(fechaMin);

        vista.btnIngresar.addActionListener(e -> procesarIngreso());
        vista.btnSalir.addActionListener(e -> System.exit(0));
    }

    private void procesarIngreso() {
        String res = authProceso.iniciarSesion(
                vista.txtUsuario.getText(),
                new String(vista.txtPassword.getPassword()),
                vista.txtFecha.getText()
        );

        if (res.startsWith("OK:")) {
            String rol = res.split(":")[1];
            if ("ADMIN".equals(rol)) {
                FrmDashboardAdmin adminVista = new FrmDashboardAdmin();
                new AdminDashboardControlador(adminVista);
                adminVista.setVisible(true);
            } else {
                int idCliente = getSesion().getIdUsuario();
                List<String> alertas = authProceso.obtenerAlertasClienteAlIngresar(getSesion().getFechaSimulada(), idCliente);
                if (!alertas.isEmpty()) {
                    JOptionPane.showMessageDialog(vista,
                            "Recordatorio de Pago:\n• " + String.join("\n• ", alertas),
                            "Aviso de Vencimiento", JOptionPane.WARNING_MESSAGE);
                }
                FrmDashboardCliente clienteVista = new FrmDashboardCliente();
                clienteVista.setVisible(true);
            }
            vista.dispose();
        } else {
            JOptionPane.showMessageDialog(vista, res, "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
        }
    }
}
