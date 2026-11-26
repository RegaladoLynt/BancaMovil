package DAO;

import Modelo.Cuenta;
import Modelo.Movimiento;
import Modelo.Usuario;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

public class UsuarioDAO extends BaseDAO {

    private final RowMapper<Usuario> mapUsuario = rs -> {
        Usuario u = new Usuario(
            rs.getInt("id_usuario"), rs.getString("dni"), rs.getString("nombres"),
            rs.getString("apellidos"), rs.getString("celular"), rs.getString("username"),
            rs.getString("password"), rs.getString("rol"), rs.getString("estado")
        );
        try {
            u.setNumeroCuenta(rs.getString("numero_cuenta"));
            u.setSaldo(rs.getDouble("saldo"));
        } catch (SQLException ignored) { }
        return u;
    };

    private final RowMapper<Cuenta> mapCuenta = rs -> new Cuenta(
        rs.getInt("id_cuenta"), rs.getInt("id_usuario"), rs.getString("numero_cuenta"),
        rs.getString("tipo_cuenta"), rs.getDouble("saldo"), rs.getDouble("limite_diario"), rs.getString("estado")
    );

    private final RowMapper<Movimiento> mapMovimiento = rs -> new Movimiento(
        rs.getInt("id_transaccion"), rs.getInt("id_usuario"), rs.getString("tipo"),
        rs.getString("detalle"), rs.getDouble("monto"), rs.getDate("fecha_transaccion").toLocalDate(), rs.getBoolean("visto_admin")
    );

    public LocalDate obtenerFechaMinima() {
        LocalDate fecha = consultarUno("SELECT fecha_minima FROM configuracion_sistema WHERE id_config = 1", rs -> rs.getDate("fecha_minima").toLocalDate());
        return fecha != null ? fecha : LocalDate.of(2012, 12, 12);
    }

    public boolean actualizarFechaMinima(LocalDate nuevaFecha) {
        return ejecutarCambio("UPDATE configuracion_sistema SET fecha_minima = ? WHERE id_config = 1", Date.valueOf(nuevaFecha));
    }

    public Usuario validarLogin(String username, String password) {
        return consultarUno("SELECT * FROM usuarios WHERE username = ? AND password = ? AND estado = 'ACTIVO'", mapUsuario, username, password);
    }

    public Usuario buscarClientePorDni(String dni) {
        String sql = "SELECT u.*, c.numero_cuenta, c.saldo FROM usuarios u LEFT JOIN cuentas c ON u.id_usuario = c.id_usuario WHERE u.dni = ? AND u.rol = 'CLIENTE'";
        return consultarUno(sql, mapUsuario, dni);
    }

    public List<Usuario> listarTodosLosClientes() {
        String sql = "SELECT u.*, c.numero_cuenta, c.saldo FROM usuarios u LEFT JOIN cuentas c ON u.id_usuario = c.id_usuario WHERE u.rol = 'CLIENTE' ORDER BY u.id_usuario ASC";
        return consultarLista(sql, mapUsuario);
    }

    public Cuenta obtenerCuentaPorUsuario(int idUsuario) {
        return consultarUno("SELECT * FROM cuentas WHERE id_usuario = ?", mapCuenta, idUsuario);
    }

    public List<Movimiento> listarMovimientosUsuario(int idUsuario) {
        return consultarLista("SELECT * FROM transacciones WHERE id_usuario = ? ORDER BY id_transaccion DESC", mapMovimiento, idUsuario);
    }

    public Movimiento buscarTransaccionPorId(int idTransaccion, int idUsuario) {
        return consultarUno("SELECT * FROM transacciones WHERE id_transaccion = ? AND id_usuario = ?", mapMovimiento, idTransaccion, idUsuario);
    }

    // --- LLAMADAS A PROCEDIMIENTOS ALMACENADOS EN MYSQL ---

    public String registrarClienteSP(Usuario u, String numeroCuenta, double saldoInicial, LocalDate fecha) {
        try (CallableStatement cs = getCon().prepareCall("{CALL sp_registrar_cliente(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {
            cs.setString(1, u.getDni());
            cs.setString(2, u.getNombres());
            cs.setString(3, u.getApellidos());
            cs.setString(4, u.getCelular());
            cs.setString(5, u.getUsername());
            cs.setString(6, u.getPassword());
            cs.setString(7, numeroCuenta);
            cs.setDouble(8, saldoInicial);
            cs.setDate(9, Date.valueOf(fecha));
            cs.registerOutParameter(10, Types.VARCHAR);
            cs.execute();
            return cs.getString(10);
        } catch (SQLException e) {
            return "Error SQL: " + e.getMessage();
        }
    }

    public String operarVentanillaSP(Usuario cliente, boolean esDeposito, double monto, LocalDate fecha) {
        try (CallableStatement cs = getCon().prepareCall("{CALL sp_operar_ventanilla(?, ?, ?, ?, ?, ?)}")) {
            cs.setInt(1, cliente.getIdUsuario());
            cs.setBoolean(2, esDeposito);
            cs.setDouble(3, monto);
            cs.setDate(4, Date.valueOf(fecha));
            cs.registerOutParameter(5, Types.VARCHAR);
            cs.registerOutParameter(6, Types.DECIMAL);
            cs.execute();
            String res = cs.getString(5);
            if ("OK".equals(res)) {
                cliente.setSaldo(cs.getDouble(6));
            }
            return res;
        } catch (SQLException e) {
            return "Error SQL: " + e.getMessage();
        }
    }

    public String realizarTransferenciaSP(int idEmisor, String cuentaDestino, double monto, LocalDate fecha) {
        try (CallableStatement cs = getCon().prepareCall("{CALL sp_realizar_transferencia(?, ?, ?, ?, ?)}")) {
            cs.setInt(1, idEmisor);
            cs.setString(2, cuentaDestino);
            cs.setDouble(3, monto);
            cs.setDate(4, Date.valueOf(fecha));
            cs.registerOutParameter(5, Types.VARCHAR);
            cs.execute();
            return cs.getString(5);
        } catch (SQLException e) {
            return "Error SQL: " + e.getMessage();
        }
    }

    public String pagarServicioSP(int idUsuario, String detalle, double monto, LocalDate fecha) {
        try (CallableStatement cs = getCon().prepareCall("{CALL sp_pagar_servicio(?, ?, ?, ?, ?)}")) {
            cs.setInt(1, idUsuario);
            cs.setString(2, detalle);
            cs.setDouble(3, monto);
            cs.setDate(4, Date.valueOf(fecha));
            cs.registerOutParameter(5, Types.VARCHAR);
            cs.execute();
            return cs.getString(5);
        } catch (SQLException e) {
            return "Error SQL: " + e.getMessage();
        }
    }
}
