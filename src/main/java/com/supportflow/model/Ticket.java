package com.supportflow.model;

import java.time.LocalDateTime;

// Clase que representa una incidencia informatica
public class Ticket {
    private long id;
    private String titulo;
    private String descripcion;
    private TicketPriority prioridad;
    private TicketStatus estado;
    private LocalDateTime fechaCreacion;
    private Integer usuarioCreador;
    private Integer tecnicoAsignado;
    private Integer equipoId;

    public Ticket(String titulo, String descripcion) {
        this(titulo, descripcion, TicketPriority.MEDIUM);
    }

    // CONSTRUCTOR: Crear una incidencia nueva ////////////////
    public Ticket(String titulo, String descripcion, TicketPriority prioridad) {
        this.id = 0;
        setTitulo(titulo);
        setDescripcion(descripcion);
        setPrioridad(prioridad);
        this.estado = TicketStatus.OPEN;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Reconstruir una incidencia leida de la base de datos
    public Ticket(long id, String titulo, String descripcion, TicketPriority prioridad,
            TicketStatus estado, LocalDateTime fechaCreacion) {
        if (id <= 0) {
            throw new IllegalArgumentException("Un ticket guardado debe tener un id positivo.");
        }
        setId(id);
        setTitulo(titulo);
        setDescripcion(descripcion);
        setPrioridad(prioridad);
        setEstado(estado);
        setFechaCreacion(fechaCreacion);
    }

    public long getId() {
        return id;
    }

    // El futuro DAO asignara el id que devuelva SQLite al insertar
    public void setId(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("El id no puede ser negativo.");
        }
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El titulo es obligatorio.");
        }
        if (titulo.trim().length() > 150) {
            throw new IllegalArgumentException("El titulo no puede superar 150 caracteres.");
        }
        this.titulo = titulo.trim();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripcion es obligatoria.");
        }
        this.descripcion = descripcion.trim();
    }

    public TicketPriority getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(TicketPriority prioridad) {
        if (prioridad == null) {
            this.prioridad = TicketPriority.MEDIUM;
        } else {
            this.prioridad = prioridad;
        }
    }

    public TicketStatus getEstado() {
        return estado;
    }

    public void setEstado(TicketStatus estado) {
        if (estado == null) {
            this.estado = TicketStatus.OPEN;
        } else {
            this.estado = estado;
        }
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        if (fechaCreacion == null) {
            throw new IllegalArgumentException("La fecha de creacion es obligatoria.");
        }
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getUsuarioCreador() {
        return usuarioCreador;
    }

    public void setUsuarioCreador(Integer usuarioCreador) {
        this.usuarioCreador = usuarioCreador;
    }

    public Integer getTecnicoAsignado() {
        return tecnicoAsignado;
    }

    public void setTecnicoAsignado(Integer tecnicoAsignado) {
        this.tecnicoAsignado = tecnicoAsignado;
    }

    public Integer getEquipoId() {
        return equipoId;
    }

    public void setEquipoId(Integer equipoId) {
        this.equipoId = equipoId;
    }

    @Override
    public String toString() {
        return "Ticket [id=" + id + ", titulo=" + titulo + ", prioridad=" + prioridad
                + ", estado=" + estado + ", fechaCreacion=" + fechaCreacion + "]";
    }
}
