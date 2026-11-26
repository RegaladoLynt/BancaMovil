package Comportamiento;

import Modelo.Cuota;
import java.time.LocalDate;

public interface EstadoCuota {
    void calcular(Cuota cuota, LocalDate fechaActual);
}
