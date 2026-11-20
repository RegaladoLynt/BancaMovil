package Modelo;

public class Cuenta {
    private int idCuenta;
    private int idUsuario;
    private String numeroCuenta;
    private String tipoCuenta;
    private double saldo;
    private double limiteDiario;
    private String estado;

    public Cuenta() {}

    public Cuenta(int idCuenta, int idUsuario, String numeroCuenta, String tipoCuenta, double saldo, double limiteDiario, String estado) {
        this.idCuenta = idCuenta;
        this.idUsuario = idUsuario;
        this.numeroCuenta = numeroCuenta;
        this.tipoCuenta = tipoCuenta;
        this.saldo = saldo;
        this.limiteDiario = limiteDiario;
        this.estado = estado;
    }

    public int getIdCuenta() { return idCuenta; }
    public void setIdCuenta(int idCuenta) { this.idCuenta = idCuenta; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }

    public String getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(String tipoCuenta) { this.tipoCuenta = tipoCuenta; }

    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }

    public double getLimiteDiario() { return limiteDiario; }
    public void setLimiteDiario(double limiteDiario) { this.limiteDiario = limiteDiario; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
