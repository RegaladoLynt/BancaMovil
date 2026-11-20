package Modelo;

import java.time.LocalDate;

public class Movimiento {
    private int idRegistro;
    private int idUsuario;
    private String tipo;
    private String detalle;
    private double monto;
    private LocalDate fechaOperacion;
    private boolean vistoAdmin;

    public Movimiento() {}

    public Movimiento(int idRegistro, int idUsuario, String tipo, String detalle, double monto, LocalDate fechaOperacion, boolean vistoAdmin) {
        this.idRegistro = idRegistro;
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.detalle = detalle;
        this.monto = monto;
        this.fechaOperacion = fechaOperacion;
        this.vistoAdmin = vistoAdmin;
    }

    public int getIdRegistro() { return idRegistro; }
    public void setIdRegistro(int idRegistro) { this.idRegistro = idRegistro; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public LocalDate getFechaOperacion() { return fechaOperacion; }
    public void setFechaOperacion(LocalDate fechaOperacion) { this.fechaOperacion = fechaOperacion; }

    public boolean isVistoAdmin() { return vistoAdmin; }
    public void setVistoAdmin(boolean vistoAdmin) { this.vistoAdmin = vistoAdmin; }
}
