package Controlador;

import Modelo.Usuario;
import Vista.PnlAdminInfo;
import java.awt.Color;
import javax.swing.JOptionPane;

public class AdminInfoControlador extends BaseControlador {
    private final PnlAdminInfo vista;
    private final Usuario cliente;
    private final AdminVentanillaControlador ventanillaProceso = new AdminVentanillaControlador();

    public AdminInfoControlador(PnlAdminInfo vista, Usuario cliente) {
        this.vista = vista;
        this.cliente = cliente;
        inicializar();
    }

    @Override
    public String obtenerNombreModulo() {
        return "Controlador Expediente y Ventanilla";
    }

    private void inicializar() {
        cargarDatosCliente();
        vista.btnDepositar.addActionListener(e -> ejecutarOperacion(true));
        vista.btnRetiro.addActionListener(e -> ejecutarOperacion(false));
    }

    private void cargarDatosCliente() {
        if (cliente == null) return;
        vista.lblNombreCompleto.setText(cliente.getNombreCompleto());
        vista.lblDniCliente.setText(cliente.getDni());
        vista.lblCelularCliente.setText(cliente.getCelular());
        vista.lblNumeroCuenta.setText(cliente.getNumeroCuenta() != null ? cliente.getNumeroCuenta() : "Sin cuenta");
        vista.lblSaldoActual.setText(String.format("S/. %.2f", cliente.getSaldo()));
        vista.lblSaldoActual.setForeground(new Color(30, 132, 73));

        String estado = cliente.getEstado() != null ? cliente.getEstado() : "ACTIVO";
        vista.lblEstadoCliente.setText(estado);
        vista.lblEstadoCliente.setForeground("ACTIVO".equalsIgnoreCase(estado)
                ? new Color(30, 132, 73)
                : new Color(169, 50, 38));
    }

    private void ejecutarOperacion(boolean esDeposito) {
        String montoTexto = esDeposito ? vista.txtDepositar.getText() : vista.txtRetiro.getText();
        String res = ventanillaProceso.operarVentanilla(cliente, montoTexto, esDeposito);

        if ("OK".equals(res)) {
            cargarDatosCliente();
            if (esDeposito) {
                vista.txtDepositar.setText("");
            } else {
                vista.txtRetiro.setText("");
            }
            JOptionPane.showMessageDialog(vista,
                    (esDeposito ? "Depósito" : "Retiro") + " realizado con éxito.\nNuevo saldo: S/. " + String.format("%.2f", cliente.getSaldo()),
                    "Operación Exitosa", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(vista, res, "Error en Ventanilla", JOptionPane.WARNING_MESSAGE);
        }
    }
}
