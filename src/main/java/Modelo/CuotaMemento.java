package Modelo;

import java.time.LocalDate;

public class CuotaMemento {
    private final double montoInteres;
    private final double montoMulta;
    private final LocalDate fechaVencimiento;
    private final LocalDate fechaPago;
    private final int diasAtraso;
    private final String estadoCuota;

    public CuotaMemento(double montoInteres, double montoMulta, LocalDate fechaVencimiento, LocalDate fechaPago, int diasAtraso, String estadoCuota) {
        this.montoInteres = montoInteres;
        this.montoMulta = montoMulta;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaPago = fechaPago;
        this.diasAtraso = diasAtraso;
        this.estadoCuota = estadoCuota;
    }

    public double getMontoInteres() { return montoInteres; }
    public double getMontoMulta() { return montoMulta; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public LocalDate getFechaPago() { return fechaPago; }
    public int getDiasAtraso() { return diasAtraso; }
    public String getEstadoCuota() { return estadoCuota; }
}
