package Fabrica;

import Modelo.Movimiento;
import java.time.LocalDate;

public class ComprobanteBuilder {
    private int idUsuario;
    private String tipo;
    private String concepto;
    private String referencia;
    private double monto;
    private LocalDate fecha;
    private boolean vistoAdmin = true;

    public ComprobanteBuilder paraUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
        return this;
    }

    public ComprobanteBuilder deTipo(String tipo) {
        this.tipo = tipo;
        return this;
    }

    public ComprobanteBuilder conConcepto(String concepto) {
        this.concepto = concepto;
        return this;
    }

    public ComprobanteBuilder conReferencia(String referencia) {
        this.referencia = referencia;
        return this;
    }

    public ComprobanteBuilder porMonto(double monto) {
        this.monto = monto;
        return this;
    }

    public ComprobanteBuilder enFecha(LocalDate fecha) {
        this.fecha = fecha;
        return this;
    }

    public ComprobanteBuilder notificarAdmin(boolean notificar) {
        this.vistoAdmin = !notificar;
        return this;
    }

    public Movimiento construir() {
        String detalleFinal = concepto + (referencia != null && !referencia.isEmpty() ? " | Ref: " + referencia : "");
        return new Movimiento(0, idUsuario, tipo, detalleFinal, monto, fecha, vistoAdmin);
    }
}
