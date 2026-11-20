package Controlador;

import Comportamiento.AlertaVencimiento;
import Conexion.BaseDatosConexion;
import Modelo.Usuario;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class AuthControlador extends BaseControlador {
    private final DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public String obtenerNombreModulo() {
        return "Módulo de Autenticación y Control de Fecha";
    }

    public String obtenerFechaMinimaTexto() {
        return usuarioDAO.obtenerFechaMinima().format(formato);
    }

    public String iniciarSesion(String username, String password, String fechaTexto) {
        if (username.isEmpty() || password.isEmpty() || fechaTexto.isEmpty()) {
            return "Complete todos los campos (usuario, contraseña y fecha).";
        }

        LocalDate fechaIngresada;
        try {
            fechaIngresada = LocalDate.parse(fechaTexto.trim(), formato);
        } catch (DateTimeParseException e) {
            return "Formato de fecha inválido. Use dd/MM/yyyy (Ej: 12/12/2012).";
        }

        LocalDate fechaMinima = usuarioDAO.obtenerFechaMinima();
        if (fechaIngresada.isBefore(fechaMinima)) {
            return "La fecha no puede ser menor a la fecha mínima permitida (" + fechaMinima.format(formato) + ").";
        }

        Usuario u = usuarioDAO.validarLogin(username.trim(), password.trim());
        if (u == null) {
            return "Credenciales incorrectas o usuario bloqueado.";
        }

        usuarioDAO.actualizarFechaMinima(fechaIngresada);
        getSesion().iniciarSesion(
            u.getIdUsuario(), u.getDni(), u.getNombreCompleto(),
            u.getCelular(), u.getRol(), fechaIngresada
        );
        return "OK:" + u.getRol();
    }

    public void cerrarSesion() {
        getSesion().cerrarSesion();
    }

    public List<String> verificarAlertasVencimientoAlIngresar(LocalDate fechaSimulada, Integer idUsuarioFiltro) {
        List<String> alertas = new ArrayList<>();
        String sql = "SELECT u.id_usuario, CONCAT(u.nombres, ' ', u.apellidos) AS cliente, " +
                     "c.numero_cuota, c.monto_cuota, c.fecha_vencimiento " +
                     "FROM cuotas c " +
                     "INNER JOIN creditos cr ON c.id_credito = cr.id_credito " +
                     "INNER JOIN usuarios u ON cr.id_usuario = u.id_usuario " +
                     "WHERE c.estado = 'POR_PAGAR' " +
                     (idUsuarioFiltro != null ? "AND u.id_usuario = ? " : "");

        Connection cn = BaseDatosConexion.GetInstancia().GetConectar();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (idUsuarioFiltro != null) {
                ps.setInt(1, idUsuarioFiltro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate venc = rs.getDate("fecha_vencimiento").toLocalDate();
                    long dias = ChronoUnit.DAYS.between(fechaSimulada, venc);

                    AlertaVencimiento alerta = AlertaVencimiento.crearSegunDias(
                            dias, rs.getString("cliente"), rs.getInt("numero_cuota"), rs.getDouble("monto_cuota"), venc
                    );

                    if (alerta != null) {
                        String msg = alerta.generarMensaje();
                        alertas.add(msg);

                        String sqlCheck = "SELECT COUNT(*) FROM notificaciones WHERE mensaje = ? AND fecha_notificacion = ?";
                        try (PreparedStatement psCheck = cn.prepareStatement(sqlCheck)) {
                            psCheck.setString(1, msg);
                            psCheck.setDate(2, Date.valueOf(fechaSimulada));
                            try (ResultSet rsCheck = psCheck.executeQuery()) {
                                if (rsCheck.next() && rsCheck.getInt(1) == 0) {
                                    String sqlIns = "INSERT INTO notificaciones (id_usuario_origen, mensaje, fecha_notificacion, leida) VALUES (?, ?, ?, FALSE)";
                                    try (PreparedStatement psIns = cn.prepareStatement(sqlIns)) {
                                        psIns.setInt(1, rs.getInt("id_usuario"));
                                        psIns.setString(2, msg);
                                        psIns.setDate(3, Date.valueOf(fechaSimulada));
                                        psIns.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error verificando alertas: " + e.getMessage());
        }
        return alertas;
    }
}
