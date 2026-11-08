package com.supportflow.model;

import java.time.LocalDateTime;

// Datos de usuario
public class Usuario {

    // ATRIBUTOS
    private int id;
    private String firebaseUid;
    private String nombre;
    private String email;
    private UsuarioRol rol;
    private LocalDateTime fechaRegistro;

    // CONSTRUCTOR
    public Usuario(String firebaseUid, String nombre, String email, UsuarioRol rol) {
        setFirebaseUid(firebaseUid);
        setNombre(nombre);
        setEmail(email);
        setRol(rol);
        this.fechaRegistro = LocalDateTime.now();
    }

    // GETTERS Y SETTERS
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        if(firebaseUid == null || firebaseUid.isBlank()) { throw new IllegalArgumentException("El UID es obligatorio."); }
        this.firebaseUid = firebaseUid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if(nombre == null || nombre.isBlank()) { throw new IllegalArgumentException("El nombre es obligatorio."); }
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if(email == null || email.isBlank()) { throw new IllegalArgumentException("El email es obligatorio."); }
        this.email = email;
    }

    public UsuarioRol getRol() {
        return rol;
    }

    public void setRol(UsuarioRol rol) {
        if(rol == null) { throw new IllegalArgumentException("El rol es obligatorio."); }
        this.rol = rol;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    // Mostrar el resum a les llistes
    @Override
    public String toString() {
        return id + " - " + nombre + " (" + rol + ")";
    }
}

