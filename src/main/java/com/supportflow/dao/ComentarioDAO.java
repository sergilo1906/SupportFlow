package com.supportflow.dao;

import com.supportflow.model.Comentario;
import com.supportflow.util.ConexionBD;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

// Comentaris de cada incidencia
public class ComentarioDAO {
    private ConexionBD baseDatos;

    public ComentarioDAO(ConexionBD baseDatos) {
        this.baseDatos = baseDatos;
    }

    public void guardarComentario(Comentario comentario) throws SQLException {
        String sql = "INSERT INTO comentarios(ticket_id,usuario_id,texto,fecha) VALUES(?,?,?,?)";
        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, comentario.getTicketId());
            consulta.setInt(2, comentario.getUsuarioId());
            consulta.setString(3, comentario.getTexto());
            consulta.setString(4, comentario.getFecha().toString());
            consulta.executeUpdate();
        }
    }

    public ArrayList<Comentario> obtenerComentarios(int ticketId) throws SQLException {
        ArrayList<Comentario> comentarios = new ArrayList<Comentario>();
        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM comentarios WHERE ticket_id=? ORDER BY id")) {
            consulta.setInt(1, ticketId);
            try(ResultSet resultado = consulta.executeQuery()) {
                while(resultado.next()) {
                    Comentario comentario = new Comentario(ticketId, resultado.getInt("usuario_id"), resultado.getString("texto"));
                    comentario.setId(resultado.getInt("id"));
                    comentario.setFecha(LocalDateTime.parse(resultado.getString("fecha")));
                    comentarios.add(comentario);
                }
            }
        }
        return comentarios;
    }
}

