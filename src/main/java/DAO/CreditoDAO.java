package DAO;

import Modelo.Credito;
import Modelo.Cuota;
import Modelo.OfertaCredito;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

public class CreditoDAO extends BaseDAO {

    private final RowMapper<Credito> mapCredito = rs -> new Credito(
        rs.getInt("id_credito"), rs.getInt("id_usuario"), rs.getDouble("monto_capital"),
        rs.getDouble("tasa_interes"), rs.getInt("total_cuotas"), rs.getDate("fecha_desembolso").toLocalDate(), rs.getString("estado")
    );

    private final RowMapper<Cuota> mapCuota = rs -> {
        Date fPago = rs.getDate("fecha_pago");
        return new Cuota(rs.getInt("id_cuota"), rs.getInt("id_credito"), rs.getInt("numero_cuota"),
                rs.getDouble("monto_capital"), rs.getDouble("monto_interes"), rs.getDouble("monto_multa"),
                rs.getDate("fecha_vencimiento").toLocalDate(), fPago != null ? fPago.toLocalDate() : null,
                rs.getInt("dias_atraso"), rs.getString("estado_cuota"));
    };

    private final RowMapper<OfertaCredito> mapOferta = rs -> new OfertaCredito(
        rs.getInt("id_oferta"), rs.getInt("id_usuario"), rs.getString("titulo_atractivo"),
        rs.getDouble("monto_ofrecido"), rs.getDouble("tasa_interes"), rs.getInt("cuotas_sugeridas"), rs.getString("estado")
    );

    public Credito obtenerCreditoActivo(int idUsuario) {
        return consultarUno("SELECT * FROM creditos WHERE id_usuario = ? AND estado IN ('ACTIVO', 'MORA') ORDER BY id_credito DESC LIMIT 1", mapCredito, idUsuario);
    }

    public List<Cuota> listarCuotasPorCredito(int idCredito) {
        return consultarLista("SELECT * FROM cuotas_credito WHERE id_credito = ? ORDER BY numero_cuota ASC", mapCuota, idCredito);
    }

    public List<Cuota> listarHistorialCuotasUsuario(int idUsuario) {
        String sql = "SELECT cc.* FROM cuotas_credito cc INNER JOIN creditos cr ON cc.id_credito = cr.id_credito WHERE cr.id_usuario = ?";
        return consultarLista(sql, mapCuota, idUsuario);
    }

    public OfertaCredito obtenerOfertaDisponible(int idUsuario) {
        return consultarUno("SELECT * FROM ofertas_credito WHERE id_usuario = ? AND estado = 'PENDIENTE' ORDER BY id_oferta DESC LIMIT 1", mapOferta, idUsuario);
    }

    public boolean enviarOfertaCredito(OfertaCredito o, LocalDate fecha) {
        ejecutarCambio("UPDATE ofertas_credito SET estado = 'REEMPLAZADA' WHERE id_usuario = ? AND estado = 'PENDIENTE'", o.getIdUsuario());
        return ejecutarCambio("INSERT INTO ofertas_credito (id_usuario, titulo_atractivo, monto_ofrecido, tasa_interes, cuotas_sugeridas, estado, fecha_oferta) VALUES (?, ?, ?, ?, ?, 'PENDIENTE', ?)",
                o.getIdUsuario(), o.getTituloAtractivo(), o.getMontoOfertado(), o.getTasaInteres(), o.getCuotasSugeridas(), Date.valueOf(fecha));
    }

    // --- LLAMADAS A PROCEDIMIENTOS ALMACENADOS EN MYSQL ---

    public String desembolsarCreditoSP(int idOferta, int idUsuario, int cuotas, LocalDate fecha) {
        try (CallableStatement cs = getCon().prepareCall("{CALL sp_desembolsar_credito(?, ?, ?, ?, ?)}")) {
            cs.setInt(1, idOferta);
            cs.setInt(2, idUsuario);
            cs.setInt(3, cuotas);
            cs.setDate(4, Date.valueOf(fecha));
            cs.registerOutParameter(5, Types.VARCHAR);
            cs.execute();
            return cs.getString(5);
        } catch (SQLException e) {
            return "Error SQL: " + e.getMessage();
        }
    }

    public String pagarCuotaSP(int idCuota, int idUsuario, LocalDate fecha) {
        try (CallableStatement cs = getCon().prepareCall("{CALL sp_pagar_cuota(?, ?, ?, ?)}")) {
            cs.setInt(1, idCuota);
            cs.setInt(2, idUsuario);
            cs.setDate(3, Date.valueOf(fecha));
            cs.registerOutParameter(4, Types.VARCHAR);
            cs.execute();
            return cs.getString(4);
        } catch (SQLException e) {
            return "Error SQL: " + e.getMessage();
        }
    }
}
