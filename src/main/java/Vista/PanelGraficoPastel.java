package Vista;

import Modelo.Cuota;
import Procesos.ProcesoGrafico;
import java.awt.*;
import java.util.List;
import javax.swing.JPanel;

public class PanelGraficoPastel extends JPanel implements GraficoEstadistico {
    private List<Cuota> cuotas;

    public PanelGraficoPastel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(420, 270));
    }

    @Override
    public void actualizarDatos(List<Cuota> cuotas) {
        this.cuotas = cuotas;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int total = (cuotas == null) ? 0 : cuotas.size();
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(new Color(44, 62, 80));
        g2.drawString("Estado General de Letras (" + total + " en total)", 20, 25);

        if (total == 0) {
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            g2.setColor(Color.GRAY);
            g2.drawString("El cliente no tiene un crédito activo.", 20, 120);
            return;
        }

        int verde = ProcesoGrafico.contarATiempo(cuotas);
        int rojo = ProcesoGrafico.contarAtrasadas(cuotas);
        int morado = ProcesoGrafico.contarPendientes(cuotas);

        int gVerde = ProcesoGrafico.calcularGrados(verde, total);
        int gRojo = ProcesoGrafico.calcularGrados(rojo, total);
        int gMorado = 360 - gVerde - gRojo;

        int x = 25, y = 45, diametro = 175;

        g2.setColor(new Color(46, 204, 113)); // Verde
        g2.fillArc(x, y, diametro, diametro, 0, gVerde);

        g2.setColor(new Color(231, 76, 60));  // Rojo
        g2.fillArc(x, y, diametro, diametro, gVerde, gRojo);

        g2.setColor(new Color(155, 89, 182)); // Morado
        g2.fillArc(x, y, diametro, diametro, gVerde + gRojo, gMorado);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x, y, diametro, diametro);

        int lx = 220, ly = 75;
        dibujarLeyenda(g2, lx, ly, new Color(46, 204, 113), String.format("A tiempo: %d (%.1f%%)", verde, ProcesoGrafico.calcularPorcentaje(verde, total)));
        dibujarLeyenda(g2, lx, ly + 38, new Color(231, 76, 60), String.format("Atrasadas: %d (%.1f%%)", rojo, ProcesoGrafico.calcularPorcentaje(rojo, total)));
        dibujarLeyenda(g2, lx, ly + 76, new Color(155, 89, 182), String.format("Por pagar: %d (%.1f%%)", morado, ProcesoGrafico.calcularPorcentaje(morado, total)));
    }

    private void dibujarLeyenda(Graphics2D g2, int x, int y, Color color, String texto) {
        g2.setColor(color);
        g2.fillRect(x, y, 16, 16);
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString(texto, x + 22, y + 13);
    }
}
