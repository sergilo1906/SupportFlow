package com.supportflow.service;

import static org.junit.jupiter.api.Assertions.*;
import com.supportflow.dao.*;
import com.supportflow.model.*;
import com.supportflow.util.*;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// Operacions reals de JDBC en arxius temporals
class AplicacionServiceTest {
    @TempDir
    Path carpetaTemporal;
    private ConexionBD baseDatos;
    private Usuario empleado;
    private Usuario otro;
    private Usuario tecnico;
    private Usuario admin;

    @BeforeEach
    void prepararDatos() throws Exception {
        baseDatos = new ConexionBD(carpetaTemporal.resolve("servicios.db"));
        EsquemaBD.inicializar(baseDatos);
        UsuarioDAO dao = new UsuarioDAO(baseDatos);
        empleado = new Usuario("demo:empleado", "Empleado", "empleado@example.invalid", UsuarioRol.EMPLEADO);
        otro = new Usuario("demo:otro", "Otro", "otro@example.invalid", UsuarioRol.EMPLEADO);
        tecnico = new Usuario("demo:tecnico", "Tecnico", "tecnico@example.invalid", UsuarioRol.TECNICO);
        admin = new Usuario("demo:admin", "Admin", "admin@example.invalid", UsuarioRol.ADMIN);
        dao.guardarUsuario(empleado);
        dao.guardarUsuario(otro);
        dao.guardarUsuario(tecnico);
        dao.guardarUsuario(admin);
    }

    private Sesion sesion(Usuario usuario) {
        return new Sesion(usuario, true, null);
    }

    private TicketService tickets(Usuario usuario) {
        return new TicketService(baseDatos, sesion(usuario));
    }

    private Ticket crearTicket() throws Exception {
        tickets(empleado).crearTicket("Sin red", "No hay conexion", TicketPriority.HIGH, null);
        return tickets(empleado).obtenerTickets(null, null).get(0);
    }

    @Test
    void creaLeeYConservaTicketAlAbrirOtraConexion() throws Exception {
        Ticket ticket = crearTicket();
        assertTrue(ticket.getId() > 0);
        assertEquals(empleado.getId(), ticket.getUsuarioCreador());
        assertEquals("Sin red", new TicketDAO(new ConexionBD(baseDatos.getRutaBD())).obtenerPorId((int)ticket.getId()).getTitulo());
        assertEquals(1, tickets(empleado).obtenerHistorial((int)ticket.getId()).size());
    }

    @Test
    void filtraTicketsSinMostrarLosDeOtroEmpleado() throws Exception {
        crearTicket();
        tickets(otro).crearTicket("Otro ticket", "Descripcion", TicketPriority.LOW, null);
        assertEquals(1, tickets(empleado).obtenerTickets(null, null).size());
        assertEquals(2, tickets(admin).obtenerTickets(null, null).size());
        assertEquals(0, tickets(empleado).obtenerTickets(null, TicketPriority.LOW).size());
        assertEquals(1, tickets(empleado).obtenerTickets(TicketStatus.OPEN, TicketPriority.HIGH).size());
        assertThrows(SecurityException.class, () -> tickets(otro).consultarTicket(1));
    }

    @Test
    void asignaTecnicoYCambiaEstadoConHistorial() throws Exception {
        Ticket ticket = crearTicket();
        int id = (int)ticket.getId();
        assertEquals(0, tickets(tecnico).obtenerTickets(null, null).size());
        tickets(admin).asignarTecnico(id, tecnico.getId());
        assertEquals(1, tickets(tecnico).obtenerTickets(null, null).size());
        tickets(tecnico).cambiarEstado(id, TicketStatus.IN_PROGRESS);
        assertEquals(TicketStatus.IN_PROGRESS, tickets(empleado).consultarTicket(id).getEstado());
        ArrayList<HistorialTicket> historial = tickets(empleado).obtenerHistorial(id);
        assertEquals(3, historial.size());
        assertEquals(tecnico.getId(), historial.get(2).getUsuarioId());
        assertTrue(historial.get(2).getAccion().contains("OPEN a IN_PROGRESS"));
    }

    @Test
    void impideCambiosYAsignacionesSinPermiso() throws Exception {
        int id = (int)crearTicket().getId();
        assertThrows(SecurityException.class, () -> tickets(empleado).cambiarEstado(id, TicketStatus.IN_PROGRESS));
        assertThrows(SecurityException.class, () -> tickets(tecnico).cambiarEstado(id, TicketStatus.IN_PROGRESS));
        assertThrows(SecurityException.class, () -> tickets(empleado).asignarTecnico(id, tecnico.getId()));
        assertThrows(IllegalArgumentException.class, () -> tickets(admin).asignarTecnico(id, otro.getId()));
        assertThrows(IllegalArgumentException.class, () -> tickets(admin).cambiarEstado(id, TicketStatus.CLOSED));
    }

