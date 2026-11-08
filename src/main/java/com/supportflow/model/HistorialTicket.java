package com.supportflow.model;

import java.time.LocalDateTime;

// Datos de historialticket
public class HistorialTicket {

    // ATRIBUTOS
    private int id;
    private int ticketId;
    private int usuarioId;
    private String accion;
    private LocalDateTime fecha;

    // CONSTRUCTOR
    public HistorialTicket(int ticketId, int usuarioId, String accion) {
        this.ticketId = ticketId;
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.fecha = LocalDateTime.now();
    }

    // GETTERS Y SETTERS
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

}

