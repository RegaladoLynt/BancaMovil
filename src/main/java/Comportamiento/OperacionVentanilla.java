package Comportamiento;

public abstract class OperacionVentanilla {
    public abstract String getTipo();
    public abstract String getDetalle();
    public abstract String validar(double saldoActual, double monto);
    public abstract double calcularNuevoSaldo(double saldoActual, double monto);

    public static OperacionVentanilla crear(boolean esDeposito) {
        return esDeposito ? new Deposito() : new Retiro();
    }

    private static class Deposito extends OperacionVentanilla {
        @Override public String getTipo() { return "DEPOSITO"; }
        @Override public String getDetalle() { return "Depósito en ventanilla bancaria"; }
        @Override public String validar(double saldoActual, double monto) { return null; }
        @Override public double calcularNuevoSaldo(double saldoActual, double monto) { return saldoActual + monto; }
    }

    private static class Retiro extends OperacionVentanilla {
        @Override public String getTipo() { return "RETIRO"; }
        @Override public String getDetalle() { return "Retiro en ventanilla bancaria"; }
        @Override public String validar(double saldoActual, double monto) {
            return (saldoActual < monto) ? "Saldo insuficiente para realizar el retiro en ventanilla." : null;
        }
        @Override public double calcularNuevoSaldo(double saldoActual, double monto) { return saldoActual - monto; }
    }
}
