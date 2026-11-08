package com.supportflow.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Crear les taules i conservar les incidencies antigues
public class EsquemaBD {
    public static void inicializar(ConexionBD baseDatos) throws SQLException {
        try(Connection conexion = baseDatos.abrirConexion()) {
            boolean tablaAntigua = existeTabla(conexion, "tickets");
            boolean necesitaCambio = tablaAntigua && (!existeColumna(conexion, "usuario_creador") ||
                    !existeColumna(conexion, "tecnico_asignado") || !existeColumna(conexion, "equipo_id"));
            if(necesitaCambio) {
                // Copia SQLite coherent abans de canviar l'esquema
                Path carpeta = baseDatos.getRutaBD().getParent().resolve("backups");
                try {
                    Files.createDirectories(carpeta);
                } catch(IOException error) {
                    throw new SQLException("No se puede crear el respaldo de SQLite.", error);
                }
                String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
                Path copia = carpeta.resolve(baseDatos.getRutaBD().getFileName() + ".antes-v2-" + fecha + ".db");
                try(PreparedStatement consulta = conexion.prepareStatement("VACUUM INTO ?")) {
                    consulta.setString(1, copia.toString());
                    consulta.execute();
                }
            }
            conexion.setAutoCommit(false);
            try {
                ejecutarScript(conexion, "/db/crear_tickets.sql");
                ejecutarScript(conexion, "/db/crear_aplicacion.sql");
                if(!existeColumna(conexion, "usuario_creador")) {
                    ejecutar(conexion, "ALTER TABLE tickets ADD COLUMN usuario_creador INTEGER REFERENCES usuarios(id) ON DELETE RESTRICT");
                }
                if(!existeColumna(conexion, "tecnico_asignado")) {
                    ejecutar(conexion, "ALTER TABLE tickets ADD COLUMN tecnico_asignado INTEGER REFERENCES usuarios(id) ON DELETE SET NULL");
                }
                if(!existeColumna(conexion, "equipo_id")) {
                    ejecutar(conexion, "ALTER TABLE tickets ADD COLUMN equipo_id INTEGER REFERENCES equipos(id) ON DELETE RESTRICT");
                }
                conexion.commit();
            } catch(SQLException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    private static void ejecutarScript(Connection conexion, String ruta) throws SQLException {
        try(InputStream archivo = EsquemaBD.class.getResourceAsStream(ruta)) {
            if(archivo == null) {
                throw new SQLException("No se encuentra el SQL: " + ruta);
            }
            String texto = new String(archivo.readAllBytes(), StandardCharsets.UTF_8);
            String[] instrucciones = texto.split(";");
            for(int i = 0; i < instrucciones.length; i++) {
                if(!instrucciones[i].isBlank()) {
                    ejecutar(conexion, instrucciones[i]);
                }
            }
        } catch(IOException error) {
            throw new SQLException("No se puede leer el esquema SQL.", error);
        }
    }

    private static void ejecutar(Connection conexion, String sql) throws SQLException {
        try(Statement consulta = conexion.createStatement()) {
            consulta.execute(sql);
        }
    }

    private static boolean existeTabla(Connection conexion, String nombre) throws SQLException {
        try(PreparedStatement consulta = conexion.prepareStatement("SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?")) {
            consulta.setString(1, nombre);
            try(ResultSet resultado = consulta.executeQuery()) {
                return resultado.next();
            }
        }
    }

    private static boolean existeColumna(Connection conexion, String nombre) throws SQLException {
        try(Statement consulta = conexion.createStatement();
            ResultSet resultado = consulta.executeQuery("PRAGMA table_info(tickets)")) {
            while(resultado.next()) {
                if(nombre.equals(resultado.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }
}

