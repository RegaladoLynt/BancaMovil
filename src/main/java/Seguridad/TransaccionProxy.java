package Seguridad;

import Conexion.SesionSistema;
import java.util.function.BooleanSupplier;
import javax.swing.JOptionPane;

public class TransaccionProxy implements InterfazTransaccion {
    private final ServicioTransaccionReal servicioReal;
    private final String conceptoOperacion;
    private final double monto;

    public TransaccionProxy(String conceptoOperacion, double monto, BooleanSupplier accionBancaria) {
        this.conceptoOperacion = conceptoOperacion;
        this.monto = monto;
        this.servicioReal = new ServicioTransaccionReal(accionBancaria);
    }

    @Override
    public boolean ejecutarOperacion() {
        if (monto <= 0) {
            JOptionPane.showMessageDialog(null, "El monto debe ser mayor a S/. 0.00", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String celular = SesionSistema.getInstancia().getCelular();
        boolean tokenValido = GestorToken.solicitarYValidarToken(celular, conceptoOperacion);

        if (tokenValido) {
            return servicioReal.ejecutarOperacion();
        }
        return false;
    }
}
