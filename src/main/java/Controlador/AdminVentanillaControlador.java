package Controlador;

import Comportamiento.OperacionVentanilla;
import Fabrica.BancaProductoFabrica;
import Fabrica.CuentaAhorro;
import Modelo.Usuario;
import java.time.LocalDate;

public class AdminVentanillaControlador extends BaseControlador {

    @Override
    public String obtenerNombreModulo() {
        return "Gestión de Clientes y Ventanilla";
    }

    public Usuario buscarClientePorDni(String dni) {
        if (dni == null || !dni.trim().matches("\\d{8}")) return null;
        return usuarioDAO.buscarClientePorDni(dni.trim());
    }

    public String registrarNuevoCliente(String dni, String nombres, String apellidos, String celular, String user, String pass, String saldoStr) {
        if (dni.isEmpty() || nombres.isEmpty() || apellidos.isEmpty() || user.isEmpty() || pass.isEmpty() || saldoStr.isEmpty())
            return "Complete todos los campos obligatorios.";
        if (!dni.matches("\\d{8}")) return "El DNI debe tener exactamente 8 dígitos.";

        double saldoInicial;
        try {
            saldoInicial = Double.parseDouble(saldoStr.trim().replace(",", "."));
            if (saldoInicial < 0) return "El saldo inicial no puede ser negativo.";
        } catch (NumberFormatException e) {
            return "Ingrese un monto numérico válido para el saldo inicial.";
        }

        Usuario nuevo = new Usuario(0, dni.trim(), nombres.trim(), apellidos.trim(), celular.trim(), user.trim(), pass.trim(), "CLIENTE", "ACTIVO");
        CuentaAhorro cuentaFabrica = BancaProductoFabrica.crearCuentaAhorro(0);
        LocalDate fecha = (getSesion().getFechaSimulada() != null) ? getSesion().getFechaSimulada() : LocalDate.now();

        return usuarioDAO.registrarClienteSP(nuevo, cuentaFabrica.getCuenta().getNumeroCuenta(), saldoInicial, fecha);
    }

    public String operarVentanilla(Usuario cliente, String montoStr, boolean esDeposito) {
        if (cliente == null) return "Seleccione primero un cliente válido.";
        double monto;
        try {
            monto = Double.parseDouble(montoStr.trim().replace(",", "."));
            if (monto <= 0) return "El monto debe ser mayor a S/. 0.00.";
        } catch (NumberFormatException e) {
            return "Ingrese un monto numérico válido.";
        }

        OperacionVentanilla op = OperacionVentanilla.crear(esDeposito);
        String error = op.validar(cliente.getSaldo(), monto);
        if (error != null) return error;

        LocalDate fecha = (getSesion().getFechaSimulada() != null) ? getSesion().getFechaSimulada() : LocalDate.now();
        return usuarioDAO.operarVentanillaSP(cliente, esDeposito, monto, fecha);
    }
}
