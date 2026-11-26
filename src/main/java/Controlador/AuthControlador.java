package Controlador;

import Modelo.Cuota;
import Modelo.Usuario;
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

        // Sincroniza alertas en BD para que el Admin las vea en su bandeja de Notificaciones
        notificacionDAO.sincronizarAlertasRelojSP(fechaIngresada);

        return "OK:" + u.getRol();
    }

    public void cerrarSesion() {
        getSesion().cerrarSesion();
    }

    public List<String> obtenerAlertasClienteAlIngresar(LocalDate fechaSimulada, int idCliente) {
        List<String> alertas = new ArrayList<>();
        if (fechaSimulada == null) return alertas;

        List<Cuota> cuotas = creditoDAO.listarHistorialCuotasUsuario(idCliente);
        for (Cuota c : cuotas) {
            if ("PENDIENTE".equals(c.getEstadoCuota())) {
                long dias = ChronoUnit.DAYS.between(fechaSimulada, c.getFechaVencimiento());
                if (dias <= 1) {
                    String montoFmt = String.format("S/. %.2f", c.getMontoTotal());
                    if (dias < 0) {
                        alertas.add("[CUOTA VENCIDA] Tienes la cuota #" + c.getNumeroCuota() + " (" + montoFmt + ") vencida hace " + Math.abs(dias) + " día(s).");
                    } else if (dias == 0) {
                        alertas.add("[VENCE HOY] ¡Recuerda! Hoy vence tu cuota #" + c.getNumeroCuota() + " (" + montoFmt + ").");
                    } else {
                        alertas.add("[VENCE MAÑANA] Tu cuota #" + c.getNumeroCuota() + " (" + montoFmt + ") vence mañana (" + c.getFechaVencimiento() + ").");
                    }
                }
            }
        }
        return alertas;
    }
}
