package Comportamiento;

import Modelo.Cuota;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PagoAtrasadoStrategy implements CalculoCuotaStrategy {
    @Override
    public void calcular(Cuota cuota, LocalDate fechaActual) {
        int diasDemora = (int) ChronoUnit.DAYS.between(cuota.getFechaVencimiento(), fechaActual);
        double multa = (diasDemora >= 7) ? 35.00 : (diasDemora >= 3 ? 20.00 : 15.00);
        cuota.setDiasAtraso(diasDemora);
        cuota.setMontoMulta(multa);
        cuota.setEstadoCuota("ATRASADA");
        cuota.setFechaPago(fechaActual);
    }
}
