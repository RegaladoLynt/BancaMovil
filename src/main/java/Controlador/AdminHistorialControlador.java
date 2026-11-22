package Controlador;

import Modelo.Usuario;
import Vista.EstiloUI;
import Vista.PnlEstadistica;
import Vista.PnlHistorial;
import Vista.PnlTransacciones;

public class AdminHistorialControlador extends BaseControlador {
    private final PnlHistorial vista;
    private final Usuario cliente;

    public AdminHistorialControlador(PnlHistorial vista, Usuario cliente) {
        this.vista = vista;
        this.cliente = cliente;
        inicializar();
    }

    @Override
    public String obtenerNombreModulo() {
        return "Orquestador Pestaña Historial";
    }

    private void inicializar() {
        vista.btnTransacciones.addActionListener(e -> mostrarTransacciones());
        vista.btnIngresosMen.addActionListener(e -> mostrarEstadistica());
        mostrarTransacciones();
    }

    private void mostrarTransacciones() {
        EstiloUI.aplicarEstiloPestanas(vista.btnTransacciones, vista.btnTransacciones, vista.btnIngresosMen);
        PnlTransacciones pnl = new PnlTransacciones();
        new AdminTransaccionesControlador(pnl, cliente);
        EstiloUI.cambiarVista(vista.Graficas, pnl);
    }

    private void mostrarEstadistica() {
        EstiloUI.aplicarEstiloPestanas(vista.btnIngresosMen, vista.btnTransacciones, vista.btnIngresosMen);
        PnlEstadistica pnl = new PnlEstadistica();
        new AdminEstadisticaControlador(pnl, cliente);
        EstiloUI.cambiarVista(vista.Graficas, pnl);
    }
}
