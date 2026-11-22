package Vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class PanelGraficoPuntualidad extends JPanel {
    private final int puntuales;
    private final int atrasadas;
    private final int porPagar;

    public PanelGraficoPuntualidad(int puntuales, int atrasadas, int porPagar) {
        this.puntuales = puntuales;
        this.atrasadas = atrasadas;
        this.porPagar = porPagar;
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int total = puntuales + atrasadas + porPagar;
        int w = getWidth();
        int h = getHeight();

        if (total <= 0) {
            g2.setColor(new Color(127, 140, 141));
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            g2.drawString("Sin historial de cuotas", Math.max(20, (w - 130) / 2), h / 2);
            return;
        }

        int d = Math.min(w - 30, h - 20);
        int x = (w - d) / 2;
        int y = (h - d) / 2;

        int angPuntual = (int) Math.round((puntuales * 360.0) / total);
        int angAtrasado = (int) Math.round((atrasadas * 360.0) / total);
        int angPorPagar = 360 - angPuntual - angAtrasado;

        g2.setColor(new Color(119, 221, 119)); // Verde pastel
        g2.fillArc(x, y, d, d, 90, angPuntual);

        g2.setColor(new Color(255, 105, 97));  // Rojo pastel
        g2.fillArc(x, y, d, d, 90 + angPuntual, angAtrasado);

        g2.setColor(new Color(179, 158, 181)); // Morado pastel
        g2.fillArc(x, y, d, d, 90 + angPuntual + angAtrasado, angPorPagar);
    }
}
