package Servicios;

import Modelo.Credito;
import Modelo.Cuota;
import Modelo.OfertaCredito;
import java.sql.*;
import java.util.List;

public class BancaCreditoDAO extends BaseDAO {

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
        rs.getDouble("monto_ofertado"), rs.getDouble("tasa_interes"), rs.getInt("cuotas_sugeridas"), rs.getString("estado")
    );

    public Credito obtenerCreditoActivo(int idUsuario) {
        return consultarUno("SELECT * FROM creditos WHERE id_usuario = ? AND estado IN ('ACTIVO', 'MORA') ORDER BY id_credito DESC LIMIT 1", mapCredito, idUsuario);
    }

    public List<Cuota> listarCuotasPorCredito(int idCredito) {
        return consultarLista("SELECT * FROM cuotas_credito WHERE id_credito = ? ORDER BY numero_cuota ASC", mapCuota, idCredito);
    }

    public boolean crearCreditoConCuotas(Credito c, List<Cuota> cuotas) {
        String sqlC = "INSERT INTO creditos (id_usuario, monto_capital, tasa_interes, total_cuotas, fecha_desembolso, estado) VALUES (?, ?, ?, ?, ?, 'ACTIVO')";
        try (PreparedStatement ps = getCon().prepareStatement(sqlC, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getIdUsuario()); ps.setDouble(2, c.getMontoCapital()); ps.setDouble(3, c.getTasaInteres());
            ps.setInt(4, c.getTotalCuotas()); ps.setDate(5, Date.valueOf(c.getFechaDesembolso()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idCred = rs.getInt(1);
                    for (Cuota cu : cuotas) {
                        ejecutarCambio("INSERT INTO cuotas_credito (id_credito, numero_cuota, monto_capital, monto_interes, monto_multa, fecha_vencimiento, estado_cuota) VALUES (?, ?, ?, ?, 0.00, ?, 'PENDIENTE')",
                                idCred, cu.getNumeroCuota(), cu.getMontoCapital(), cu.getMontoInteres(), Date.valueOf(cu.getFechaVencimiento()));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error Credito]: " + e.getMessage());
        }
        return false;
    }

    public boolean actualizarCuota(Cuota c) {
        return ejecutarCambio("UPDATE cuotas_credito SET monto_interes = ?, monto_multa = ?, fecha_vencimiento = ?, fecha_pago = ?, dias_atraso = ?, estado_cuota = ? WHERE id_cuota = ?",
                c.getMontoInteres(), c.getMontoMulta(), Date.valueOf(c.getFechaVencimiento()),
                c.getFechaPago() != null ? Date.valueOf(c.getFechaPago()) : null, c.getDiasAtraso(), c.getEstadoCuota(), c.getIdCuota());
    }

    public void verificarYFinalizarCredito(int idCredito) {
        Integer pendientes = consultarUno("SELECT COUNT(*) AS p FROM cuotas_credito WHERE id_credito = ? AND estado_cuota = 'PENDIENTE'", rs -> rs.getInt("p"), idCredito);
        if (pendientes != null && pendientes == 0) {
            ejecutarCambio("UPDATE creditos SET estado = 'PAGADO' WHERE id_credito = ?", idCredito);
        }
    }

    public OfertaCredito obtenerOfertaDisponible(int idUsuario) {
        return consultarUno("SELECT * FROM ofertas_credito WHERE id_usuario = ? AND estado = 'DISPONIBLE' ORDER BY id_oferta DESC LIMIT 1", mapOferta, idUsuario);
    }

    public boolean asignarOferta(OfertaCredito o) {
        ejecutarCambio("UPDATE ofertas_credito SET estado = 'RECHAZADA' WHERE id_usuario = ? AND estado = 'DISPONIBLE'", o.getIdUsuario());
        return ejecutarCambio("INSERT INTO ofertas_credito (id_usuario, titulo_atractivo, monto_ofertado, tasa_interes, cuotas_sugeridas, estado) VALUES (?, ?, ?, ?, ?, 'DISPONIBLE')",
                o.getIdUsuario(), o.getTituloAtractivo(), o.getMontoOfertado(), o.getTasaInteres(), o.getCuotasSugeridas());
    }

    public boolean marcarOfertaAceptada(int idOferta) {
        return ejecutarCambio("UPDATE ofertas_credito SET estado = 'ACEPTADA' WHERE id_oferta = ?", idOferta);
    }
}
