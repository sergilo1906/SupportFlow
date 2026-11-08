package com.supportflow.dao;

import com.supportflow.model.Usuario;
import com.supportflow.model.UsuarioRol;
import com.supportflow.util.ConexionBD;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

// Guardar les dades locals, mai les contrasenyes
public class UsuarioDAO {
    private ConexionBD baseDatos;

    public UsuarioDAO(ConexionBD baseDatos) {
        this.baseDatos = baseDatos;
    }

    public void guardarUsuario(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios(firebase_uid,nombre,email,rol,fecha_registro) VALUES(?,?,?,?,?)";
        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, usuario.getFirebaseUid());
            consulta.setString(2, usuario.getNombre());
            consulta.setString(3, usuario.getEmail());
            consulta.setString(4, usuario.getRol().name());
            consulta.setString(5, usuario.getFechaRegistro().toString());
            consulta.executeUpdate();
            try(ResultSet resultado = consulta.getGeneratedKeys()) {
                if(resultado.next()) { usuario.setId(resultado.getInt(1)); }
            }
        }
    }

    public Usuario obtenerPorUid(String uid) throws SQLException {
        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM usuarios WHERE firebase_uid = ?")) {
            consulta.setString(1, uid);
            try(ResultSet resultado = consulta.executeQuery()) {
                if(resultado.next()) { return leerUsuario(resultado); }
                return null;
            }
        }
    }

    public Usuario obtenerPorId(int id) throws SQLException {
        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM usuarios WHERE id = ?")) {
            consulta.setInt(1, id);
            try(ResultSet resultado = consulta.executeQuery()) {
                if(resultado.next()) { return leerUsuario(resultado); }
                return null;
            }
        }
    }

    public ArrayList<Usuario> obtenerUsuarios() throws SQLException {
        ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
        try(Connection conexion = baseDatos.abrirConexion();
            Statement consulta = conexion.createStatement();
            ResultSet resultado = consulta.executeQuery("SELECT * FROM usuarios ORDER BY nombre,id")) {
            while(resultado.next()) { usuarios.add(leerUsuario(resultado)); }
        }
        return usuarios;
    }

    public void cambiarRol(int id, UsuarioRol rol) throws SQLException {
        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement("UPDATE usuarios SET rol = ? WHERE id = ?")) {
            consulta.setString(1, rol.name());
            consulta.setInt(2, id);
            if(consulta.executeUpdate() != 1) { throw new SQLException("No existe el usuario."); }
        }
    }

    public void cambiarNombre(int id, String nombre) throws SQLException {
        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement("UPDATE usuarios SET nombre = ? WHERE id = ?")) {
            consulta.setString(1, nombre);
            consulta.setInt(2, id);
            if(consulta.executeUpdate() != 1) { throw new SQLException("No existe el usuario."); }
        }
    }

    public void eliminarUsuario(int id) throws SQLException {
        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            consulta.setInt(1, id);
            if(consulta.executeUpdate() != 1) { throw new SQLException("No existe el usuario."); }
        }
    }

    public int contarAdministradores() throws SQLException {
        try(Connection conexion = baseDatos.abrirConexion();
            Statement consulta = conexion.createStatement();
            ResultSet resultado = consulta.executeQuery("SELECT COUNT(*) FROM usuarios WHERE rol = 'ADMIN'")) {
            resultado.next();
            return resultado.getInt(1);
        }
    }

    private Usuario leerUsuario(ResultSet resultado) throws SQLException {
        Usuario usuario = new Usuario(resultado.getString("firebase_uid"), resultado.getString("nombre"),
                resultado.getString("email"), UsuarioRol.valueOf(resultado.getString("rol")));
        usuario.setId(resultado.getInt("id"));
        usuario.setFechaRegistro(LocalDateTime.parse(resultado.getString("fecha_registro")));
        return usuario;
    }
}

