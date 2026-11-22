package Controlador;

import Modelo.Usuario;
import Vista.PnlAdminRegistrar;
import javax.swing.JOptionPane;

public class AdminRegistrarControlador extends BaseControlador {
    private final PnlAdminRegistrar vista;
    private final AdminDashboardControlador dashboardCtrl;
    private final AdminVentanillaControlador ventanillaProceso = new AdminVentanillaControlador();

    public AdminRegistrarControlador(PnlAdminRegistrar vista, String dniBuscado, AdminDashboardControlador dashboardCtrl) {
        this.vista = vista;
        this.dashboardCtrl = dashboardCtrl;
        inicializar(dniBuscado);
    }

    @Override
    public String obtenerNombreModulo() {
        return "Controlador Alta de Nuevo Cliente";
    }

    private void inicializar(String dniBuscado) {
        vista.txtRegDni.setText(dniBuscado);
        vista.txtRegDni.setEditable(false);

        vista.btnRegistrar.addActionListener(e -> registrar());
        vista.btnCancelar.addActionListener(e -> cancelar());
    }

    private void registrar() {
        String dni = vista.txtRegDni.getText().trim();
        String res = ventanillaProceso.registrarNuevoCliente(
                dni,
                vista.txtRegNombres.getText(),
                vista.txtRegApellidos.getText(),
                vista.txtRegCelular.getText(),
                vista.txtRegUsuario.getText(),
                vista.txtRegPassword.getText(),
                vista.txtRegSaldo.getText()
        );

        if ("OK".equals(res)) {
            JOptionPane.showMessageDialog(vista,
                    "Cliente registrado y cuenta bancaria creada con éxito.",
                    "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
            Usuario nuevoCliente = ventanillaProceso.buscarClientePorDni(dni);
            dashboardCtrl.setClienteActual(nuevoCliente);
            dashboardCtrl.abrirPestanaInfo();
        } else {
            JOptionPane.showMessageDialog(vista, res, "Error en Registro", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cancelar() {
        dashboardCtrl.setClienteActual(null);
        dashboardCtrl.abrirPestanaInfo();
    }
}
