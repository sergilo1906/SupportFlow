package com.supportflow.util;

import com.supportflow.dao.*;
import com.supportflow.model.*;
import com.supportflow.service.*;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;

// Dades ficticies exclusives de la demostracio
public class DemoDatos {
    public static void preparar(ConexionBD baseDatos) throws SQLException {
        Path real = Path.of("data", "supportflow.db").toAbsolutePath().normalize();
        if(baseDatos.getRutaBD().equals(real)) { throw new IllegalArgumentException("La DEMO no puede usar la base real."); }
        EsquemaBD.inicializar(baseDatos);
        UsuarioDAO usuarios = new UsuarioDAO(baseDatos);
        ArrayList<Usuario> existentes = usuarios.obtenerUsuarios();
        if(!existentes.isEmpty()) {
            for(int i = 0; i < existentes.size(); i++) {
                if(!existentes.get(i).getFirebaseUid().startsWith("demo:")) {
                    throw new IllegalArgumentException("La base DEMO contiene usuarios del modo real.");
                }
            }
            return;
        }
        Usuario admin = new Usuario("demo:admin", "Administrador DEMO", "admin@example.invalid", UsuarioRol.ADMIN);
        Usuario tecnico = new Usuario("demo:tecnico", "Tecnico DEMO", "tecnico@example.invalid", UsuarioRol.TECNICO);
        Usuario empleado = new Usuario("demo:empleado", "Empleado DEMO", "empleado@example.invalid", UsuarioRol.EMPLEADO);
        Usuario otro = new Usuario("demo:otro", "Otro empleado DEMO", "otro@example.invalid", UsuarioRol.EMPLEADO);
        usuarios.guardarUsuario(admin);
        usuarios.guardarUsuario(tecnico);
        usuarios.guardarUsuario(empleado);
        usuarios.guardarUsuario(otro);
        Sesion sesionAdmin = new Sesion(admin, true, null);
        Equipo equipo = new Equipo("Portatil de oficina", "Portatil", "Marca de ejemplo", "Modelo DAM", "DEMO-001");
        equipo.setUsuarioAsignado(empleado.getId());
        new EquipoService(baseDatos, sesionAdmin).guardarEquipo(equipo);
        TicketService incidencias = new TicketService(baseDatos, new Sesion(empleado, true, null));
        incidencias.crearTicket("Impresora sin conexion", "No puedo imprimir desde el portatil.", TicketPriority.HIGH, equipo.getId());
        int id = (int)incidencias.obtenerTickets(null, null).get(0).getId();
        new TicketService(baseDatos, sesionAdmin).asignarTecnico(id, tecnico.getId());
        incidencias.anadirComentario(id, "He comprobado que la impresora esta encendida.");
        new TicketService(baseDatos, new Sesion(otro, true, null)).crearTicket(
                "Actualizar programa", "Solicitud de actualizacion de ejemplo.", TicketPriority.LOW, null);
    }
}

