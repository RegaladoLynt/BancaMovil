package Vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class PanelGraficoBalance extends PanelGraficoBase {
    private final double ingresos;
    private final double egresos;

    public PanelGraficoBalance(double ingresos, double egresos) {
        super("Balance Mensual");
        this.ingresos = ingresos;
        this.egresos = egresos;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = prepararLienzo(g);

        double total = ingresos + egresos;
        int w = getWidth();
        int h = getHeight();

        if (total <= 0) {
            dibujarMensajeVacio(g2, "Sin movimientos registrados en este mes");
            return;
        }

        int diametro = Math.min(w - 40, h - 75);
        int x = (w - diametro) / 2;
        int y = 12;

        int anguloIngresos = (int) Math.round((ingresos / total) * 360.0);
        int anguloEgresos = 360 - anguloIngresos;

        Color verde = new Color(30, 132, 73);
        Color rojo = new Color(192, 57, 43);

        g2.setColor(verde);
        g2.fillArc(x, y, diametro, diametro, 90, anguloIngresos);

        g2.setColor(rojo);
        g2.fillArc(x, y, diametro, diametro, 90 + anguloIngresos, anguloEgresos);

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
