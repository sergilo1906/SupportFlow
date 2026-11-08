package com.supportflow.dao;

import com.supportflow.model.Equipo;
import com.supportflow.util.ConexionBD;
import java.sql.*;
import java.util.ArrayList;

// Consultes senzilles de l'inventari
public class EquipoDAO {
    private ConexionBD baseDatos;

    public EquipoDAO(ConexionBD baseDatos) {
        this.baseDatos = baseDatos;
    }

    public void guardarEquipo(Equipo equipo) throws SQLException {
        String sql = "INSERT INTO equipos(nombre,tipo,marca,modelo,numero_serie,estado,usuario_asignado) VALUES(?,?,?,?,?,?,?)";
        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ponerDatos(consulta, equipo);
            consulta.executeUpdate();
            try(ResultSet resultado = consulta.getGeneratedKeys()) {
                if(resultado.next()) { equipo.setId(resultado.getInt(1)); }
            }
        }
    }

    public void modificarEquipo(Equipo equipo) throws SQLException {
        String sql = "UPDATE equipos SET nombre=?,tipo=?,marca=?,modelo=?,numero_serie=?,estado=?,usuario_asignado=? WHERE id=?";
        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement(sql)) {
            ponerDatos(consulta, equipo);
            consulta.setInt(8, equipo.getId());
            if(consulta.executeUpdate() != 1) { throw new SQLException("No existe el equipo."); }
        }
    }

    public Equipo obtenerPorId(int id) throws SQLException {
        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM equipos WHERE id=?")) {
            consulta.setInt(1, id);
            try(ResultSet resultado = consulta.executeQuery()) {
                if(resultado.next()) { return leerEquipo(resultado); }
                return null;
            }
        }
    }

    public ArrayList<Equipo> obtenerEquipos() throws SQLException {
        ArrayList<Equipo> equipos = new ArrayList<Equipo>();
        try(Connection conexion = baseDatos.abrirConexion();
            Statement consulta = conexion.createStatement();
            ResultSet resultado = consulta.executeQuery("SELECT * FROM equipos ORDER BY id")) {
            while(resultado.next()) { equipos.add(leerEquipo(resultado)); }
        }
        return equipos;
    }

    private void ponerDatos(PreparedStatement consulta, Equipo equipo) throws SQLException {
        consulta.setString(1, equipo.getNombre());
        consulta.setString(2, equipo.getTipo());
        consulta.setString(3, equipo.getMarca());
        consulta.setString(4, equipo.getModelo());
        consulta.setString(5, equipo.getNumeroSerie());
        consulta.setString(6, equipo.getEstado());
        consulta.setObject(7, equipo.getUsuarioAsignado());
    }

    private Equipo leerEquipo(ResultSet resultado) throws SQLException {
        Equipo equipo = new Equipo(resultado.getString("nombre"), resultado.getString("tipo"),
                resultado.getString("marca"), resultado.getString("modelo"), resultado.getString("numero_serie"));
        equipo.setId(resultado.getInt("id"));
        equipo.setEstado(resultado.getString("estado"));
        int usuarioId = resultado.getInt("usuario_asignado");
        if(!resultado.wasNull()) { equipo.setUsuarioAsignado(usuarioId); }
        return equipo;
    }
}

