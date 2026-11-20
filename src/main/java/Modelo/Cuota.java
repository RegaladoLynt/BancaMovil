package Modelo;

import java.time.LocalDate;

public class Cuota {
    private int idCuota;
    private int idCredito;
    private int numeroCuota;
    private double montoCapital;
    private double montoInteres;
    private double montoMulta;
    private LocalDate fechaVencimiento;
    private LocalDate fechaPago;
    private int diasAtraso;
    private String estadoCuota;

    public Cuota() {}

    public Cuota(int idCuota, int idCredito, int numeroCuota, double montoCapital, double montoInteres, double montoMulta, LocalDate fechaVencimiento, LocalDate fechaPago, int diasAtraso, String estadoCuota) {
        this.idCuota = idCuota;
        this.idCredito = idCredito;
        this.numeroCuota = numeroCuota;
        this.montoCapital = montoCapital;
        this.montoInteres = montoInteres;
        this.montoMulta = montoMulta;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaPago = fechaPago;
        this.diasAtraso = diasAtraso;
        this.estadoCuota = estadoCuota;
    }

    public int getIdCuota() { return idCuota; }
    public void setIdCuota(int idCuota) { this.idCuota = idCuota; }

    public int getIdCredito() { return idCredito; }
    public void setIdCredito(int idCredito) { this.idCredito = idCredito; }

    public int getNumeroCuota() { return numeroCuota; }
    public void setNumeroCuota(int numeroCuota) { this.numeroCuota = numeroCuota; }

    public double getMontoCapital() { return montoCapital; }
    public void setMontoCapital(double montoCapital) { this.montoCapital = montoCapital; }

    public double getMontoInteres() { return montoInteres; }
    public void setMontoInteres(double montoInteres) { this.montoInteres = montoInteres; }

    public double getMontoMulta() { return montoMulta; }
    public void setMontoMulta(double montoMulta) { this.montoMulta = montoMulta; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    public int getDiasAtraso() { return diasAtraso; }
    public void setDiasAtraso(int diasAtraso) { this.diasAtraso = diasAtraso; }

    public String getEstadoCuota() { return estadoCuota; }
    public void setEstadoCuota(String estadoCuota) { this.estadoCuota = estadoCuota; }

    public double getMontoTotal() {
        return montoCapital + montoInteres + montoMulta;
    }
}
