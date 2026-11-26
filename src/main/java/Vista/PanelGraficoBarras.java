package Vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class PanelGraficoBarras extends PanelGraficoBase {
    private final int dia1;
    private final int dia3;
    private final int dia7Mas;

    public PanelGraficoBarras(int dia1, int dia3, int dia7Mas) {
        super("Detalle de Cuotas Atrasadas");
        this.dia1 = dia1;
        this.dia3 = dia3;
        this.dia7Mas = dia7Mas;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = prepararLienzo(g);

        int max = Math.max(1, Math.max(dia1, Math.max(dia3, dia7Mas)));
        int total = dia1 + dia3 + dia7Mas;
        int w = getWidth();
        int h = getHeight();

        if (total <= 0) {
            dibujarMensajeVacio(g2, "Sin retrasos registrados");
            return;
        }

        int anchoBarra = 48;
        int base = h - 25;
        int alturaMax = h - 55;
        int espacio = (w - (anchoBarra * 3)) / 4;

        int h1 = (int) ((dia1 * 1.0 / max) * alturaMax);
        int h3 = (int) ((dia3 * 1.0 / max) * alturaMax);
        int h7 = (int) ((dia7Mas * 1.0 / max) * alturaMax);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));

        // 1 día
        int x1 = espacio;
        g2.setColor(new Color(253, 253, 150));
        g2.fillRect(x1, base - h1, anchoBarra, h1);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(x1, base - h1, anchoBarra, h1);
        g2.drawString(dia1 + " (1d)", x1 + 8, base + 15);

        // 3 días
        int x2 = espacio * 2 + anchoBarra;
        g2.setColor(new Color(255, 179, 71));
        g2.fillRect(x2, base - h3, anchoBarra, h3);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(x2, base - h3, anchoBarra, h3);
        g2.drawString(dia3 + " (3d)", x2 + 8, base + 15);

        // 7+ días
        int x3 = espacio * 3 + anchoBarra * 2;
        g2.setColor(new Color(255, 105, 97));
        g2.fillRect(x3, base - h7, anchoBarra, h7);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(x3, base - h7, anchoBarra, h7);
        g2.drawString(dia7Mas + " (7+d)", x3 + 4, base + 15);
    }
}