    @Test
    void completaEstadosYPermiteReabrirComoAdmin() throws Exception {
        int id = (int)crearTicket().getId();
        tickets(admin).asignarTecnico(id, tecnico.getId());
        tickets(tecnico).cambiarEstado(id, TicketStatus.IN_PROGRESS);
        tickets(tecnico).cambiarEstado(id, TicketStatus.RESOLVED);
        tickets(tecnico).cambiarEstado(id, TicketStatus.CLOSED);
        assertThrows(IllegalArgumentException.class, () -> tickets(tecnico).cambiarEstado(id, TicketStatus.OPEN));
        tickets(admin).cambiarEstado(id, TicketStatus.OPEN);
        assertEquals(TicketStatus.OPEN, tickets(admin).consultarTicket(id).getEstado());
    }

    @Test
    void revierteElEstadoSiFallaLaEscrituraDelHistorial() throws Exception {
        int id = (int)crearTicket().getId();
        try(Connection conexion = baseDatos.abrirConexion(); Statement consulta = conexion.createStatement()) {
            consulta.execute("CREATE TRIGGER impedir_historial BEFORE INSERT ON historial_tickets BEGIN SELECT RAISE(ABORT,'Fallo de prueba'); END");
        }
        assertThrows(SQLException.class, () -> tickets(admin).cambiarEstado(id, TicketStatus.IN_PROGRESS));
        assertEquals(TicketStatus.OPEN, tickets(admin).consultarTicket(id).getEstado());
        assertThrows(SQLException.class, () -> tickets(admin).asignarTecnico(id, tecnico.getId()));
        assertNull(tickets(admin).consultarTicket(id).getTecnicoAsignado());
        assertEquals(1, tickets(admin).obtenerHistorial(id).size());
    }

    @Test
    void guardaComentariosConAutorYPermisos() throws Exception {
        int id = (int)crearTicket().getId();
        tickets(empleado).anadirComentario(id, "Necesito ayuda");
        Comentario comentario = tickets(empleado).obtenerComentarios(id).get(0);
        assertEquals(empleado.getId(), comentario.getUsuarioId());
        assertEquals("Necesito ayuda", comentario.getTexto());
        assertNotNull(comentario.getFecha());
        assertThrows(SecurityException.class, () -> tickets(otro).anadirComentario(id, "No autorizado"));
        assertThrows(SecurityException.class, () -> tickets(otro).obtenerHistorial(id));
        assertThrows(IllegalArgumentException.class, () -> tickets(empleado).anadirComentario(id, " "));
    }

    @Test
    void soloAdminPuedeEliminarTicketYSusDatosDependientes() throws Exception {
        int id = (int)crearTicket().getId();
        tickets(empleado).anadirComentario(id, "Comentario");
        assertThrows(SecurityException.class, () -> tickets(empleado).eliminarTicket(id));
        tickets(admin).eliminarTicket(id);
        assertNull(new TicketDAO(baseDatos).obtenerPorId(id));
        assertEquals(0, new ComentarioDAO(baseDatos).obtenerComentarios(id).size());
        assertEquals(0, new HistorialTicketDAO(baseDatos).obtenerHistorial(id).size());
    }

    @Test
    void guardaModificaYAsignaEquipos() throws Exception {
        EquipoService servicio = new EquipoService(baseDatos, sesion(admin));
        Equipo equipo = new Equipo("Portatil", "PC", "Marca", "Modelo", "SERIE-1");
        equipo.setUsuarioAsignado(empleado.getId());
        servicio.guardarEquipo(equipo);
        assertTrue(equipo.getId() > 0);
        equipo.setModelo("Modelo actualizado");
        servicio.modificarEquipo(equipo);
        assertEquals("Modelo actualizado", new EquipoDAO(baseDatos).obtenerPorId(equipo.getId()).getModelo());
        assertEquals(1, new EquipoService(baseDatos, sesion(empleado)).obtenerEquipos().size());
        assertEquals(0, new EquipoService(baseDatos, sesion(otro)).obtenerEquipos().size());
        assertThrows(SecurityException.class, () -> new EquipoService(baseDatos, sesion(empleado)).guardarEquipo(equipo));
    }

    @Test
    void bajaConservaLaRelacionConElTicketHistorico() throws Exception {
        EquipoService servicio = new EquipoService(baseDatos, sesion(admin));
        Equipo equipo = new Equipo("PC", "Sobremesa", "Marca", "Modelo", "SERIE-2");
        equipo.setUsuarioAsignado(empleado.getId());
        servicio.guardarEquipo(equipo);
        tickets(empleado).crearTicket("Disco", "No arranca", TicketPriority.CRITICAL, equipo.getId());
        servicio.darDeBaja(equipo.getId());
        assertEquals(equipo.getId(), tickets(empleado).obtenerTickets(null, null).get(0).getEquipoId());
        assertEquals("BAJA", new EquipoDAO(baseDatos).obtenerPorId(equipo.getId()).getEstado());
        assertThrows(IllegalArgumentException.class, () -> tickets(empleado).crearTicket("Otra", "Descripcion", TicketPriority.LOW, equipo.getId()));
    }

