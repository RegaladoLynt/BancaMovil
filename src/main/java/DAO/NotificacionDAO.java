package DAO;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class NotificacionDAO extends BaseDAO {

    public static class NotificacionItem {
        public final int id;
        public final Date fecha;
        public final String mensaje;
        public final boolean leida;

        public NotificacionItem(int id, Date fecha, String mensaje, boolean leida) {
            this.id = id;
            this.fecha = fecha;
            this.mensaje = mensaje;
            this.leida = leida;
        }
    }

    public void sincronizarAlertasRelojSP(LocalDate fecha) {
        try (CallableStatement cs = getCon().prepareCall("{CALL sp_sincronizar_alertas_reloj(?)}")) {
            cs.setDate(1, Date.valueOf(fecha));
            cs.execute();
        } catch (SQLException e) {
            System.err.println("[Error SP Alertas]: " + e.getMessage());
        }
    }

    public List<NotificacionItem> listarNotificacionesAdmin() {
        return consultarLista("SELECT * FROM notificaciones ORDER BY id_notificacion DESC",
                rs -> new NotificacionItem(rs.getInt("id_notificacion"), rs.getDate("fecha_notificacion"), rs.getString("mensaje"), rs.getBoolean("leida")));
    }

    public boolean marcarTodasLeidas() {
        return ejecutarCambio("UPDATE notificaciones SET leida = TRUE WHERE leida = FALSE");
    }

    public boolean reenviarNotificacion(int idNotificacion, LocalDate nuevaFecha) {
        return ejecutarCambio("UPDATE notificaciones SET leida = FALSE, fecha_notificacion = ? WHERE id_notificacion = ?", Date.valueOf(nuevaFecha), idNotificacion);
    }
}
