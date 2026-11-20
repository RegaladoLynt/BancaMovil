package Vista;

import java.awt.Color;
import javax.swing.JToggleButton;

public class EstiloUI {
    // Paleta ejecutiva VIP del banco
    public static final Color AZUL_OSCURO = new Color(27, 42, 74);    // #1B2A4A
    public static final Color AZUL_MEDIO  = new Color(44, 62, 102);   // #2C3E66
    public static final Color DORADO      = new Color(184, 134, 11);  // #B8860B
    public static final Color BLANCO_HUMO = new Color(244, 246, 249); // #F4F6F9
    public static final Color ROJO_SALIR  = new Color(169, 50, 38);   // #A93226

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
}
