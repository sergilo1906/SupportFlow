package com.supportflow.service;

import com.supportflow.auth.AuthException;
import com.supportflow.auth.FirebaseAuth;
import com.supportflow.dao.UsuarioDAO;
import com.supportflow.model.Usuario;
import com.supportflow.model.UsuarioRol;
import com.supportflow.util.ConexionBD;
import java.sql.SQLException;

// Entrar nomes despres de la verificacio de Firebase
public class AutenticacionService {
    private ConexionBD baseDatos;
    private FirebaseAuth firebase;
    private String adminInicialUid;

    public AutenticacionService(ConexionBD baseDatos, FirebaseAuth firebase, String adminInicialUid) {
        this.baseDatos = baseDatos;
        this.firebase = firebase;
        this.adminInicialUid = adminInicialUid;
    }

    public Sesion iniciarSesion(String email, String contrasena) throws AuthException, SQLException {
        firebase.iniciarSesion(email, contrasena);
        return crearSesionVerificada();
    }

    public Sesion crearSesionVerificada() throws AuthException, SQLException {
        if(!firebase.isVerificado() || firebase.getUid() == null || firebase.getUid().startsWith("demo:")) {
            throw new AuthException("Verifica tu correo antes de entrar. Puedes reenviar la verificacion desde el acceso.");
        }
        UsuarioDAO dao = new UsuarioDAO(baseDatos);
        Usuario usuario = dao.obtenerPorUid(firebase.getUid());
        if(usuario == null) {
            usuario = new Usuario(firebase.getUid(), firebase.getNombre(), firebase.getEmail(), UsuarioRol.EMPLEADO);
            dao.guardarUsuario(usuario);
        }
        // Instalacio explicita: nomes el UID indicat al fitxer local
        if(adminInicialUid != null && !adminInicialUid.isBlank() && adminInicialUid.equals(firebase.getUid()) &&
                dao.contarAdministradores() == 0) {
            dao.cambiarRol(usuario.getId(), UsuarioRol.ADMIN);
            usuario.setRol(UsuarioRol.ADMIN);
        }
        return new Sesion(usuario, false, firebase.getCaducidad());
    }
}

