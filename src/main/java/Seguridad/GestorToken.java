package Seguridad;

import java.security.SecureRandom;
import javax.swing.JOptionPane;

public class GestorToken {
    private static final SecureRandom random = new SecureRandom();

    public static boolean solicitarYValidarToken(String celular, String conceptoOperacion) {
        int codigo = 100000 + random.nextInt(900000);
        String tokenGenerado = String.valueOf(codigo);

        String mensajeNotificacion = "Token enviado a " + celular + ":\n"
                + "Tu token personal\n"
                + conceptoOperacion + "\n"
                + "es " + tokenGenerado;

        JOptionPane.showMessageDialog(null, mensajeNotificacion, "SMS - Seguridad Bancaria", JOptionPane.INFORMATION_MESSAGE);

        String ingreso = JOptionPane.showInputDialog(null, "Ingrese el token de 6 dígitos recibido:", "Verificación 2FA", JOptionPane.QUESTION_MESSAGE);

        if (ingreso != null && ingreso.trim().equals(tokenGenerado)) {
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Token incorrecto o cancelado. Operación rechazada.", "Error de Seguridad", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
