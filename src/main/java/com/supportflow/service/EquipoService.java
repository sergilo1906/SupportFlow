package com.supportflow.service;

import com.supportflow.dao.EquipoDAO;
import com.supportflow.dao.UsuarioDAO;
import com.supportflow.model.Equipo;
import com.supportflow.model.Usuario;
import com.supportflow.model.UsuarioRol;
import com.supportflow.util.ConexionBD;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

// Gestionar equips sense perdre les incidencies historiques
public class EquipoService {
    private ConexionBD baseDatos;
    private Sesion sesion;
    private EquipoDAO equipoDAO;

    public EquipoService(ConexionBD baseDatos, Sesion sesion) {
        this.baseDatos = baseDatos;
        this.sesion = sesion;
        this.equipoDAO = new EquipoDAO(baseDatos);
    }

    public ArrayList<Equipo> obtenerEquipos() throws SQLException {
        Usuario usuario = Permisos.usuarioActual(baseDatos, sesion);
        ArrayList<Equipo> todos = equipoDAO.obtenerEquipos();
        ArrayList<Equipo> visibles = new ArrayList<Equipo>();
        for(int i = 0; i < todos.size(); i++) {
            Equipo equipo = todos.get(i);
            if(usuario.getRol() != UsuarioRol.EMPLEADO || Integer.valueOf(usuario.getId()).equals(equipo.getUsuarioAsignado())) {
                visibles.add(equipo);
            }
        }
        return visibles;
    }

    public void guardarEquipo(Equipo equipo) throws SQLException {
        Permisos.exigirAdmin(Permisos.usuarioActual(baseDatos, sesion));
        validarUsuario(equipo.getUsuarioAsignado());
        equipoDAO.guardarEquipo(equipo);
    }

    public void modificarEquipo(Equipo equipo) throws SQLException {
        Usuario usuario = Permisos.usuarioActual(baseDatos, sesion);
        if(usuario.getRol() == UsuarioRol.EMPLEADO) { throw new SecurityException("No puedes modificar inventario."); }
        Equipo anterior = equipoDAO.obtenerPorId(equipo.getId());
        if(anterior == null) { throw new IllegalArgumentException("No existe el equipo."); }
        if(usuario.getRol() == UsuarioRol.TECNICO &&
                (!Objects.equals(anterior.getUsuarioAsignado(), equipo.getUsuarioAsignado()) ||
                        !anterior.getEstado().equals(equipo.getEstado()))) {
            throw new SecurityException("Solo ADMIN puede asignar equipos, darlos de baja o reactivarlos.");
        }
        validarUsuario(equipo.getUsuarioAsignado());
        equipoDAO.modificarEquipo(equipo);
    }

    public void darDeBaja(int id) throws SQLException {
        Permisos.exigirAdmin(Permisos.usuarioActual(baseDatos, sesion));
        Equipo equipo = equipoDAO.obtenerPorId(id);
        if(equipo == null) { throw new IllegalArgumentException("No existe el equipo."); }
        equipo.setEstado("BAJA");
        equipoDAO.modificarEquipo(equipo);
    }

    private void validarUsuario(Integer id) throws SQLException {
        if(id != null && new UsuarioDAO(baseDatos).obtenerPorId(id) == null) {
            throw new IllegalArgumentException("No existe el usuario asignado.");
        }
    }
}

