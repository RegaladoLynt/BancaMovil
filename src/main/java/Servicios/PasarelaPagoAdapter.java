package Servicios;

public class PasarelaPagoAdapter {
    public static String procesarPagoServicioExterno(String empresa, String codigoSuministro, double monto) {
        String codigoOperacion = "EXT-" + (System.currentTimeMillis() % 1000000);
        return "Pago de servicio " + empresa + " (Cod: " + codigoSuministro + ") | Op: " + codigoOperacion;
    }
}
