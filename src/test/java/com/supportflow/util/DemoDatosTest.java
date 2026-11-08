package com.supportflow.util;

import static org.junit.jupiter.api.Assertions.*;
import com.supportflow.dao.*;
import com.supportflow.model.*;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DemoDatosTest {
    @TempDir
    Path carpetaTemporal;

    @Test
    void demoEsRepetibleSinDuplicarNiReiniciarDatos() throws Exception {
        ConexionBD baseDatos = new ConexionBD(carpetaTemporal.resolve("demo.db"));
        DemoDatos.preparar(baseDatos);
        UsuarioDAO usuarios = new UsuarioDAO(baseDatos);
        Usuario empleado = usuarios.obtenerPorUid("demo:empleado");
        usuarios.cambiarNombre(empleado.getId(), "Nombre conservado");
        DemoDatos.preparar(baseDatos);
        assertEquals(4, usuarios.obtenerUsuarios().size());
        assertEquals("Nombre conservado", usuarios.obtenerPorUid("demo:empleado").getNombre());
        assertEquals(2, new TicketDAO(baseDatos).obtenerTickets().size());
        assertEquals(1, new EquipoDAO(baseDatos).obtenerEquipos().size());
    }

    @Test
    void demoRechazaLaRutaRealAntesDeAbrirla() {
        ConexionBD baseDatos = new ConexionBD(Path.of("data", "supportflow.db"));
        assertThrows(IllegalArgumentException.class, () -> DemoDatos.preparar(baseDatos));
    }

    @Test
    void demoRechazaUsuariosDelModoReal() throws Exception {
        ConexionBD baseDatos = new ConexionBD(carpetaTemporal.resolve("mezcla.db"));
        EsquemaBD.inicializar(baseDatos);
        new UsuarioDAO(baseDatos).guardarUsuario(new Usuario("uid-real-de-prueba", "Prueba", "prueba@example.invalid", UsuarioRol.EMPLEADO));
        assertThrows(IllegalArgumentException.class, () -> DemoDatos.preparar(baseDatos));
    }
}
