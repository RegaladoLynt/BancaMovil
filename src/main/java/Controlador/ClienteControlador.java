package Controlador;

import Comportamiento.EstrategiaPagoCuota;
import Comportamiento.NotificadorEvento;
import Fabrica.BancaProductoFabrica;
import Fabrica.ComprobanteBuilder;
import Fabrica.CreditoPersonal;
import Modelo.*;
import Seguridad.TransaccionProxy;
import Servicios.PasarelaPagoAdapter;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JOptionPane;

public class ClienteControlador extends BaseControlador {

    @Override
    public String obtenerNombreModulo() {
        return "Banca Digital - Portal Cliente (" + getSesion().getNombreCompleto() + ")";
    }

    public Cuenta obtenerCuentaActual() {
        return usuarioDAO.obtenerCuentaPorUsuario(getSesion().getIdUsuario());
    }

    public List<Movimiento> obtenerMovimientosActuales() {
        return usuarioDAO.listarMovimientosUsuario(getSesion().getIdUsuario());
    }

    public Credito obtenerCreditoActivo() {
        return creditoDAO.obtenerCreditoActivo(getSesion().getIdUsuario());
    }

    public List<Cuota> obtenerCuotasConActualizacionMora(int idCredito) {
        List<Cuota> cuotas = creditoDAO.listarCuotasPorCredito(idCredito);
        LocalDate hoy = getSesion().getFechaSimulada();
        for (Cuota c : cuotas) {
            if ("PENDIENTE".equals(c.getEstadoCuota()) && hoy != null && hoy.isAfter(c.getFechaVencimiento())) {
                EstrategiaPagoCuota.aplicarProrrogaPorMora(c, hoy);
                creditoDAO.actualizarCuota(c);
            }
        }
        return creditoDAO.listarCuotasPorCredito(idCredito);
    }

    public OfertaCredito obtenerOfertaDisponible() {
        return creditoDAO.obtenerOfertaDisponible(getSesion().getIdUsuario());
    }

