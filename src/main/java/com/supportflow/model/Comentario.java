package com.supportflow.model;

import java.time.LocalDateTime;

// Datos de comentario
public class Comentario {

    // ATRIBUTOS
    private int id;
    private int ticketId;
    private int usuarioId;
    private String texto;
    private LocalDateTime fecha;

    // CONSTRUCTOR
    public Comentario(int ticketId, int usuarioId, String texto) {
        this.ticketId = ticketId;
        this.usuarioId = usuarioId;
        setTexto(texto);
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

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        if(texto == null || texto.isBlank()) { throw new IllegalArgumentException("El comentario no puede estar vacio."); }
        this.texto = texto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

}

