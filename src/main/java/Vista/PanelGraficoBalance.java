package Vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class PanelGraficoBalance extends JPanel {
    private final double ingresos;
    private final double egresos;

    public PanelGraficoBalance(double ingresos, double egresos) {
        this.ingresos = ingresos;
        this.egresos = egresos;
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double total = ingresos + egresos;
        int w = getWidth();
        int h = getHeight();

        if (total <= 0) {
            g2.setColor(new Color(93, 109, 126));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString("Sin movimientos registrados en este mes", Math.max(20, (w - 250) / 2), h / 2);
            return;
        }

        int diametro = Math.min(w - 40, h - 75);
        int x = (w - diametro) / 2;
        int y = 12;

        int anguloIngresos = (int) Math.round((ingresos / total) * 360.0);
        int anguloEgresos = 360 - anguloIngresos;

        Color verde = new Color(30, 132, 73);
        Color rojo = new Color(192, 57, 43);

        // Sector Verde (Ingresos)
        g2.setColor(verde);
        g2.fillArc(x, y, diametro, diametro, 90, anguloIngresos);

        // Sector Rojo (Egresos)
        g2.setColor(rojo);
        g2.fillArc(x, y, diametro, diametro, 90 + anguloIngresos, anguloEgresos);

        // Leyenda inferior con color, nombre, cantidad y porcentaje
        double pctIng = (ingresos / total) * 100.0;
        double pctEgr = (egresos / total) * 100.0;
        int yLeyenda = y + diametro + 18;

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.setColor(verde);
        g2.fillRect(18, yLeyenda - 10, 11, 11);
        g2.drawString(String.format("Ingresos: S/. %.2f (%.1f%%)", ingresos, pctIng), 34, yLeyenda);

        g2.setColor(rojo);
        g2.fillRect(18, yLeyenda + 10, 11, 11);
        g2.drawString(String.format("Egresos:  S/. %.2f (%.1f%%)", egresos, pctEgr), 34, yLeyenda + 20);
    }
}
