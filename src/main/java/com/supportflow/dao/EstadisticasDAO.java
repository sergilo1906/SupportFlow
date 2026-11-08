package com.supportflow.dao;

import com.supportflow.model.Estadisticas;
import com.supportflow.util.ConexionBD;
import java.sql.*;

// Comptar els registres reals de SQLite
public class EstadisticasDAO {
    private ConexionBD baseDatos;

    public EstadisticasDAO(ConexionBD baseDatos) {
        this.baseDatos = baseDatos;
    }

    public Estadisticas obtenerEstadisticas() throws SQLException {
        Estadisticas datos = new Estadisticas();
        try(Connection conexion = baseDatos.abrirConexion()) {
            // La mateixa lectura per a tots els comptadors
            conexion.setAutoCommit(false);
            datos.setTotalTickets(contar(conexion, "SELECT COUNT(*) FROM tickets"));
            datos.setAbiertos(contar(conexion, "SELECT COUNT(*) FROM tickets WHERE estado='OPEN'"));
            datos.setEnProgreso(contar(conexion, "SELECT COUNT(*) FROM tickets WHERE estado='IN_PROGRESS'"));
            datos.setResueltos(contar(conexion, "SELECT COUNT(*) FROM tickets WHERE estado='RESOLVED'"));
            datos.setCerrados(contar(conexion, "SELECT COUNT(*) FROM tickets WHERE estado='CLOSED'"));
            datos.setTotalEquipos(contar(conexion, "SELECT COUNT(*) FROM equipos"));
            datos.setEquiposActivos(contar(conexion, "SELECT COUNT(*) FROM equipos WHERE estado='ACTIVO'"));
            datos.setEquiposBaja(contar(conexion, "SELECT COUNT(*) FROM equipos WHERE estado='BAJA'"));
            conexion.commit();
        }
        return datos;
    }

    private int contar(Connection conexion, String sql) throws SQLException {
        try(Statement consulta = conexion.createStatement(); ResultSet resultado = consulta.executeQuery(sql)) {
            resultado.next();
            return resultado.getInt(1);
        }
    }
}

