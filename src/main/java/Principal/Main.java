package Principal;

import Controlador.LoginControlador;
import Vista.FrmLogin;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FrmLogin login = new FrmLogin();
            new LoginControlador(login);
            login.setVisible(true);
        });
    }
}
