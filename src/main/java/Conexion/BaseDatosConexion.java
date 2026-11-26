package Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BaseDatosConexion {
    private static BaseDatosConexion instancia;
    private Connection conectar;

    private final String url = "jdbc:mysql://localhost:3306/banca_digital?serverTimezone=UTC&useSSL=false";
    private final String usuario = "root";
    private final String clave = "admin";

    private BaseDatosConexion() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.conectar = DriverManager.getConnection(url, usuario, clave);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("[Error Singleton] " + e.getMessage());
        }
    }

    public static synchronized BaseDatosConexion getInstancia() {
        if (instancia == null) {
            instancia = new BaseDatosConexion();
        }
        return instancia;
    }

    public static synchronized BaseDatosConexion GetInstancia() {
        return getInstancia();
    }

    public Connection getConectar() {
        try {
            if (this.conectar == null || this.conectar.isClosed()) {
                this.conectar = DriverManager.getConnection(url, usuario, clave);
            }
        } catch (SQLException e) {
            System.out.println("[Error Conexion] " + e.getMessage());
        }
        return this.conectar;
    }

    public Connection GetConectar() {
        return getConectar();
    }
}
