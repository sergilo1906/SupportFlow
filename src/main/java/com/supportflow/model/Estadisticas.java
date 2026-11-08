package com.supportflow.model;

// Datos de estadisticas
public class Estadisticas {

    // ATRIBUTOS
    private int totalTickets;
    private int abiertos;
    private int enProgreso;
    private int resueltos;
    private int cerrados;
    private int totalEquipos;
    private int equiposActivos;
    private int equiposBaja;

    // CONSTRUCTOR
    public Estadisticas() {
        // Los contadores empiezan en cero
    }

    // GETTERS Y SETTERS
    public int getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public int getAbiertos() {
        return abiertos;
    }

    public void setAbiertos(int abiertos) {
        this.abiertos = abiertos;
    }

    public int getEnProgreso() {
        return enProgreso;
    }

    public void setEnProgreso(int enProgreso) {
        this.enProgreso = enProgreso;
    }

    public int getResueltos() {
        return resueltos;
    }

    public void setResueltos(int resueltos) {
        this.resueltos = resueltos;
    }

    public int getCerrados() {
        return cerrados;
    }

    public void setCerrados(int cerrados) {
        this.cerrados = cerrados;
    }

    public int getTotalEquipos() {
        return totalEquipos;
    }

    public void setTotalEquipos(int totalEquipos) {
        this.totalEquipos = totalEquipos;
    }

    public int getEquiposActivos() {
        return equiposActivos;
    }

    public void setEquiposActivos(int equiposActivos) {
        this.equiposActivos = equiposActivos;
    }

    public int getEquiposBaja() {
        return equiposBaja;
    }

    public void setEquiposBaja(int equiposBaja) {
        this.equiposBaja = equiposBaja;
    }

}

