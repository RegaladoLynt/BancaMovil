package Procesos;

import Modelo.Cuota;
import Modelo.Movimiento;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class ProcesoVista {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String formatearSoles(double monto) {
        return String.format("S/. %.2f", monto);
    }

    public static DefaultTableModel construirModeloTablaCuotas(List<Cuota> cuotas) {
        String[] columnas = {"N°", "Capital", "Interés", "Multa", "Total", "Vencimiento", "Fecha Pago", "Atraso", "Estado"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        if (cuotas != null) {
            for (Cuota c : cuotas) {
                modelo.addRow(new Object[]{
                    c.getNumeroCuota(),
                    formatearSoles(c.getMontoCapital()),
                    formatearSoles(c.getMontoInteres()),
                    formatearSoles(c.getMontoMulta()),
                    formatearSoles(c.getMontoTotal()),
                    c.getFechaVencimiento().format(FMT),
                    c.getFechaPago() != null ? c.getFechaPago().format(FMT) : "-",
                    c.getDiasAtraso() + " días",
                    c.getEstadoCuota()
                });
            }
        }
        return modelo;
    }

    public static DefaultTableModel construirModeloTablaMovimientos(List<Movimiento> movimientos) {
        String[] columnas = {"Fecha", "Tipo Operación", "Detalle", "Monto"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        if (movimientos != null) {
            for (Movimiento m : movimientos) {
                modelo.addRow(new Object[]{
                    m.getFechaOperacion().format(FMT),
                    m.getTipo(),
                    m.getDetalle(),
                    formatearSoles(m.getMonto())
                });
            }
        }
        return modelo;
    }
}
