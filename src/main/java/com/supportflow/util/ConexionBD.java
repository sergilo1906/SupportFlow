package com.supportflow.util;

//Import de Gestio D'errors
import java.io.IOException;
//Crear i revisar arxius
import java.nio.file.Files;
//Ruta arxiu 
import java.nio.file.Path;
//Conexio amb SQLLite
import java.sql.Connection;
//  JDBC
import java.sql.DriverManager;
//Errors SQL
import java.sql.SQLException;
//Executar instrucciones SQL
import java.sql.Statement;

// Abrir conexiones JDBC a un archivo SQLite local
public class ConexionBD {
    private final Path rutaBD;

    public ConexionBD() {
        this(Path.of("data", "supportflow.db"));
    }

    // Permite usar un archivo temporal independiente en las pruebas
    public ConexionBD(Path rutaBD) {
        if (rutaBD == null) {
            throw new IllegalArgumentException("La ruta de la base de datos es obligatoria.");
        }
        this.rutaBD = rutaBD.toAbsolutePath().normalize();
    }

    public Path getRutaBD() {
        return rutaBD;
    }

    // Quien llama a este metodo debe cerrar la conexion con try-with-resources
    public Connection abrirConexion() throws SQLException {
        try {
            Files.createDirectories(rutaBD.getParent());
        } catch (IOException error) {
            throw new SQLException("No se puede preparar la carpeta de SQLite: " + rutaBD, error);
        }

        Connection conexion;
        try {
            conexion = DriverManager.getConnection("jdbc:sqlite:" + rutaBD);
        } catch (SQLException error) {
            throw new SQLException("No se puede abrir la base de datos SQLite: " + rutaBD, error);
        }

        try {
            try (Statement consulta = conexion.createStatement()) {
                consulta.execute("PRAGMA foreign_keys = ON");
            }
            return conexion;
        } catch (SQLException error) {
            // Cerrar tambien si falla la preparacion de la conexion
            try {
                conexion.close();
            } catch (SQLException errorCierre) {
                error.addSuppressed(errorCierre);
            }
            throw new SQLException("No se pueden activar las claves foraneas de SQLite.", error);
        }
    }
}
