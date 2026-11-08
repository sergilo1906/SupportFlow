package com.supportflow.dao;

import com.supportflow.model.HistorialTicket;
import com.supportflow.util.ConexionBD;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

// Historial dins de la mateixa transaccio del ticket
public class HistorialTicketDAO {
    private ConexionBD baseDatos;

    public HistorialTicketDAO(ConexionBD baseDatos) {
        this.baseDatos = baseDatos;
    }

    public void guardarHistorial(Connection conexion, HistorialTicket historial) throws SQLException {
        String sql = "INSERT INTO historial_tickets(ticket_id,usuario_id,accion,fecha) VALUES(?,?,?,?)";
        try(PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, historial.getTicketId());
            consulta.setInt(2, historial.getUsuarioId());
            consulta.setString(3, historial.getAccion());
            consulta.setString(4, historial.getFecha().toString());
            consulta.executeUpdate();
        }
    }

    public ArrayList<HistorialTicket> obtenerHistorial(int ticketId) throws SQLException {
        ArrayList<HistorialTicket> registros = new ArrayList<HistorialTicket>();
        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM historial_tickets WHERE ticket_id=? ORDER BY id")) {
            consulta.setInt(1, ticketId);
            try(ResultSet resultado = consulta.executeQuery()) {
                while(resultado.next()) {
                    HistorialTicket registro = new HistorialTicket(ticketId, resultado.getInt("usuario_id"), resultado.getString("accion"));
                    registro.setId(resultado.getInt("id"));
                    registro.setFecha(LocalDateTime.parse(resultado.getString("fecha")));
                    registros.add(registro);
                }
            }
        }
        return registros;
    }
}

