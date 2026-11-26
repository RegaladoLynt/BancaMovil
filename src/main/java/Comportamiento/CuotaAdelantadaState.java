package Comportamiento;

import Modelo.Cuota;
import java.time.LocalDate;

public class CuotaAdelantadaState implements EstadoCuota {
    @Override
    public void calcular(Cuota cuota, LocalDate fechaActual) {
        double nuevoInteres = Math.round((cuota.getMontoInteres() * 0.50) * 100.0) / 100.0;
        cuota.setMontoInteres(nuevoInteres);
        cuota.setMontoMulta(0.00);
        cuota.setDiasAtraso(0);
        cuota.setEstadoCuota("PAGADA_A_TIEMPO");
        cuota.setFechaPago(fechaActual);
    }
}
