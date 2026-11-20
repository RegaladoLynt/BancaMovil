package Comportamiento;

import java.time.LocalDate;

public abstract class AlertaVencimiento {
    protected String cliente;
    protected int numeroCuota;
    protected double monto;
    protected LocalDate fechaVencimiento;

    public AlertaVencimiento(String cliente, int numeroCuota, double monto, LocalDate fechaVencimiento) {
        this.cliente = cliente;
        this.numeroCuota = numeroCuota;
        this.monto = monto;
        this.fechaVencimiento = fechaVencimiento;
    }

    public abstract String generarMensaje();

    // Método fábrica polimórfico según los días restantes
    public static AlertaVencimiento crearSegunDias(long dias, String cliente, int numCuota, double monto, LocalDate venc) {
        if (dias == 1) return new AlertaManana(cliente, numCuota, monto, venc);
        if (dias == 0) return new AlertaHoy(cliente, numCuota, monto, venc);
        if (dias < 0)  return new AlertaVencida(cliente, numCuota, monto, venc, Math.abs(dias));
        return null;
    }

    // --- Subclases polimórficas internas (evita crear 4 archivos extra) ---
    private static class AlertaManana extends AlertaVencimiento {
        public AlertaManana(String c, int n, double m, LocalDate v) { super(c, n, m, v); }
        @Override
        public String generarMensaje() {
            return "[VENCE MAÑANA] " + cliente + " - Cuota #" + numeroCuota + " (S/. " + monto + ") vence mañana (" + fechaVencimiento + ").";
        }
    }

    private static class AlertaHoy extends AlertaVencimiento {
        public AlertaHoy(String c, int n, double m, LocalDate v) { super(c, n, m, v); }
        @Override
        public String generarMensaje() {
            return "[VENCE HOY] " + cliente + " - Cuota #" + numeroCuota + " (S/. " + monto + ") vence HOY (" + fechaVencimiento + ").";
        }
    }

    private static class AlertaVencida extends AlertaVencimiento {
        private final long diasAtraso;
        public AlertaVencida(String c, int n, double m, LocalDate v, long diasAtraso) {
            super(c, n, m, v);
            this.diasAtraso = diasAtraso;
        }
        @Override
        public String generarMensaje() {
            return "[VENCIDA] " + cliente + " - Cuota #" + numeroCuota + " (S/. " + monto + ") tiene " + diasAtraso + " día(s) de atraso.";
        }
    }
}
