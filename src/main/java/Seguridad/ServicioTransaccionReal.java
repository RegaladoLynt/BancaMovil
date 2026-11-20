package Seguridad;

import java.util.function.BooleanSupplier;

public class ServicioTransaccionReal implements InterfazTransaccion {
    private final BooleanSupplier accionBancaria;

    public ServicioTransaccionReal(BooleanSupplier accionBancaria) {
        this.accionBancaria = accionBancaria;
    }

    @Override
    public boolean ejecutarOperacion() {
        return accionBancaria.getAsBoolean();
    }
}
