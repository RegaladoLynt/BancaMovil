package Controlador;

import Comportamiento.OperacionVentanilla;
import Conexion.BaseDatosConexion;
import Modelo.Usuario;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class AdminVentanillaControlador extends BaseControlador {

    @Override
    public String obtenerNombreModulo() {
        return "Gestión de Clientes y Ventanilla";
    }

    public Usuario buscarClientePorDni(String dni) {
        if (dni == null || !dni.trim().matches("\\d{8}")) return null;
        String sql = "SELECT u.*, c.numero_cuenta, c.saldo FROM usuarios u " +
                     "LEFT JOIN cuentas c ON u.id_usuario = c.id_usuario WHERE u.dni = ? AND u.rol = 'CLIENTE'";
        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dni.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setDni(rs.getString("dni"));
                    u.setNombres(rs.getString("nombres"));
                    u.setApellidos(rs.getString("apellidos"));
                    u.setCelular(rs.getString("celular"));
                    u.setEstado(rs.getString("estado"));
                    u.setNumeroCuenta(rs.getString("numero_cuenta"));
                    u.setSaldo(rs.getDouble("saldo"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error buscando cliente: " + e.getMessage());
        }
        return null;
    }

    public String registrarNuevoCliente(String dni, String nombres, String apellidos, String celular, String user, String pass, String saldoStr) {
        if (dni.isEmpty() || nombres.isEmpty() || apellidos.isEmpty() || user.isEmpty() || pass.isEmpty() || saldoStr.isEmpty())
            return "Complete todos los campos obligatorios.";
        if (!dni.matches("\\d{8}")) return "El DNI debe tener exactamente 8 dígitos.";

        double saldoInicial;
        try {
            saldoInicial = Double.parseDouble(saldoStr.trim());
            if (saldoInicial < 0) return "El saldo inicial no puede ser negativo.";
        } catch (NumberFormatException e) {
            return "Ingrese un monto numérico válido para el saldo inicial.";
        }

        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try {
            int idGenerado = 0;
            String sqlUser = "INSERT INTO usuarios (dni, nombres, apellidos, celular, username, password, rol, estado) VALUES (?, ?, ?, ?, ?, ?, 'CLIENTE', 'ACTIVO')";
            try (PreparedStatement ps = cn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, dni.trim()); ps.setString(2, nombres.trim());
                ps.setString(3, apellidos.trim()); ps.setString(4, celular.trim());
                ps.setString(5, user.trim()); ps.setString(6, pass.trim());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) idGenerado = rs.getInt(1); }
            }

            try (PreparedStatement psC = cn.prepareStatement("INSERT INTO cuentas (id_usuario, numero_cuenta, saldo) VALUES (?, ?, ?)")) {
                psC.setInt(1, idGenerado);
                psC.setString(2, "100" + (1000000 + idGenerado));
                psC.setDouble(3, saldoInicial);
                psC.executeUpdate();
            }

            if (saldoInicial > 0) registrarMovimiento(idGenerado, "DEPOSITO", "Apertura de cuenta en ventanilla", saldoInicial);
            return "OK";
        } catch (SQLException e) {
            return "Error al registrar: " + e.getMessage();
        }
    }

    public String operarVentanilla(Usuario cliente, String montoStr, boolean esDeposito) {
        if (cliente == null) return "Seleccione primero un cliente válido.";
        double monto;
        try {
            monto = Double.parseDouble(montoStr.trim());
            if (monto <= 0) return "El monto debe ser mayor a S/. 0.00.";
        } catch (NumberFormatException e) {
            return "Ingrese un monto numérico válido.";
        }

        // Polimorfismo: delegamos validación y cálculo a la operación correspondiente
        OperacionVentanilla op = OperacionVentanilla.crear(esDeposito);
        String error = op.validar(cliente.getSaldo(), monto);
        if (error != null) return error;

        double nuevoSaldo = op.calcularNuevoSaldo(cliente.getSaldo(), monto);
        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try (PreparedStatement ps = cn.prepareStatement("UPDATE cuentas SET saldo = ? WHERE id_usuario = ?")) {
            ps.setDouble(1, nuevoSaldo);
            ps.setInt(2, cliente.getIdUsuario());
            ps.executeUpdate();
            cliente.setSaldo(nuevoSaldo);
            registrarMovimiento(cliente.getIdUsuario(), op.getTipo(), op.getDetalle(), monto);
            return "OK";
        } catch (SQLException e) {
            return "Error en operación: " + e.getMessage();
        }
    }

    private void registrarMovimiento(int idUsuario, String tipo, String detalle, double monto) throws SQLException {
        LocalDate fecha = (getSesion().getFechaSimulada() != null) ? getSesion().getFechaSimulada() : LocalDate.now();
        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try (PreparedStatement ps = cn.prepareStatement("INSERT INTO transacciones (id_usuario, tipo, detalle, monto, fecha_transaccion) VALUES (?, ?, ?, ?, ?)")) {
            ps.setInt(1, idUsuario); ps.setString(2, tipo); ps.setString(3, detalle);
            ps.setDouble(4, monto); ps.setDate(5, Date.valueOf(fecha));
            ps.executeUpdate();
        }
    }
}
