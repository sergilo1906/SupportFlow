package com.supportflow.service;

import com.supportflow.dao.UsuarioDAO;
import com.supportflow.model.Usuario;
import com.supportflow.model.UsuarioRol;
import com.supportflow.util.ConexionBD;
import java.sql.SQLException;
import java.util.ArrayList;

// Administrar nomes els registres locals
public class UsuarioService {
    private ConexionBD baseDatos;
    private Sesion sesion;
    private UsuarioDAO usuarioDAO;

    public UsuarioService(ConexionBD baseDatos, Sesion sesion) {
        this.baseDatos = baseDatos;
        this.sesion = sesion;
        usuarioDAO = new UsuarioDAO(baseDatos);
    }

    public ArrayList<Usuario> obtenerUsuarios() throws SQLException {
        Permisos.exigirAdmin(Permisos.usuarioActual(baseDatos, sesion));
        return usuarioDAO.obtenerUsuarios();
    }

    public ArrayList<Usuario> obtenerAsignables() throws SQLException {
        Usuario usuario = Permisos.usuarioActual(baseDatos, sesion);
        if(usuario.getRol() == UsuarioRol.EMPLEADO) { throw new SecurityException("No puedes consultar el directorio local."); }
        return usuarioDAO.obtenerUsuarios();
    }

    public void cambiarRol(int id, UsuarioRol rol) throws SQLException {
        Permisos.exigirAdmin(Permisos.usuarioActual(baseDatos, sesion));
        Usuario usuario = usuarioDAO.obtenerPorId(id);
        if(usuario == null || rol == null) { throw new IllegalArgumentException("Selecciona usuario y rol."); }
        if(usuario.getRol() == UsuarioRol.ADMIN && rol != UsuarioRol.ADMIN && usuarioDAO.contarAdministradores() <= 1) {
            throw new IllegalArgumentException("Debe quedar al menos un administrador.");
        }
        usuarioDAO.cambiarRol(id, rol);
    }

    public void cambiarNombre(int id, String nombre) throws SQLException {
        Permisos.exigirAdmin(Permisos.usuarioActual(baseDatos, sesion));
        if(nombre == null || nombre.isBlank()) { throw new IllegalArgumentException("El nombre es obligatorio."); }
        usuarioDAO.cambiarNombre(id, nombre.trim());
    }

    public void eliminarUsuario(int id) throws SQLException {
        Usuario actual = Permisos.usuarioActual(baseDatos, sesion);
        Permisos.exigirAdmin(actual);
        if(id == actual.getId()) { throw new IllegalArgumentException("No puedes eliminar tu propio registro."); }
        Usuario usuario = usuarioDAO.obtenerPorId(id);
        if(usuario == null) { throw new IllegalArgumentException("No existe el usuario."); }
        if(usuario.getRol() == UsuarioRol.ADMIN && usuarioDAO.contarAdministradores() <= 1) {
            throw new IllegalArgumentException("Debe quedar al menos un administrador.");
        }
        // Las claves foraneas impiden borrar autores de tickets y comentarios
        usuarioDAO.eliminarUsuario(id);
    }
}

