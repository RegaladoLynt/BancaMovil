package Comportamiento;

import Modelo.Cuota;
import java.time.LocalDate;

public class PagoPuntualStrategy implements CalculoCuotaStrategy {
    @Override
    public void calcular(Cuota cuota, LocalDate fechaActual) {
        cuota.setMontoMulta(0.00);
        cuota.setDiasAtraso(0);
        cuota.setEstadoCuota("PAGADA_A_TIEMPO");
        cuota.setFechaPago(fechaActual);
    }
}
