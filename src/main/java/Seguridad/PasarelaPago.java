package Seguridad;

public interface PasarelaPago {
    String procesarPago(String empresa, String codigoSuministro, double monto);
}
