package Vista;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class EstiloUI {
    public static final Color AZUL_OSCURO = new Color(27, 42, 74);
    public static final Color AZUL_MEDIO  = new Color(44, 62, 102);
    public static final Color DORADO      = new Color(184, 134, 11);
    public static final Color BLANCO_HUMO = new Color(244, 246, 249);
    public static final Color ROJO_SALIR  = new Color(169, 50, 38);

    public static void aplicarEstiloPestanas(JToggleButton activo, JToggleButton... todas) {
        for (JToggleButton btn : todas) {
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);
            if (btn == activo) {
                btn.setBackground(DORADO);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(AZUL_MEDIO);
                btn.setForeground(Color.WHITE);
            }
        }
    }

    public static void cambiarVista(JPanel contenedor, JPanel nuevaVista) {
        contenedor.setLayout(new BorderLayout());
        contenedor.removeAll();
        contenedor.add(nuevaVista, BorderLayout.CENTER);
        contenedor.revalidate();
        contenedor.repaint();
    }

    // Sobrecarga polimórfica para insertar y refrescar cualquier PanelGraficoBase en su contenedor
    public static void mostrarGrafico(JPanel contenedor, PanelGraficoBase grafico) {
        cambiarVista(contenedor, grafico);
        grafico.refrescarGrafico();
    }
}
