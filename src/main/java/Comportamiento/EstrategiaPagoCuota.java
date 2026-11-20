package Comportamiento;

import Modelo.Cuota;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class EstrategiaPagoCuota {

    public static Cuota procesarCalculoPago(Cuota cuota, LocalDate fechaActual) {
        CalculoCuotaStrategy estrategia;
        if (fechaActual.isBefore(cuota.getFechaVencimiento())) {
            estrategia = new PagoAdelantadoStrategy();
        } else if (fechaActual.isEqual(cuota.getFechaVencimiento())) {
            estrategia = new PagoPuntualStrategy();
        } else {
            estrategia = new PagoAtrasadoStrategy();
        }
        estrategia.calcular(cuota, fechaActual);
        return cuota;
    }

    public static void aplicarProrrogaPorMora(Cuota cuota, LocalDate fechaActual) {
        int diasDemora = (int) ChronoUnit.DAYS.between(cuota.getFechaVencimiento(), fechaActual);
        if (diasDemora > 0) {
            double multa = (diasDemora >= 7) ? 35.00 : (diasDemora >= 3 ? 20.00 : 15.00);
            cuota.setDiasAtraso(diasDemora);
            cuota.setMontoMulta(multa);
            cuota.setFechaVencimiento(cuota.getFechaVencimiento().plusDays(7));
        }
    }
}
