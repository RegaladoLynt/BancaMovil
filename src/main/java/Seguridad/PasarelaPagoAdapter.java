package Seguridad;

public class PasarelaPagoAdapter implements PasarelaPago {
    @Override
    public String procesarPago(String empresa, String codigoSuministro, double monto) {
        String codigoOperacion = "EXT-" + (System.currentTimeMillis() % 1000000);
        return "Pago de servicio " + empresa + " (Cod: " + codigoSuministro + ") | Op: " + codigoOperacion;
    }
}
