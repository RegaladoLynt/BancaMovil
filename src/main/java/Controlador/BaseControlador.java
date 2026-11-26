package Controlador;

import Conexion.SesionSistema;
import DAO.AtencionDAO;
import DAO.CreditoDAO;
import DAO.NotificacionDAO;
import DAO.UsuarioDAO;

public abstract class BaseControlador {
    protected final UsuarioDAO usuarioDAO = new UsuarioDAO();
    protected final CreditoDAO creditoDAO = new CreditoDAO();
    protected final AtencionDAO atencionDAO = new AtencionDAO();
    protected final NotificacionDAO notificacionDAO = new NotificacionDAO();

    protected SesionSistema getSesion() {
        return SesionSistema.getInstancia();
    }

    public abstract String obtenerNombreModulo();
}
