package Controlador;

import Modelo.Usuario;
import Vista.*;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

public class AdminDashboardControlador extends BaseControlador {
    private final FrmDashboardAdmin vista;
    private final AdminVentanillaControlador ventanillaCtrl = new AdminVentanillaControlador();
    private Usuario clienteActual = null;

    public AdminDashboardControlador(FrmDashboardAdmin vista) {
        this.vista = vista;
        inicializar();
    }

    @Override
    public String obtenerNombreModulo() {
        return "Orquestador Panel Administrador";
    }

    private void inicializar() {
        vista.setLocationRelativeTo(null);
        vista.txtInfoAdmin.setText("ADMIN: " + getSesion().getNombreCompleto());
        String fechaTxt = (getSesion().getFechaSimulada() != null)
                ? getSesion().getFechaSimulada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        vista.txtFecha.setText("FECHA: " + fechaTxt);

        vista.btnCerrarSesion.addActionListener(e -> cerrarSesion());
        vista.btnBuscarDni.addActionListener(e -> buscarCliente());

        vista.btnInfo.addActionListener(e -> navegarA(vista.btnInfo, new VistasAdminFactory.VistaInfo()));
        vista.btnHistorial.addActionListener(e -> navegarA(vista.btnHistorial, new VistasAdminFactory.VistaHistorial()));
        vista.btnCredito.addActionListener(e -> navegarA(vista.btnCredito, new VistasAdminFactory.VistaCredito()));
        vista.btnAtencionCliente.addActionListener(e -> navegarA(vista.btnAtencionCliente, new VistasAdminFactory.VistaAtencion()));
        vista.Notificaciones.addActionListener(e -> navegarA(vista.Notificaciones, new VistasAdminFactory.VistaNotificaciones()));

        abrirPestanaInfo();
    }

    public void navegarA(JToggleButton botonActivo, VistaAdmin estrategiaVista) {
        EstiloUI.aplicarEstiloPestanas(botonActivo,
                vista.btnInfo, vista.btnHistorial, vista.btnCredito, vista.btnAtencionCliente, vista.Notificaciones);
        EstiloUI.cambiarVista(vista.pnlContenido, estrategiaVista.construirPanel(clienteActual, this));
    }

    public void abrirPestanaInfo() {
        navegarA(vista.btnInfo, new VistasAdminFactory.VistaInfo());
    }

    private void buscarCliente() {
        String dni = vista.txtBuscarDni.getText().trim();
        if (!dni.matches("\\d{8}")) {
            JOptionPane.showMessageDialog(vista, "Ingrese un DNI válido de 8 dígitos numéricos.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario encontrado = ventanillaCtrl.buscarClientePorDni(dni);
        if (encontrado != null) {
            this.clienteActual = encontrado;
            abrirPestanaInfo();
        } else {
            this.clienteActual = null;
            EstiloUI.aplicarEstiloPestanas(vista.btnInfo,
                    vista.btnInfo, vista.btnHistorial, vista.btnCredito, vista.btnAtencionCliente, vista.Notificaciones);
            PnlAdminRegistrar pnlReg = new PnlAdminRegistrar();
            new AdminRegistrarControlador(pnlReg, dni, this);
            EstiloUI.cambiarVista(vista.pnlContenido, pnlReg);
        }
    }

    private void cerrarSesion() {
        getSesion().cerrarSesion();
        FrmLogin login = new FrmLogin();
        new LoginControlador(login);
        login.setVisible(true);
        vista.dispose();
    }

    public FrmDashboardAdmin getVista() { return vista; }
    public Usuario getClienteActual() { return clienteActual; }
    public void setClienteActual(Usuario clienteActual) { this.clienteActual = clienteActual; }
}