    @Test
    void tecnicoActualizaInventarioPeroNoLoReasignaNiDaDeBaja() throws Exception {
        Equipo equipo = new Equipo("PC", "PC", "Marca", "Modelo", "SERIE-3");
        new EquipoService(baseDatos, sesion(admin)).guardarEquipo(equipo);
        EquipoService servicio = new EquipoService(baseDatos, sesion(tecnico));
        equipo.setMarca("Otra marca");
        servicio.modificarEquipo(equipo);
        equipo.setUsuarioAsignado(empleado.getId());
        assertThrows(SecurityException.class, () -> servicio.modificarEquipo(equipo));
        assertThrows(SecurityException.class, () -> servicio.darDeBaja(equipo.getId()));
    }

    @Test
    void gestionaRolesYCompruebaElRolActualEnCadaOperacion() throws Exception {
        UsuarioService servicio = new UsuarioService(baseDatos, sesion(admin));
        assertEquals(4, servicio.obtenerUsuarios().size());
        assertThrows(SecurityException.class, () -> new UsuarioService(baseDatos, sesion(empleado)).obtenerUsuarios());
        assertThrows(IllegalArgumentException.class, () -> servicio.cambiarRol(admin.getId(), UsuarioRol.EMPLEADO));
        Sesion sesionTecnico = sesion(tecnico);
        EquipoService inventario = new EquipoService(baseDatos, sesionTecnico);
        Equipo equipo = new Equipo("PC", "PC", "Marca", "Modelo", "SERIE-4");
        new EquipoService(baseDatos, sesion(admin)).guardarEquipo(equipo);
        servicio.cambiarRol(tecnico.getId(), UsuarioRol.EMPLEADO);
        assertThrows(SecurityException.class, () -> inventario.modificarEquipo(equipo));
        servicio.cambiarNombre(otro.getId(), "Nombre corregido");
        assertEquals("Nombre corregido", new UsuarioDAO(baseDatos).obtenerPorId(otro.getId()).getNombre());
        servicio.eliminarUsuario(otro.getId());
        assertNull(new UsuarioDAO(baseDatos).obtenerPorId(otro.getId()));
    }

    @Test
    void estadisticasCuentanDatosRealesYSoloAdminAccede() throws Exception {
        crearTicket();
        tickets(otro).crearTicket("Otra", "Descripcion", TicketPriority.LOW, null);
        tickets(admin).cambiarEstado(1, TicketStatus.IN_PROGRESS);
        Equipo equipo = new Equipo("PC", "PC", "Marca", "Modelo", "SERIE-5");
        EquipoService inventario = new EquipoService(baseDatos, sesion(admin));
        inventario.guardarEquipo(equipo);
        inventario.darDeBaja(equipo.getId());
        Estadisticas datos = new EstadisticasService(baseDatos, sesion(admin)).obtenerEstadisticas();
        assertEquals(2, datos.getTotalTickets());
        assertEquals(1, datos.getAbiertos());
        assertEquals(1, datos.getEnProgreso());
        assertEquals(0, datos.getResueltos());
        assertEquals(0, datos.getCerrados());
        assertEquals(1, datos.getTotalEquipos());
        assertEquals(0, datos.getEquiposActivos());
        assertEquals(1, datos.getEquiposBaja());
        assertThrows(SecurityException.class, () -> new EstadisticasService(baseDatos, sesion(empleado)).obtenerEstadisticas());
    }

    @Test
    void rechazaSesionCerradaCaducadaYDemoEnModoReal() throws Exception {
        Sesion cerrada = sesion(empleado);
        cerrada.cerrarSesion();
        assertThrows(SecurityException.class, () -> new TicketService(baseDatos, cerrada).obtenerTickets(null, null));
        Usuario real = new Usuario("uid-real-prueba", "Prueba", "prueba@example.invalid", UsuarioRol.EMPLEADO);
        new UsuarioDAO(baseDatos).guardarUsuario(real);
        Sesion caducada = new Sesion(real, false, Instant.now().minusSeconds(1));
        assertThrows(SecurityException.class, () -> new TicketService(baseDatos, caducada).obtenerTickets(null, null));
        Sesion mezcla = new Sesion(empleado, false, Instant.now().plusSeconds(100));
        assertThrows(SecurityException.class, () -> new TicketService(baseDatos, mezcla).obtenerTickets(null, null));
    }
}

