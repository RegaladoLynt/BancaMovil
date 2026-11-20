package Modelo;

import java.time.LocalDate;

public class Credito {
    private int idCredito;
    private int idUsuario;
    private double montoCapital;
    private double tasaInteres;
    private int totalCuotas;
    private LocalDate fechaDesembolso;
    private String estado;

    public Credito() {}

    public Credito(int idCredito, int idUsuario, double montoCapital, double tasaInteres, int totalCuotas, LocalDate fechaDesembolso, String estado) {
        this.idCredito = idCredito;
        this.idUsuario = idUsuario;
        this.montoCapital = montoCapital;
        this.tasaInteres = tasaInteres;
        this.totalCuotas = totalCuotas;
        this.fechaDesembolso = fechaDesembolso;
        this.estado = estado;
    }

    public int getIdCredito() { return idCredito; }
    public void setIdCredito(int idCredito) { this.idCredito = idCredito; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public double getMontoCapital() { return montoCapital; }
    public void setMontoCapital(double montoCapital) { this.montoCapital = montoCapital; }

    public double getTasaInteres() { return tasaInteres; }
    public void setTasaInteres(double tasaInteres) { this.tasaInteres = tasaInteres; }

    public int getTotalCuotas() { return totalCuotas; }
    public void setTotalCuotas(int totalCuotas) { this.totalCuotas = totalCuotas; }

    public LocalDate getFechaDesembolso() { return fechaDesembolso; }
    public void setFechaDesembolso(LocalDate fechaDesembolso) { this.fechaDesembolso = fechaDesembolso; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
