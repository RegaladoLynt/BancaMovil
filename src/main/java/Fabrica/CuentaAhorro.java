package Fabrica;

import Modelo.Cuenta;
import java.security.SecureRandom;

public class CuentaAhorro implements ProductoFinanciero {
    private final Cuenta cuenta;
    private static final SecureRandom random = new SecureRandom();

    public CuentaAhorro(int idUsuario) {
        String numeroCuenta = "191" + (10000000000L + (Math.abs(random.nextLong()) % 90000000000L));
        this.cuenta = new Cuenta(0, idUsuario, numeroCuenta, "AHORRO", 0.00, 1500.00, "ACTIVA");
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    @Override
    public String getTipoProducto() {
        return "CUENTA_AHORRO";
    }

    @Override
    public String generarResumen() {
        return "Cuenta Ahorro N° " + cuenta.getNumeroCuenta() + " | Límite diario: S/. " + cuenta.getLimiteDiario();
    }
}
