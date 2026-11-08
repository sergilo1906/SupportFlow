package com.supportflow.model;

// Datos de equipo
public class Equipo {

    // ATRIBUTOS
    private int id;
    private String nombre;
    private String tipo;
    private String marca;
    private String modelo;
    private String numeroSerie;
    private String estado;
    private Integer usuarioAsignado;

    // CONSTRUCTOR
    public Equipo(String nombre, String tipo, String marca, String modelo, String numeroSerie) {
        setNombre(nombre);
        setTipo(tipo);
        setMarca(marca);
        setModelo(modelo);
        setNumeroSerie(numeroSerie);
        this.estado = "ACTIVO";
    }

    // GETTERS Y SETTERS
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if(nombre == null || nombre.isBlank()) { throw new IllegalArgumentException("Completa nombre."); }
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        if(tipo == null || tipo.isBlank()) { throw new IllegalArgumentException("Completa tipo."); }
        this.tipo = tipo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        if(marca == null || marca.isBlank()) { throw new IllegalArgumentException("Completa marca."); }
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        if(modelo == null || modelo.isBlank()) { throw new IllegalArgumentException("Completa modelo."); }
        this.modelo = modelo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        if(numeroSerie == null || numeroSerie.isBlank()) { throw new IllegalArgumentException("Completa numeroSerie."); }
        this.numeroSerie = numeroSerie;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if(!"ACTIVO".equals(estado) && !"BAJA".equals(estado)) { throw new IllegalArgumentException("Estado de equipo incorrecto."); }
        this.estado = estado;
    }

    public Integer getUsuarioAsignado() {
        return usuarioAsignado;
    }

    public void setUsuarioAsignado(Integer usuarioAsignado) {
        this.usuarioAsignado = usuarioAsignado;
    }

    // Mostrar el resum a les llistes
    @Override
    public String toString() {
        return id + " - " + nombre + " (" + numeroSerie + ", " + estado + ")";
    }
}