    public boolean realizarTransferencia(String numeroCuentaDestino, double monto) {
        Cuenta origen = obtenerCuentaActual();
        Cuenta destino = usuarioDAO.obtenerCuentaPorNumero(numeroCuentaDestino.trim());

        if (destino == null) {
            JOptionPane.showMessageDialog(null, "La cuenta destino no existe.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (origen.getNumeroCuenta().equals(destino.getNumeroCuenta())) {
            JOptionPane.showMessageDialog(null, "No puede transferir a su misma cuenta.", "Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (origen.getSaldo() < monto) {
            JOptionPane.showMessageDialog(null, "Saldo insuficiente.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String concepto = "Transferencia a cuenta " + destino.getNumeroCuenta() + " por S/. " + monto;
        TransaccionProxy proxy = new TransaccionProxy(concepto, monto, () -> {
            LocalDate hoy = getSesion().getFechaSimulada();
            usuarioDAO.actualizarSaldo(origen.getIdCuenta(), origen.getSaldo() - monto);
            usuarioDAO.actualizarSaldo(destino.getIdCuenta(), destino.getSaldo() + monto);

            boolean inusual = monto > origen.getLimiteDiario();
            Movimiento movEmisor = new ComprobanteBuilder()
                    .paraUsuario(origen.getIdUsuario()).deTipo("TRANSFERENCIA_ENVIADA")
                    .conConcepto("Transferencia enviada a " + destino.getNumeroCuenta())
                    .porMonto(monto).enFecha(hoy).notificarAdmin(inusual).construir();

            Movimiento movReceptor = new ComprobanteBuilder()
                    .paraUsuario(destino.getIdUsuario()).deTipo("TRANSFERENCIA_RECIBIDA")
                    .conConcepto("Transferencia recibida de " + getSesion().getNombreCompleto())
                    .porMonto(monto).enFecha(hoy).construir();

            usuarioDAO.registrarMovimiento(movEmisor);
            usuarioDAO.registrarMovimiento(movReceptor);

            if (inusual) {
                usuarioDAO.registrarMovimiento(NotificadorEvento.crearNotificacionAdmin(
                        origen.getIdUsuario(), "ALERTA_SEGURIDAD",
                        "Operación inusual: Superó límite diario (S/. " + monto + ")", monto, hoy));
            }
            return true;
        });

        return proxy.ejecutarOperacion();
    }

    public boolean pagarServicio(String empresa, String suministro, double monto) {
        Cuenta origen = obtenerCuentaActual();
        if (origen.getSaldo() < monto) {
            JOptionPane.showMessageDialog(null, "Saldo insuficiente para pagar el servicio.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String concepto = "Pago de servicio de " + empresa;
        TransaccionProxy proxy = new TransaccionProxy(concepto, monto, () -> {
            LocalDate hoy = getSesion().getFechaSimulada();
            String detalleAdaptado = PasarelaPagoAdapter.procesarPagoServicioExterno(empresa, suministro, monto);
            usuarioDAO.actualizarSaldo(origen.getIdCuenta(), origen.getSaldo() - monto);

            Movimiento mov = new ComprobanteBuilder()
                    .paraUsuario(origen.getIdUsuario()).deTipo("PAGO_SERVICIO")
                    .conConcepto(detalleAdaptado).porMonto(monto).enFecha(hoy).construir();
            return usuarioDAO.registrarMovimiento(mov);
        });

        return proxy.ejecutarOperacion();
    }

    public boolean pagarCuotaCredito(Cuota cuota) {
        Cuenta origen = obtenerCuentaActual();
        LocalDate hoy = getSesion().getFechaSimulada();
        EstrategiaPagoCuota.procesarCalculoPago(cuota, hoy);
        double totalPagar = cuota.getMontoTotal();

        if (origen.getSaldo() < totalPagar) {
            JOptionPane.showMessageDialog(null, "Saldo insuficiente. Total requerido: S/. " + totalPagar, "Saldo Insuficiente", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String concepto = "Pago de Cuota #" + cuota.getNumeroCuota() + " por S/. " + totalPagar;
        TransaccionProxy proxy = new TransaccionProxy(concepto, totalPagar, () -> {
            usuarioDAO.actualizarSaldo(origen.getIdCuenta(), origen.getSaldo() - totalPagar);
            creditoDAO.actualizarCuota(cuota);
            creditoDAO.verificarYFinalizarCredito(cuota.getIdCredito());

            Movimiento mov = new ComprobanteBuilder()
                    .paraUsuario(origen.getIdUsuario()).deTipo("PAGO_CUOTA")
                    .conConcepto("Pago Cuota #" + cuota.getNumeroCuota() + " (" + cuota.getEstadoCuota() + ")")
                    .porMonto(totalPagar).enFecha(hoy).construir();
            return usuarioDAO.registrarMovimiento(mov);
        });

        return proxy.ejecutarOperacion();
    }

    public boolean aceptarOfertaCredito(OfertaCredito oferta, int cuotasElegidas) {
        String concepto = "Desembolso de Crédito Preaprobado por S/. " + oferta.getMontoOfertado();
        TransaccionProxy proxy = new TransaccionProxy(concepto, oferta.getMontoOfertado(), () -> {
            LocalDate hoy = getSesion().getFechaSimulada();
            int idUsuario = getSesion().getIdUsuario();
            Cuenta cuenta = obtenerCuentaActual();

            CreditoPersonal nuevoCred = BancaProductoFabrica.crearCreditoPersonal(
                    idUsuario, oferta.getMontoOfertado(), oferta.getTasaInteres(), cuotasElegidas, hoy);

            if (creditoDAO.crearCreditoConCuotas(nuevoCred.getCredito(), nuevoCred.getCronograma())) {
                creditoDAO.marcarOfertaAceptada(oferta.getIdOferta());
                usuarioDAO.actualizarSaldo(cuenta.getIdCuenta(), cuenta.getSaldo() + oferta.getMontoOfertado());

                Movimiento notifAdmin = NotificadorEvento.crearNotificacionAdmin(
                        idUsuario, "CREDITO_SOLICITADO",
                        "Cliente " + getSesion().getNombreCompleto() + " aceptó crédito de S/. " + oferta.getMontoOfertado() + " a " + cuotasElegidas + " cuotas.",
                        oferta.getMontoOfertado(), hoy);
                return usuarioDAO.registrarMovimiento(notifAdmin);
            }
            return false;
        });

        return proxy.ejecutarOperacion();
    }
}
