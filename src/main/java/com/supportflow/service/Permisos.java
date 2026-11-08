package com.supportflow.service;

import com.supportflow.dao.UsuarioDAO;
import com.supportflow.model.Usuario;
import com.supportflow.model.UsuarioRol;
import com.supportflow.util.ConexionBD;
import java.sql.SQLException;

// Comprovar el rol actual abans d'executar l'operacio
public class Permisos {
    public static Usuario usuarioActual(ConexionBD baseDatos, Sesion sesion) throws SQLException {
        if(sesion == null) { throw new SecurityException("No hay una sesion abierta."); }
        sesion.comprobarActiva();
        Usuario usuario = new UsuarioDAO(baseDatos).obtenerPorId(sesion.getUsuario().getId());
        if(usuario == null || !usuario.getFirebaseUid().equals(sesion.getUsuario().getFirebaseUid())) {
            throw new SecurityException("El usuario local ya no existe.");
        }
        return usuario;
    }

    public static void exigirAdmin(Usuario usuario) {
        if(usuario.getRol() != UsuarioRol.ADMIN) {
            throw new SecurityException("Esta operacion requiere el rol ADMIN.");
        }
    }
}

