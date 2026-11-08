package com.supportflow.util;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConexionBDTest {
    @TempDir
    Path carpetaTemporal;

    @Test
    void consultaSQLiteTemporalYCierraTodosLosRecursos() throws Exception {
        Path archivo = carpetaTemporal.resolve("datos con espacios").resolve("prueba.db");
        ConexionBD baseDatos = new ConexionBD(archivo);
        Connection conexion;
        PreparedStatement consulta;
        ResultSet resultado;

        try (Connection abierta = baseDatos.abrirConexion()) {
            conexion = abierta;
            try (PreparedStatement preparada = conexion.prepareStatement("SELECT sqlite_version(), ?")) {
                consulta = preparada;
                consulta.setString(1, "texto ' con comillas");
                try (ResultSet filas = consulta.executeQuery()) {
                    resultado = filas;
                    assertTrue(resultado.next());
                    assertTrue(resultado.getString(1).matches("\\d+\\.\\d+\\.\\d+"));
                    assertEquals("texto ' con comillas", resultado.getString(2));
                    assertFalse(resultado.next());
                }
                assertTrue(resultado.isClosed());
            }
            assertTrue(consulta.isClosed());
            assertFalse(conexion.isClosed());
        }

        assertTrue(conexion.isClosed());
        assertTrue(Files.isRegularFile(archivo));
        Files.delete(archivo);
        assertFalse(Files.exists(archivo));
    }

    @Test
    void activaYAplicaClavesForaneasEnCadaConexion() throws Exception {
        ConexionBD baseDatos = new ConexionBD(carpetaTemporal.resolve("claves.db"));
        // Estas tablas solo existen en el archivo temporal de esta prueba
        try (Connection conexion = baseDatos.abrirConexion(); Statement consulta = conexion.createStatement()) {
            consulta.execute("CREATE TABLE padre (id INTEGER PRIMARY KEY)");
            consulta.execute("CREATE TABLE hijo (padre_id INTEGER REFERENCES padre(id))");
            assertThrows(SQLException.class, () -> consulta.executeUpdate("INSERT INTO hijo VALUES (99)"));
        }

        try (Connection conexion = baseDatos.abrirConexion(); Statement consulta = conexion.createStatement();
                ResultSet resultado = consulta.executeQuery("PRAGMA foreign_keys")) {
            assertTrue(resultado.next());
            assertEquals(1, resultado.getInt(1));
        }
    }

    @Test
    void informaCuandoLaCarpetaDeSQLiteNoEsValida() throws Exception {
        Path archivoNormal = carpetaTemporal.resolve("archivo.txt");
        Files.writeString(archivoNormal, "Esto no es una carpeta");
        ConexionBD baseDatos = new ConexionBD(archivoNormal.resolve("prueba.db"));
        SQLException error = assertThrows(SQLException.class, () -> baseDatos.abrirConexion());
        assertTrue(error.getMessage().contains("carpeta de SQLite"));
        assertNotNull(error.getCause());
    }
}
