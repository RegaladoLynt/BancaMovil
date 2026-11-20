package Fabrica;

import Modelo.Credito;
import Modelo.Cuota;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreditoPersonal implements ProductoFinanciero {
    private final Credito credito;
    private final List<Cuota> cronograma;

    public CreditoPersonal(int idUsuario, double montoCapital, double tasaInteres, int totalCuotas, LocalDate fechaDesembolso) {
        this.credito = new Credito(0, idUsuario, montoCapital, tasaInteres, totalCuotas, fechaDesembolso, "ACTIVO");
        this.cronograma = new ArrayList<>();

        double capitalPorCuota = Math.round((montoCapital / totalCuotas) * 100.0) / 100.0;
        double interesTotal = montoCapital * (tasaInteres / 100.0);
        double interesPorCuota = Math.round((interesTotal / totalCuotas) * 100.0) / 100.0;

        for (int i = 1; i <= totalCuotas; i++) {
            LocalDate fechaVencimiento = fechaDesembolso.plusDays(30L * i);
            cronograma.add(new Cuota(0, 0, i, capitalPorCuota, interesPorCuota, 0.00, fechaVencimiento, null, 0, "PENDIENTE"));
        }
    }

    public Credito getCredito() {
        return credito;
    }

    public List<Cuota> getCronograma() {
        return cronograma;
    }

    @Override
    public String getTipoProducto() {
        return "CREDITO_PERSONAL";
    }

    @Override
    public String generarResumen() {
        return "Crédito Personal S/. " + credito.getMontoCapital() + " a " + credito.getTotalCuotas() + " cuotas (" + credito.getTasaInteres() + "%)";
    }
}
