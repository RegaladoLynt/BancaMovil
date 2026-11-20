package Vista;

import Modelo.Cuota;
import Procesos.ProcesoGrafico;
import java.awt.*;
import java.util.List;
import javax.swing.JPanel;

public class PanelGraficoBarras extends JPanel implements GraficoEstadistico {
    private List<Cuota> cuotas;

    public PanelGraficoBarras() {
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

        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(new Color(44, 62, 80));
        g2.drawString("Detalle de Cuotas Atrasadas por Días de Demora", 20, 25);

        int b1 = ProcesoGrafico.contarAtraso1Dia(cuotas);
        int b3 = ProcesoGrafico.contarAtraso3Dias(cuotas);
        int b7 = ProcesoGrafico.contarAtraso7Dias(cuotas);

        int max = Math.max(1, Math.max(b1, Math.max(b3, b7)));
        int baseLinea = 210;
        int alturaMax = 140;

        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(40, baseLinea, 380, baseLinea);

        dibujarBarra(g2, 75, baseLinea, alturaMax, b1, max, new Color(241, 196, 15), "1 día (" + b1 + ")");
        dibujarBarra(g2, 185, baseLinea, alturaMax, b3, max, new Color(230, 126, 34), "3 días (" + b3 + ")");
        dibujarBarra(g2, 295, baseLinea, alturaMax, b7, max, new Color(231, 76, 60), "7+ días (" + b7 + ")");
    }

    private void dibujarBarra(Graphics2D g2, int x, int baseY, int alturaMax, int valor, int max, Color color, String etiqueta) {
        int alto = (int) ((valor * 1.0 / max) * alturaMax);
        g2.setColor(color);
        g2.fillRect(x, baseY - alto, 60, alto);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(x, baseY - alto, 60, alto);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString(etiqueta, x + 2, baseY + 20);
    }
}
