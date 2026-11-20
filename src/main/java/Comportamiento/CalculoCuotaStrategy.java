package Comportamiento;

import Modelo.Cuota;
import java.time.LocalDate;

public interface CalculoCuotaStrategy {
    void calcular(Cuota cuota, LocalDate fechaActual);
}
