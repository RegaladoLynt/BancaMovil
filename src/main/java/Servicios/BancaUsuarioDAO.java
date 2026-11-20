package Servicios;

import Modelo.Cuenta;
import Modelo.Movimiento;
import Modelo.Usuario;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class BancaUsuarioDAO extends BaseDAO {

    private final RowMapper<Usuario> mapUsuario = rs -> new Usuario(
        rs.getInt("id_usuario"), rs.getString("dni"), rs.getString("nombres"),
        rs.getString("apellidos"), rs.getString("celular"), rs.getString("username"),
        rs.getString("password"), rs.getString("rol"), rs.getString("estado")
    );

    private final RowMapper<Cuenta> mapCuenta = rs -> new Cuenta(
        rs.getInt("id_cuenta"), rs.getInt("id_usuario"), rs.getString("numero_cuenta"),
        rs.getString("tipo_cuenta"), rs.getDouble("saldo"), rs.getDouble("limite_diario"), rs.getString("estado")
    );

    private final RowMapper<Movimiento> mapMovimiento = rs -> new Movimiento(
        rs.getInt("id_registro"), rs.getInt("id_usuario"), rs.getString("tipo"),
        rs.getString("detalle"), rs.getDouble("monto"), rs.getDate("fecha_operacion").toLocalDate(), rs.getBoolean("visto_admin")
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

    public Usuario buscarUsuarioPorDni(String dni) {
        return consultarUno("SELECT * FROM usuarios WHERE dni = ? AND rol = 'CLIENTE'", mapUsuario, dni);
    }

    public Usuario buscarUsuarioPorId(int idUsuario) {
        return consultarUno("SELECT * FROM usuarios WHERE id_usuario = ?", mapUsuario, idUsuario);
    }

    public int registrarNuevoCliente(Usuario u, Cuenta c) {
        String sqlU = "INSERT INTO usuarios (dni, nombres, apellidos, celular, username, password, rol) VALUES (?, ?, ?, ?, ?, ?, 'CLIENTE')";
        try (PreparedStatement ps = getCon().prepareStatement(sqlU, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getDni()); ps.setString(2, u.getNombres()); ps.setString(3, u.getApellidos());
            ps.setString(4, u.getCelular()); ps.setString(5, u.getUsername()); ps.setString(6, u.getPassword());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGen = rs.getInt(1);
                    ejecutarCambio("INSERT INTO cuentas (id_usuario, numero_cuenta, tipo_cuenta, saldo, limite_diario) VALUES (?, ?, ?, ?, ?)",
                            idGen, c.getNumeroCuenta(), c.getTipoCuenta(), c.getSaldo(), c.getLimiteDiario());
                    return idGen;
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error Registro]: " + e.getMessage());
        }
        return -1;
    }

    public Cuenta obtenerCuentaPorUsuario(int idUsuario) {
        return consultarUno("SELECT * FROM cuentas WHERE id_usuario = ?", mapCuenta, idUsuario);
    }

    public Cuenta obtenerCuentaPorNumero(String numeroCuenta) {
        return consultarUno("SELECT * FROM cuentas WHERE numero_cuenta = ?", mapCuenta, numeroCuenta);
    }

    public boolean actualizarSaldo(int idCuenta, double nuevoSaldo) {
        return ejecutarCambio("UPDATE cuentas SET saldo = ? WHERE id_cuenta = ?", nuevoSaldo, idCuenta);
    }

    public boolean registrarMovimiento(Movimiento m) {
        return ejecutarCambio("INSERT INTO movimientos_notificaciones (id_usuario, tipo, detalle, monto, fecha_operacion, visto_admin) VALUES (?, ?, ?, ?, ?, ?)",
                m.getIdUsuario(), m.getTipo(), m.getDetalle(), m.getMonto(), Date.valueOf(m.getFechaOperacion()), m.isVistoAdmin());
    }

    public List<Movimiento> listarMovimientosUsuario(int idUsuario) {
        return consultarLista("SELECT * FROM movimientos_notificaciones WHERE id_usuario = ? ORDER BY id_registro DESC", mapMovimiento, idUsuario);
    }

    public List<Movimiento> listarNotificacionesAdmin() {
        return consultarLista("SELECT * FROM movimientos_notificaciones WHERE visto_admin = FALSE OR tipo IN ('CREDITO_SOLICITADO', 'ALERTA_SEGURIDAD') ORDER BY id_registro DESC", mapMovimiento);
    }

    public void marcarNotificacionesComoVistas() {
        ejecutarCambio("UPDATE movimientos_notificaciones SET visto_admin = TRUE WHERE visto_admin = FALSE");
    }
}
