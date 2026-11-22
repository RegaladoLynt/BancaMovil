package Controlador;

import Modelo.Usuario;
import javax.swing.JPanel;

public interface VistaAdmin {
    JPanel construirPanel(Usuario cliente, AdminDashboardControlador dashboard);
}
