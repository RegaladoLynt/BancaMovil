package Procesos;

import Modelo.Cuota;
import java.util.List;

public class ProcesoGrafico {

    public static int contarATiempo(List<Cuota> cuotas) {
        if (cuotas == null) return 0;
        return (int) cuotas.stream().filter(c -> "PAGADA_A_TIEMPO".equals(c.getEstadoCuota())).count();
    }

    public static int contarAtrasadas(List<Cuota> cuotas) {
        if (cuotas == null) return 0;
        return (int) cuotas.stream().filter(c -> "ATRASADA".equals(c.getEstadoCuota())).count();
    }

    public static int contarPendientes(List<Cuota> cuotas) {
        if (cuotas == null) return 0;
        return (int) cuotas.stream().filter(c -> "PENDIENTE".equals(c.getEstadoCuota())).count();
    }

    public static int calcularGrados(int cantidad, int total) {
        if (total == 0) return 0;
        return (int) Math.round((cantidad * 360.0) / total);
    }

    public static double calcularPorcentaje(int cantidad, int total) {
        if (total == 0) return 0.0;
        return (cantidad * 100.0) / total;
    }

    public static int contarAtraso1Dia(List<Cuota> cuotas) {
        if (cuotas == null) return 0;
        return (int) cuotas.stream().filter(c -> "ATRASADA".equals(c.getEstadoCuota()) && c.getDiasAtraso() >= 1 && c.getDiasAtraso() < 3).count();
    }

    public static int contarAtraso3Dias(List<Cuota> cuotas) {
        if (cuotas == null) return 0;
        return (int) cuotas.stream().filter(c -> "ATRASADA".equals(c.getEstadoCuota()) && c.getDiasAtraso() >= 3 && c.getDiasAtraso() < 7).count();
    }

    public static int contarAtraso7Dias(List<Cuota> cuotas) {
        if (cuotas == null) return 0;
        return (int) cuotas.stream().filter(c -> "ATRASADA".equals(c.getEstadoCuota()) && c.getDiasAtraso() >= 7).count();
    }
}
