package Personalizacion;

import Modelo.OfertaCredito;

public class DecoradorOferta {

    public static OfertaCredito decorarOfertaPorRiesgo(int idUsuario, String nivelRiesgo, double montoPersonalizado) {
        String titulo;
        double tasa;
        int cuotas;

        switch (nivelRiesgo) {
            case "BAJO":
                titulo = "¡CLIENTE VIP! Crédito Preferencial Preaprobado con Tasa Mínima";
                tasa = 7.50;
                cuotas = 12;
                break;
            case "MEDIO":
                titulo = "¡Oportunidad Única! Crédito Impulso Preaprobado al Instante";
                tasa = 10.50;
                cuotas = 6;
                break;
            default:
                titulo = "Crédito de Reactivación Financiada - Mejora tu Historial";
                tasa = 14.50;
                cuotas = 6;
                break;
        }

        return new OfertaCredito(0, idUsuario, titulo, montoPersonalizado, tasa, cuotas, "DISPONIBLE");
    }
}
