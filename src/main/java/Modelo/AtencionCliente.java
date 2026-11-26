package Modelo;

import java.time.LocalDate;

public abstract class AtencionCliente {
    protected int idRegistro;
    protected int idUsuario;
    protected String cliente;
    protected String mensaje;
    protected String respuestaAdmin;
    protected LocalDate fechaRegistro;

    public AtencionCliente(int idUsuario, String mensaje, LocalDate fechaRegistro) {
        this.idUsuario = idUsuario;
        this.mensaje = mensaje;
        this.fechaRegistro = fechaRegistro;
    }

    protected AtencionCliente() {}

    public abstract String getTipo();
    public abstract String generarMensajeNotificacion();

    public int getIdRegistro() { return idRegistro; }
    public void setIdRegistro(int idRegistro) { this.idRegistro = idRegistro; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getRespuestaAdmin() { return respuestaAdmin; }
    public void setRespuestaAdmin(String respuestaAdmin) { this.respuestaAdmin = respuestaAdmin; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
