package Controlador;

import Conexion.SesionSistema;
import Servicios.BancaCreditoDAO;
import Servicios.BancaUsuarioDAO;

public abstract class BaseControlador {
    protected final BancaUsuarioDAO usuarioDAO = new BancaUsuarioDAO();
    protected final BancaCreditoDAO creditoDAO = new BancaCreditoDAO();

    protected SesionSistema getSesion() {
        return SesionSistema.getInstancia();
    }

    public abstract String obtenerNombreModulo();
}
