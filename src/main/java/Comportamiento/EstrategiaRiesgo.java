package Comportamiento;

import Modelo.Cuota;
import java.util.List;

public class EstrategiaRiesgo {

    public static String evaluarNivelRiesgo(List<Cuota> cuotas) {
        if (cuotas == null || cuotas.isEmpty()) {
            return "BAJO";
        }

        int atrasadas = 0;
        int maxDiasAtraso = 0;

        for (Cuota c : cuotas) {
            if ("ATRASADA".equals(c.getEstadoCuota())) {
                atrasadas++;
                if (c.getDiasAtraso() > maxDiasAtraso) {
                    maxDiasAtraso = c.getDiasAtraso();
                }
            }
        }

        if (atrasadas == 0) {
            return "BAJO";
        } else if (atrasadas <= 2 && maxDiasAtraso < 7) {
            return "MEDIO";
        } else {
            return "ALTO";
        }
    }

    public static double calcularMontoSugerido(String nivelRiesgo) {
        switch (nivelRiesgo) {
            case "BAJO":
                return 3000.00;
            case "MEDIO":
                return 1200.00;
            default:
                return 500.00;
        }
    }
}
