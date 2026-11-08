
package com.supportflow.dao;
import java.sql.Statement;
import java.sql.ResultSet;
import com.supportflow.model.Ticket;
import com.supportflow.util.ConexionBD;
import com.supportflow.model.TicketStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import com.supportflow.model.HistorialTicket;
import com.supportflow.model.TicketPriority;

// Clase para guardar tickets en la base de datos
public class TicketDAO {

    // ATRIBUTOS
    private ConexionBD baseDatos;

    // CONSTRUCTOR
    public TicketDAO() {
        this.baseDatos = new ConexionBD();
    }

    // Constructor per provar amb una BD temporal
    public TicketDAO(ConexionBD baseDatos) {
        this.baseDatos = baseDatos;
    }

    // METODOS

    // Obtenir objectes per mostrar-los a JavaFX
    public ArrayList<Ticket> obtenerTickets() throws SQLException {
        ArrayList<Ticket> tickets = new ArrayList<Ticket>();
        try(Connection conexion = baseDatos.abrirConexion();
            Statement consulta = conexion.createStatement();
            ResultSet resultado = consulta.executeQuery("SELECT * FROM tickets ORDER BY id DESC")) {
            while(resultado.next()) { tickets.add(leerTicket(resultado)); }
        }
        return tickets;
    }

