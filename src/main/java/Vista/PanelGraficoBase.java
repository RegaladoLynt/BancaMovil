package Vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public abstract class PanelGraficoBase extends JPanel implements GraficoEstadistico {
    protected String tituloGrafico;

    public PanelGraficoBase(String tituloGrafico) {
        this.tituloGrafico = tituloGrafico;
        setBackground(Color.WHITE);
    }

    protected Graphics2D prepararLienzo(java.awt.Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g2;
    }

    protected void dibujarMensajeVacio(Graphics2D g2, String mensaje) {
        g2.setColor(new Color(127, 140, 141));
        g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        int x = Math.max(20, (getWidth() - (mensaje.length() * 6)) / 2);
        g2.drawString(mensaje, x, getHeight() / 2);
    }

    @Override
    public void refrescarGrafico() {
        repaint();
    }
}
