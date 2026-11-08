package com.supportflow.service;

import com.supportflow.dao.EstadisticasDAO;
import com.supportflow.model.Estadisticas;
import com.supportflow.util.ConexionBD;
import java.sql.SQLException;

// Resum reservat a administracio
public class EstadisticasService {
    private ConexionBD baseDatos;
    private Sesion sesion;

    public EstadisticasService(ConexionBD baseDatos, Sesion sesion) {
        this.baseDatos = baseDatos;
        this.sesion = sesion;
    }

    public Estadisticas obtenerEstadisticas() throws SQLException {
        Permisos.exigirAdmin(Permisos.usuarioActual(baseDatos, sesion));
        return new EstadisticasDAO(baseDatos).obtenerEstadisticas();
    }
}

