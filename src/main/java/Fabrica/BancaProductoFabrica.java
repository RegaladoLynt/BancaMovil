package Fabrica;

import java.time.LocalDate;

public class BancaProductoFabrica {

    public static CuentaAhorro crearCuentaAhorro(int idUsuario) {
        return new CuentaAhorro(idUsuario);
    }

    public static CreditoPersonal crearCreditoPersonal(int idUsuario, double montoCapital, double tasaInteres, int totalCuotas, LocalDate fechaDesembolso) {
        return new CreditoPersonal(idUsuario, montoCapital, tasaInteres, totalCuotas, fechaDesembolso);
    }
}
