package Conexion;

import java.time.LocalDate;

public class SesionSistema {
    private static SesionSistema instancia;
    private int idUsuario;
    private String dni;
    private String nombreCompleto;
    private String celular;
    private String rol;
    private LocalDate fechaSimulada;

    private SesionSistema() {}

    public static synchronized SesionSistema getInstancia() {
        if (instancia == null) {
            instancia = new SesionSistema();
        }
        return instancia;
    }

    public void iniciarSesion(int idUsuario, String dni, String nombreCompleto, String celular, String rol, LocalDate fechaSimulada) {
        this.idUsuario = idUsuario;
        this.dni = dni;
        this.nombreCompleto = nombreCompleto;
        this.celular = celular;
        this.rol = rol;
        this.fechaSimulada = fechaSimulada;
    }

    public void cerrarSesion() {
        this.idUsuario = 0;
        this.dni = null;
        this.nombreCompleto = null;
        this.celular = null;
        this.rol = null;
        this.fechaSimulada = null;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getDni() { return dni; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getCelular() { return celular; }
    public String getRol() { return rol; }
    public LocalDate getFechaSimulada() { return fechaSimulada; }
}
