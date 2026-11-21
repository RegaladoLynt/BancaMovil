package Vista;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

/** Utilidades de estilo compartidas por el portal del cliente. */
public final class ClienteUI {

    public static final Color CAMPO = new Color(235, 240, 248);

    private ClienteUI() {}

    public static JButton boton(String texto, Color fondo) {
        JButton b = new JButton(texto);
        b.setBackground(fondo);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        return b;
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(EstiloUI.AZUL_OSCURO);
        return l;
    }

    public static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return l;
    }

    public static void campo(JComponent c, String titulo) {
        c.setBackground(CAMPO);
        c.setFont(new Font("Segoe UI", Font.BOLD, 14));
        c.setBorder(BorderFactory.createTitledBorder(null, titulo,
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.ITALIC, 12), EstiloUI.AZUL_OSCURO));
    }

    public static void tabla(JTable t) {
        t.setRowHeight(22);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setBackground(EstiloUI.AZUL_OSCURO);
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setSelectionBackground(EstiloUI.DORADO);
        t.setSelectionForeground(Color.WHITE);
    }

    /** Convierte texto a monto; devuelve -1 si no es un número válido. */
    public static double leerMonto(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (NumberFormatException | NullPointerException e) {
            return -1;
        }
    }
}
