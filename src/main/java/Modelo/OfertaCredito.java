package Modelo;

public class OfertaCredito {
    private int idOferta;
    private int idUsuario;
    private String tituloAtractivo;
    private double montoOfertado;
    private double tasaInteres;
    private int cuotasSugeridas;
    private String estado;

    public OfertaCredito() {}

    public OfertaCredito(int idOferta, int idUsuario, String tituloAtractivo, double montoOfertado, double tasaInteres, int cuotasSugeridas, String estado) {
        this.idOferta = idOferta;
        this.idUsuario = idUsuario;
        this.tituloAtractivo = tituloAtractivo;
        this.montoOfertado = montoOfertado;
        this.tasaInteres = tasaInteres;
        this.cuotasSugeridas = cuotasSugeridas;
        this.estado = estado;
    }

    public int getIdOferta() { return idOferta; }
    public void setIdOferta(int idOferta) { this.idOferta = idOferta; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getTituloAtractivo() { return tituloAtractivo; }
    public void setTituloAtractivo(String tituloAtractivo) { this.tituloAtractivo = tituloAtractivo; }

    public double getMontoOfertado() { return montoOfertado; }
    public void setMontoOfertado(double montoOfertado) { this.montoOfertado = montoOfertado; }

    public double getTasaInteres() { return tasaInteres; }
    public void setTasaInteres(double tasaInteres) { this.tasaInteres = tasaInteres; }

    public int getCuotasSugeridas() { return cuotasSugeridas; }
    public void setCuotasSugeridas(int cuotasSugeridas) { this.cuotasSugeridas = cuotasSugeridas; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
