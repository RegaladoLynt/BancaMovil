package Modelo;

import java.time.LocalDate;

public class Queja extends AtencionCliente {

    public Queja(int idUsuario, String mensaje, LocalDate fechaRegistro) {
        super(idUsuario, mensaje, fechaRegistro);
    }

    public Queja() { super(); }

    @Override
    public String getTipo() {
        return "QUEJA";
    }

    @Override
    public String generarMensajeNotificacion() {
        return "[ALERTA QUEJA] Cliente ID " + idUsuario + " reportó un incidente: " + mensaje;
    }
}
