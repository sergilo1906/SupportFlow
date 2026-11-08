package com.supportflow.service;

import com.supportflow.dao.*;
import com.supportflow.model.*;
import com.supportflow.util.ConexionBD;
import java.sql.SQLException;
import java.util.ArrayList;

// Regles de les incidencies i els seus permisos
public class TicketService {
    private ConexionBD baseDatos;
    private Sesion sesion;
    private TicketDAO ticketDAO;

    public TicketService(ConexionBD baseDatos, Sesion sesion) {
        this.baseDatos = baseDatos;
        this.sesion = sesion;
        this.ticketDAO = new TicketDAO(baseDatos);
    }

    public ArrayList<Ticket> obtenerTickets(TicketStatus estado, TicketPriority prioridad) throws SQLException {
        Usuario usuario = Permisos.usuarioActual(baseDatos, sesion);
        ArrayList<Ticket> todos = ticketDAO.obtenerTickets();
        ArrayList<Ticket> visibles = new ArrayList<Ticket>();
        for(int i = 0; i < todos.size(); i++) {
            Ticket ticket = todos.get(i);
            if(puedeConsultar(usuario, ticket)) {
                if((estado == null || ticket.getEstado() == estado) &&
                        (prioridad == null || ticket.getPrioridad() == prioridad)) {
                    visibles.add(ticket);
                }
            }
        }
        return visibles;
    }

    public Ticket consultarTicket(int id) throws SQLException {
        Usuario usuario = Permisos.usuarioActual(baseDatos, sesion);
        Ticket ticket = ticketDAO.obtenerPorId(id);
        if(ticket == null) { throw new IllegalArgumentException("No existe el ticket."); }
        if(!puedeConsultar(usuario, ticket)) { throw new SecurityException("No puedes consultar este ticket."); }
        return ticket;
    }

    public void crearTicket(String titulo, String descripcion, TicketPriority prioridad, Integer equipoId) throws SQLException {
        Usuario usuario = Permisos.usuarioActual(baseDatos, sesion);
        Ticket ticket = new Ticket(titulo, descripcion, prioridad);
        if(equipoId != null) {
            Equipo equipo = new EquipoDAO(baseDatos).obtenerPorId(equipoId);
            if(equipo == null || !"ACTIVO".equals(equipo.getEstado())) {
                throw new IllegalArgumentException("El equipo debe existir y estar activo.");
            }
            if(usuario.getRol() == UsuarioRol.EMPLEADO &&
                    !Integer.valueOf(usuario.getId()).equals(equipo.getUsuarioAsignado())) {
                throw new SecurityException("Solo puedes relacionar uno de tus equipos.");
            }
        }
        ticket.setUsuarioCreador(usuario.getId());
        ticket.setEquipoId(equipoId);
        ticketDAO.guardarTicketCompleto(ticket, usuario.getId());
    }

    public void cambiarEstado(int id, TicketStatus nuevoEstado) throws SQLException {
        Usuario usuario = Permisos.usuarioActual(baseDatos, sesion);
        Ticket ticket = consultarTicket(id);
        if(usuario.getRol() == UsuarioRol.EMPLEADO ||
                (usuario.getRol() == UsuarioRol.TECNICO && !Integer.valueOf(usuario.getId()).equals(ticket.getTecnicoAsignado()))) {
            throw new SecurityException("Solo el tecnico asignado o ADMIN puede cambiar el estado.");
        }
        if(nuevoEstado == null) { throw new IllegalArgumentException("Selecciona un estado."); }
        boolean valido = false;
        if(ticket.getEstado() == TicketStatus.OPEN && nuevoEstado == TicketStatus.IN_PROGRESS) { valido = true; }
        if(ticket.getEstado() == TicketStatus.IN_PROGRESS && nuevoEstado == TicketStatus.RESOLVED) { valido = true; }
        if(ticket.getEstado() == TicketStatus.RESOLVED && nuevoEstado == TicketStatus.CLOSED) { valido = true; }
        if(usuario.getRol() == UsuarioRol.ADMIN && nuevoEstado == TicketStatus.OPEN &&
                (ticket.getEstado() == TicketStatus.RESOLVED || ticket.getEstado() == TicketStatus.CLOSED)) { valido = true; }
        if(!valido) { throw new IllegalArgumentException("Cambio no permitido: OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED. ADMIN puede reabrir resueltos o cerrados."); }
        ticketDAO.cambiarEstado(id, nuevoEstado, usuario.getId());
    }

    public void asignarTecnico(int id, Integer tecnicoId) throws SQLException {
        Usuario usuario = Permisos.usuarioActual(baseDatos, sesion);
        Permisos.exigirAdmin(usuario);
        consultarTicket(id);
        if(tecnicoId != null) {
            Usuario tecnico = new UsuarioDAO(baseDatos).obtenerPorId(tecnicoId);
            if(tecnico == null || tecnico.getRol() != UsuarioRol.TECNICO) {
                throw new IllegalArgumentException("Selecciona un usuario con rol TECNICO.");
            }
        }
        ticketDAO.asignarTecnico(id, tecnicoId, usuario.getId());
    }

    public void eliminarTicket(int id) throws SQLException {
        Permisos.exigirAdmin(Permisos.usuarioActual(baseDatos, sesion));
        consultarTicket(id);
        ticketDAO.eliminarTicket(id);
    }

    public void anadirComentario(int id, String texto) throws SQLException {
        Usuario usuario = Permisos.usuarioActual(baseDatos, sesion);
        consultarTicket(id);
        new ComentarioDAO(baseDatos).guardarComentario(new Comentario(id, usuario.getId(), texto));
    }

    public ArrayList<Comentario> obtenerComentarios(int id) throws SQLException {
        consultarTicket(id);
        return new ComentarioDAO(baseDatos).obtenerComentarios(id);
    }

    public ArrayList<HistorialTicket> obtenerHistorial(int id) throws SQLException {
        consultarTicket(id);
        return new HistorialTicketDAO(baseDatos).obtenerHistorial(id);
    }

    public String nombreUsuario(Integer id) throws SQLException {
        Permisos.usuarioActual(baseDatos, sesion);
        if(id == null) { return "Sin asignar"; }
        Usuario usuario = new UsuarioDAO(baseDatos).obtenerPorId(id);
        if(usuario == null) { return "Usuario no disponible"; }
        return usuario.getNombre();
    }

    private boolean puedeConsultar(Usuario usuario, Ticket ticket) {
        if(usuario.getRol() == UsuarioRol.ADMIN) { return true; }
        if(Integer.valueOf(usuario.getId()).equals(ticket.getUsuarioCreador())) { return true; }
        if(usuario.getRol() == UsuarioRol.TECNICO && Integer.valueOf(usuario.getId()).equals(ticket.getTecnicoAsignado())) { return true; }
        return false;
    }
}

