package Controlador;

import Modelo.Usuario;
import Vista.*;
import javax.swing.JPanel;

public class VistasAdminFactory {

    private static JPanel crearVistaVacia(AdminDashboardControlador dash) {
        PnlAdminVacio pnl = new PnlAdminVacio();
        new AdminVacioControlador(pnl, dash.getVista());
        return pnl;
    }

    public static class VistaInfo implements VistaAdmin {
        @Override
        public JPanel construirPanel(Usuario cliente, AdminDashboardControlador dash) {
            if (cliente == null) return crearVistaVacia(dash);
            PnlAdminInfo pnl = new PnlAdminInfo();
            new AdminInfoControlador(pnl, cliente);
            return pnl;
        }
    }

    public static class VistaHistorial implements VistaAdmin {
        @Override
        public JPanel construirPanel(Usuario cliente, AdminDashboardControlador dash) {
            if (cliente == null) return crearVistaVacia(dash);
            PnlHistorial pnl = new PnlHistorial();
            new AdminHistorialControlador(pnl, cliente);
            return pnl;
        }
    }

    public static class VistaCredito implements VistaAdmin {
        @Override
        public JPanel construirPanel(Usuario cliente, AdminDashboardControlador dash) {
            if (cliente == null) return crearVistaVacia(dash);
            PnlAdminCredito pnl = new PnlAdminCredito();
            new AdminCreditoControlador(pnl, cliente);
            return pnl;
        }
    }

    public static class VistaAtencion implements VistaAdmin {
        @Override
        public JPanel construirPanel(Usuario cliente, AdminDashboardControlador dash) {
            PnlAdminAtencion pnl = new PnlAdminAtencion();
            new AdminAtencionControlador(pnl);
            return pnl;
        }
    }

    public static class VistaNotificaciones implements VistaAdmin {
        @Override
        public JPanel construirPanel(Usuario cliente, AdminDashboardControlador dash) {
            PnlAdminNotificaciones pnl = new PnlAdminNotificaciones();
            new AdminNotifControlador(pnl);
            return pnl;
        }
    }
}