    public Ticket obtenerPorId(int id) throws SQLException {
        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM tickets WHERE id=?")) {
            consulta.setInt(1, id);
            try(ResultSet resultado = consulta.executeQuery()) {
                if(resultado.next()) { return leerTicket(resultado); }
                return null;
            }
        }
    }

    public void guardarTicketCompleto(Ticket ticket, int autorId) throws SQLException {
        String sql = "INSERT INTO tickets(titulo,descripcion,prioridad,estado,fecha_creacion,usuario_creador,tecnico_asignado,equipo_id) VALUES(?,?,?,?,?,?,?,?)";
        try(Connection conexion = baseDatos.abrirConexion()) {
            conexion.setAutoCommit(false);
            try {
                try(PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    consulta.setString(1, ticket.getTitulo());
                    consulta.setString(2, ticket.getDescripcion());
                    consulta.setString(3, ticket.getPrioridad().name());
                    consulta.setString(4, ticket.getEstado().name());
                    consulta.setString(5, ticket.getFechaCreacion().toString());
                    consulta.setInt(6, autorId);
                    consulta.setObject(7, ticket.getTecnicoAsignado());
                    consulta.setObject(8, ticket.getEquipoId());
                    consulta.executeUpdate();
                    try(ResultSet resultado = consulta.getGeneratedKeys()) {
                        if(resultado.next()) { ticket.setId(resultado.getLong(1)); }
                    }
                }
                HistorialTicket registro = new HistorialTicket(
                        Math.toIntExact(ticket.getId()), autorId, "Creacion de la incidencia");
                new HistorialTicketDAO(baseDatos).guardarHistorial(conexion, registro);
                conexion.commit();
            } catch(SQLException error) {
                conexion.rollback();
                ticket.setId(0);
                throw error;
            }
        }
    }

    // El canvi i l'historial es guarden junts
    public void cambiarEstado(int id, TicketStatus nuevoEstado, int autorId) throws SQLException {
        try(Connection conexion = baseDatos.abrirConexion()) {
            conexion.setAutoCommit(false);
            try {
                String estadoAnterior;
                try(PreparedStatement consulta = conexion.prepareStatement("SELECT estado FROM tickets WHERE id=?")) {
                    consulta.setInt(1, id);
                    try(ResultSet resultado = consulta.executeQuery()) {
                        if(!resultado.next()) { throw new SQLException("No existe el ticket."); }
                        estadoAnterior = resultado.getString(1);
                    }
                }
                try(PreparedStatement consulta = conexion.prepareStatement("UPDATE tickets SET estado=? WHERE id=?")) {
                    consulta.setString(1, nuevoEstado.name());
                    consulta.setInt(2, id);
                    consulta.executeUpdate();
                }
                new HistorialTicketDAO(baseDatos).guardarHistorial(conexion,
                        new HistorialTicket(id, autorId,
                                "Cambio de estado de " + estadoAnterior + " a " + nuevoEstado.name()));
                conexion.commit();
            } catch(SQLException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    public void asignarTecnico(int id, Integer tecnicoId, int autorId) throws SQLException {
        try(Connection conexion = baseDatos.abrirConexion()) {
            conexion.setAutoCommit(false);
            try {
                Integer anterior;
                try(PreparedStatement consulta = conexion.prepareStatement("SELECT tecnico_asignado FROM tickets WHERE id=?")) {
                    consulta.setInt(1, id);
                    try(ResultSet resultado = consulta.executeQuery()) {
                        if(!resultado.next()) { throw new SQLException("No existe el ticket."); }
                        anterior = leerIdOpcional(resultado, "tecnico_asignado");
                    }
                }
                try(PreparedStatement consulta = conexion.prepareStatement("UPDATE tickets SET tecnico_asignado=? WHERE id=?")) {
                    consulta.setObject(1, tecnicoId);
                    consulta.setInt(2, id);
                    consulta.executeUpdate();
                }
                new HistorialTicketDAO(baseDatos).guardarHistorial(conexion,
                        new HistorialTicket(id, autorId,
                                "Asignacion de tecnico: " + anterior + " -> " + tecnicoId));
                conexion.commit();
            } catch(SQLException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    private Ticket leerTicket(ResultSet resultado) throws SQLException {
        Ticket ticket = new Ticket(resultado.getLong("id"), resultado.getString("titulo"), resultado.getString("descripcion"),
                TicketPriority.valueOf(resultado.getString("prioridad")),
                TicketStatus.valueOf(resultado.getString("estado")),
                LocalDateTime.parse(resultado.getString("fecha_creacion")));
        ticket.setUsuarioCreador(leerIdOpcional(resultado, "usuario_creador"));
        ticket.setTecnicoAsignado(leerIdOpcional(resultado, "tecnico_asignado"));
        ticket.setEquipoId(leerIdOpcional(resultado, "equipo_id"));
        return ticket;
    }

    private Integer leerIdOpcional(ResultSet resultado, String columna) throws SQLException {
        int id = resultado.getInt(columna);
        if(resultado.wasNull()) { return null; }
        return id;
    }

    // Guardar una incidencia en SQLite
    public void guardarTicket(Ticket ticket) throws SQLException {

        // Instruccion para guardar los datos
        String sql = "INSERT INTO tickets " +
        "(titulo, descripcion, prioridad, estado, fecha_creacion) " +
        "VALUES (?, ?, ?, ?, ?)";
        // Abrir la base de datos
        try(Connection conexion = baseDatos.abrirConexion()) {

            PreparedStatement consulta = conexion.prepareStatement(sql);

            // Introducir los datos del ticket
            consulta.setString(1, ticket.getTitulo());
            consulta.setString(2, ticket.getDescripcion());
            consulta.setString(3, ticket.getPrioridad().name());
            consulta.setString(4, ticket.getEstado().name());
            consulta.setString(5, ticket.getFechaCreacion().toString());

            // Guardar el ticket
            consulta.executeUpdate();

            // Cerrar la consulta
            consulta.close();
        }
    }
    
    // Mostrar tiquets guardats de SQL
    public void mostrarTickets() throws SQLException {

        // Consulta Tickets
        String sql = "SELECT id, titulo FROM tickets";

        // Abrir la BD y preparar la consulta
        try(Connection conexion = baseDatos.abrirConexion();
            Statement consulta = conexion.createStatement();
            ResultSet resultado = consulta.executeQuery(sql)) {

            // Recorre tots els tickets i mostra el id i el títol
            while(resultado.next()) {

                int id = resultado.getInt("id");
                String titulo = resultado.getString("titulo");

                System.out.println(id + " - " + titulo);
            }
        }
    }
    
    // Canviar l'estat d'un ticket
    public void cambiarEstado(int id, TicketStatus nuevoEstado) throws SQLException {

        // Consulta SQL per modificar l'estat
        String sql = "UPDATE tickets SET estado = ? WHERE id = ?";

        // Obrir la BD
        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement(sql)) {

            // Posar les dades a la consulta
            consulta.setString(1, nuevoEstado.name());
            consulta.setInt(2, id);

            // Executar la modificacio
            int filasModificadas = consulta.executeUpdate();

            // Comprovar si s'ha modificat el ticket
            if(filasModificadas > 0) {
                System.out.println("Estado del ticket modificado correctamente");
            } else {
                System.out.println("No existe un ticket con este ID");
            }
        }
    }

    // Eliminar un ticket de la BD
    public void eliminarTicket(int id) throws SQLException {

        // Consulta SQL per eliminar un ticket
        String sql = "DELETE FROM tickets WHERE id = ?";

        // Obrir la BD
        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement(sql)) {

            // Indicar quin ticket volem eliminar
            consulta.setInt(1, id);

            // Executar la consulta
            int filasEliminadas = consulta.executeUpdate();

            // Comprovar si s'ha eliminat
            if(filasEliminadas > 0) {
                System.out.println("Ticket eliminado correctamente");
            } else {
                System.out.println("No existe un ticket con este ID");
            }
        }
    }


}

    
