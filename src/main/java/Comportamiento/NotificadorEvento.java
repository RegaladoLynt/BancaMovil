package Comportamiento;

import Modelo.Movimiento;
import java.time.LocalDate;

public class NotificadorEvento {

    public static Movimiento crearNotificacionAdmin(int idUsuario, String tipo, String detalle, double monto, LocalDate fecha) {
        return new Movimiento(0, idUsuario, tipo, detalle, monto, fecha, false);
    }

    public static Movimiento crearRegistroMovimiento(int idUsuario, String tipo, String detalle, double monto, LocalDate fecha) {
        return new Movimiento(0, idUsuario, tipo, detalle, monto, fecha, true);
    }
}
