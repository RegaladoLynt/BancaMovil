package Controlador;

import Comportamiento.EstrategiaRiesgo;
import Fabrica.BancaProductoFabrica;
import Fabrica.CuentaAhorro;
import Modelo.Credito;
import Modelo.Cuota;
import Modelo.Movimiento;
import Modelo.OfertaCredito;
import Modelo.Usuario;
import Personalizacion.DecoradorOferta;
import java.util.Collections;
import java.util.List;

public class AdminControlador extends BaseControlador {

    @Override
    public String obtenerNombreModulo() {
        return "Panel Administrativo - Gestión de Riesgos y Clientes";
    }

    public Usuario buscarClientePorDni(String dni) {
        return usuarioDAO.buscarUsuarioPorDni(dni.trim());
    }

    public List<Cuota> obtenerCuotasCliente(int idUsuario) {
        Credito cred = creditoDAO.obtenerCreditoActivo(idUsuario);
        if (cred == null) return Collections.emptyList();
        return creditoDAO.listarCuotasPorCredito(cred.getIdCredito());
    }

    public String calcularRiesgoCliente(List<Cuota> cuotas) {
        return EstrategiaRiesgo.evaluarNivelRiesgo(cuotas);
    }

    public double obtenerMontoSugeridoPorRiesgo(String riesgo) {
        return EstrategiaRiesgo.calcularMontoSugerido(riesgo);
    }

    public boolean asignarOfertaPersonalizada(int idUsuario, String riesgo, double montoAsignado) {
        OfertaCredito ofertaDecorada = DecoradorOferta.decorarOfertaPorRiesgo(idUsuario, riesgo, montoAsignado);
        return creditoDAO.asignarOferta(ofertaDecorada);
    }

    public boolean registrarNuevoCliente(String dni, String nombres, String apellidos, String celular, String username, String password, double saldoInicial) {
        Usuario nuevo = new Usuario(0, dni, nombres, apellidos, celular, username, password, "CLIENTE", "ACTIVO");
        CuentaAhorro productoCuenta = BancaProductoFabrica.crearCuentaAhorro(0);
        productoCuenta.getCuenta().setSaldo(saldoInicial);
        return usuarioDAO.registrarNuevoCliente(nuevo, productoCuenta.getCuenta()) > 0;
    }

    public List<Movimiento> obtenerNotificacionesAdmin() {
        return usuarioDAO.listarNotificacionesAdmin();
    }

    public void marcarNotificacionesLeidas() {
        usuarioDAO.marcarNotificacionesComoVistas();
    }
}
