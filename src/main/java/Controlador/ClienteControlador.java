package Controlador;

import Comportamiento.EstrategiaPagoCuota;
import Modelo.Credito;
import Modelo.Cuenta;
import Modelo.Cuota;
import Modelo.CuotaMemento;
import Modelo.Movimiento;
import Modelo.OfertaCredito;
import Seguridad.PasarelaPago;
import Seguridad.PasarelaPagoAdapter;
import Seguridad.TransaccionProxy;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JOptionPane;

public class ClienteControlador extends BaseControlador {
    private final PasarelaPago pasarelaPago = new PasarelaPagoAdapter();

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
        return creditoDAO.listarCuotasPorCredito(idCredito);
    }

    public OfertaCredito obtenerOfertaDisponible() {
        return creditoDAO.obtenerOfertaDisponible(getSesion().getIdUsuario());
    }

    public boolean realizarTransferencia(String numeroCuentaDestino, double monto) {
        Cuenta origen = obtenerCuentaActual();
        if (origen.getNumeroCuenta().equals(numeroCuentaDestino.trim())) {
            JOptionPane.showMessageDialog(null, "No puede transferir a su misma cuenta.", "Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (origen.getSaldo() < monto) {
            JOptionPane.showMessageDialog(null, "Saldo insuficiente.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String concepto = "Transferencia a cuenta " + numeroCuentaDestino.trim() + " por S/. " + monto;
        TransaccionProxy proxy = new TransaccionProxy(concepto, monto, () -> {
            LocalDate hoy = getSesion().getFechaSimulada();
            String res = usuarioDAO.realizarTransferenciaSP(getSesion().getIdUsuario(), numeroCuentaDestino.trim(), monto, hoy);
            if ("OK".equals(res)) {
                return true;
            }
            JOptionPane.showMessageDialog(null, res, "Error en Transferencia", JOptionPane.ERROR_MESSAGE);
            return false;
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
            String detalleAdaptado = pasarelaPago.procesarPago(empresa, suministro, monto);
            String res = usuarioDAO.pagarServicioSP(getSesion().getIdUsuario(), detalleAdaptado, monto, hoy);
            if ("OK".equals(res)) {
                return true;
            }
            JOptionPane.showMessageDialog(null, res, "Error en Pago de Servicio", JOptionPane.ERROR_MESSAGE);
            return false;
        });

        return proxy.ejecutarOperacion();
    }

    public boolean pagarCuotaCredito(Cuota cuota) {
        Cuenta origen = obtenerCuentaActual();
        LocalDate hoy = getSesion().getFechaSimulada();

        CuotaMemento respaldo = cuota.guardarEstado();
        EstrategiaPagoCuota.procesarCalculoPago(cuota, hoy);
        double totalPagar = cuota.getMontoTotal();

        if (origen.getSaldo() < totalPagar) {
            cuota.restaurarEstado(respaldo);
            JOptionPane.showMessageDialog(null, "Saldo insuficiente. Total requerido: S/. " + String.format("%.2f", totalPagar), "Saldo Insuficiente", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String concepto = "Pago de Cuota #" + cuota.getNumeroCuota() + " por S/. " + String.format("%.2f", totalPagar);
        TransaccionProxy proxy = new TransaccionProxy(concepto, totalPagar, () -> {
            String res = creditoDAO.pagarCuotaSP(cuota.getIdCuota(), getSesion().getIdUsuario(), hoy);
            if ("OK".equals(res)) {
                return true;
            }
            JOptionPane.showMessageDialog(null, res, "Error al Pagar Cuota", JOptionPane.ERROR_MESSAGE);
            return false;
        });

        boolean exito = proxy.ejecutarOperacion();
        if (!exito) {
            cuota.restaurarEstado(respaldo);
        }
        return exito;
    }

    public boolean aceptarOfertaCredito(OfertaCredito oferta, int cuotasElegidas) {
        String concepto = "Desembolso de Crédito Preaprobado por S/. " + oferta.getMontoOfertado();
        TransaccionProxy proxy = new TransaccionProxy(concepto, oferta.getMontoOfertado(), () -> {
            LocalDate hoy = getSesion().getFechaSimulada();
            String res = creditoDAO.desembolsarCreditoSP(oferta.getIdOferta(), getSesion().getIdUsuario(), cuotasElegidas, hoy);
            if ("OK".equals(res)) {
                return true;
            }
            JOptionPane.showMessageDialog(null, res, "Error en Desembolso", JOptionPane.ERROR_MESSAGE);
            return false;
        });

        return proxy.ejecutarOperacion();
    }
}
