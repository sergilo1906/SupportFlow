package com.supportflow.util;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.sql.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// Provar la migracio sense tocar la BD de desenvolupament
class EsquemaBDTest {
    @TempDir
    Path carpetaTemporal;

    @Test
    void migraDosVecesSinPerderElTicketAntiguo() throws Exception {
        ConexionBD baseDatos = new ConexionBD(carpetaTemporal.resolve("antigua.db"));
        try(Connection conexion = baseDatos.abrirConexion(); Statement consulta = conexion.createStatement()) {
            consulta.execute(Files.readString(Path.of("src/main/resources/db/crear_tickets.sql")));
            consulta.execute("INSERT INTO tickets VALUES(89,'Original','Descripcion original','CRITICAL','CLOSED','2026-10-01T09:15')");
        }
        EsquemaBD.inicializar(baseDatos);
        EsquemaBD.inicializar(baseDatos);
        try(Connection conexion = baseDatos.abrirConexion(); Statement consulta = conexion.createStatement();
            ResultSet resultado = consulta.executeQuery("SELECT * FROM tickets")) {
            assertTrue(resultado.next());
            assertEquals(89, resultado.getInt("id"));
            assertEquals("Original", resultado.getString("titulo"));
            assertEquals("Descripcion original", resultado.getString("descripcion"));
            assertEquals("CRITICAL", resultado.getString("prioridad"));
            assertEquals("CLOSED", resultado.getString("estado"));
            assertEquals("2026-10-01T09:15", resultado.getString("fecha_creacion"));
            assertNull(resultado.getObject("usuario_creador"));
            assertNull(resultado.getObject("tecnico_asignado"));
            assertNull(resultado.getObject("equipo_id"));
            assertFalse(resultado.next());
        }
        int cantidad = 0;
        try(DirectoryStream<Path> archivos = Files.newDirectoryStream(carpetaTemporal.resolve("backups"))) {
            for(Path archivo : archivos) { cantidad++; }
        }
        assertEquals(1, cantidad);
    }

    @Test
    void lasRelacionesNoEliminanTicketsAlBorrarUnEquipo() throws Exception {
        ConexionBD baseDatos = new ConexionBD(carpetaTemporal.resolve("relaciones.db"));
        EsquemaBD.inicializar(baseDatos);
        try(Connection conexion = baseDatos.abrirConexion(); Statement consulta = conexion.createStatement()) {
            consulta.execute("INSERT INTO equipos VALUES(1,'PC','PC','Marca','Modelo','SERIE','ACTIVO',NULL)");
            consulta.execute("INSERT INTO tickets(titulo,descripcion,prioridad,estado,fecha_creacion,equipo_id) VALUES('Prueba','Descripcion','LOW','OPEN','2026-11-07T10:00',1)");
            assertThrows(SQLException.class, () -> consulta.executeUpdate("DELETE FROM equipos WHERE id=1"));
            try(ResultSet resultado = consulta.executeQuery("SELECT COUNT(*) FROM tickets")) {
                resultado.next();
                assertEquals(1, resultado.getInt(1));
            }
        }
    }
}

