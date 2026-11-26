package Controlador;

import Modelo.AtencionCliente;
import Modelo.Cuota;
import Modelo.Queja;
import Modelo.Sugerencia;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class AtencionControlador extends BaseControlador {

    @Override
    public String obtenerNombreModulo() {
        return "Atención al Cliente y Alertas Inteligentes";
    }

    public String enviarAtencion(int idUsuario, String tipo, String mensaje, LocalDate fecha) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return "Por favor, escribe el detalle de tu " + tipo.toLowerCase() + ".";
        }
        LocalDate fechaReg = (fecha != null) ? fecha : LocalDate.now();
        AtencionCliente registro = "QUEJA".equalsIgnoreCase(tipo)
                ? new Queja(idUsuario, mensaje.trim(), fechaReg)
                : new Sugerencia(idUsuario, mensaje.trim(), fechaReg);

        return atencionDAO.registrarAtencionSP(registro) ? "OK" : "Error al registrar la atención.";
    }

    public DefaultTableModel obtenerTablaAtencion(Integer idUsuarioFiltro) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Fecha", "Cliente", "Tipo", "Mensaje", "Respuesta del Banco"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        List<AtencionCliente> lista = atencionDAO.listarAtencion(idUsuarioFiltro);
        for (AtencionCliente item : lista) {
            modelo.addRow(new Object[]{
                item.getIdRegistro(),
                item.getFechaRegistro(),
                item.getCliente(),
                item.getTipo(),
                item.getMensaje(),
                item.getRespuestaAdmin()
            });
        }
        return modelo;
    }

    public String responderAtencion(int idQueja, String respuesta) {
        if (respuesta == null || respuesta.trim().isEmpty()) {
            return "Escribe una respuesta antes de guardar.";
        }
        return atencionDAO.responder(idQueja, respuesta.trim()) ? "OK" : "Error al guardar respuesta.";
    }

    public List<String> obtenerAlertasReloj(LocalDate fechaSimulada, Integer idUsuarioFiltro) {
        List<String> alertas = new ArrayList<>();
        if (fechaSimulada == null || idUsuarioFiltro == null) return alertas;

        List<Cuota> cuotas = creditoDAO.listarHistorialCuotasUsuario(idUsuarioFiltro);
        for (Cuota c : cuotas) {
            if ("PENDIENTE".equals(c.getEstadoCuota())) {
                long dias = ChronoUnit.DAYS.between(fechaSimulada, c.getFechaVencimiento());
                String montoFmt = String.format("S/. %.2f", c.getMontoTotal());
                if (dias < 0) {
                    alertas.add("[CUOTA VENCIDA] Tienes la cuota #" + c.getNumeroCuota() + " por " + montoFmt + " con " + Math.abs(dias) + " día(s) de atraso. ¡Regularízala hoy!");
                } else if (dias == 0) {
                    alertas.add("[VENCE HOY] ¡Recuerda! Hoy vence tu cuota #" + c.getNumeroCuota() + " por " + montoFmt + ".");
                } else if (dias == 1) {
                    alertas.add("[VENCE MAÑANA] Tu cuota #" + c.getNumeroCuota() + " por " + montoFmt + " vence mañana (" + c.getFechaVencimiento() + ").");
                } else if (dias <= 3) {
                    alertas.add("[PRÓXIMO VENCIMIENTO] Tu cuota #" + c.getNumeroCuota() + " por " + montoFmt + " vence en " + dias + " días (" + c.getFechaVencimiento() + ").");
                }
            }
        }
        return alertas;
    }
}
