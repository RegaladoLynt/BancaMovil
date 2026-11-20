package Modelo;

import java.time.LocalDate;

public class Sugerencia extends AtencionCliente {

    public Sugerencia(int idUsuario, String mensaje, LocalDate fechaRegistro) {
        super(idUsuario, mensaje, fechaRegistro);
    }

    public Sugerencia() {
        super();
    }

    @Override
    public String getTipo() {
        return "SUGERENCIA";
    }

    @Override
    public String generarMensajeNotificacion() {
        return null;
    }
}
