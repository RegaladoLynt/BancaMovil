package Comportamiento;

import Modelo.Cuota;
import java.time.LocalDate;

public class EstrategiaPagoCuota {

    public static Cuota procesarCalculoPago(Cuota cuota, LocalDate fechaActual) {
        EstadoCuota estado;
        if (fechaActual.isBefore(cuota.getFechaVencimiento())) {
            estado = new CuotaAdelantadaState();
        } else if (fechaActual.isEqual(cuota.getFechaVencimiento())) {
            estado = new CuotaPuntualState();
        } else {
            estado = new CuotaAtrasadaState();
        }
        estado.calcular(cuota, fechaActual);
        return cuota;
    }
}
