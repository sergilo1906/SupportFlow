
package com.supportflow.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

// Clase para comprobar que los tickets funcionan correctamente
class TicketTest {

    // Proba Incidencia
    @Test
    void comprobarCreacionTicket() {

        //Crear Incidenccia
        Ticket ticket = new Ticket("Impresora averiada", "No imprime");

        // Comprobar dades
        assertEquals(0, ticket.getId());
        assertEquals("Impresora averiada", ticket.getTitulo());
        assertEquals("No imprime", ticket.getDescripcion());

        // Comprobar valors incials 
        assertEquals(TicketPriority.MEDIUM, ticket.getPrioridad());
        assertEquals(TicketStatus.OPEN, ticket.getEstado());

        // Comprobar que tingui dades
        assertNotNull(ticket.getFechaCreacion());
    }

    // PRUEBA 2: Modificar los datos de una incidencia
    @Test
    void comprobarModificacionTicket() {

        // Creamos un ticket con prioridad alta
        Ticket ticket = new Ticket("Sin internet", "No hay conexion",
                TicketPriority.HIGH);

        // Modificamos los datos utilizando setters
        ticket.setId(7);
        ticket.setTitulo("Conexion restaurada");
        ticket.setDescripcion("Se reviso el cable");
        ticket.setPrioridad(TicketPriority.LOW);
        ticket.setEstado(TicketStatus.RESOLVED);

        // Comprobamos los cambios
        assertEquals(7, ticket.getId());
        assertEquals("Conexion restaurada", ticket.getTitulo());
        assertEquals("Se reviso el cable", ticket.getDescripcion());
        assertEquals(TicketPriority.LOW, ticket.getPrioridad());
        assertEquals(TicketStatus.RESOLVED, ticket.getEstado());
    }

    // PRUEBA 3: Comprobar los valores por defecto
    @Test
    void comprobarValoresPorDefecto() {

        // Creamos un ticket sin indicar prioridad
        Ticket ticket = new Ticket("Pantalla negra", "No muestra imagen", null);

        // Si la prioridad es null, debe ser MEDIUM
        assertEquals(TicketPriority.MEDIUM, ticket.getPrioridad());

        // Modificamos la prioridad y el estado
        ticket.setPrioridad(TicketPriority.HIGH);
        ticket.setEstado(TicketStatus.CLOSED);

        // Intentamos asignar valores null
        ticket.setPrioridad(null);
        ticket.setEstado(null);

        // Comprobamos que se asignan los valores por defecto
        assertEquals(TicketPriority.MEDIUM, ticket.getPrioridad());
        assertEquals(TicketStatus.OPEN, ticket.getEstado());
    }

    // PRUEBA 4: Comprobar que se rechazan datos incorrectos
    @Test
    void comprobarDatosIncorrectos() {

        // No se permite un titulo vacio
        assertThrows(IllegalArgumentException.class,
                () -> new Ticket("", "Descripcion"));

        // No se permite una descripcion vacia
        assertThrows(IllegalArgumentException.class,
                () -> new Ticket("Titulo", ""));

        // No se permite un titulo de mas de 150 caracteres
        assertThrows(IllegalArgumentException.class,
                () -> new Ticket("x".repeat(151), "Descripcion"));

        // Creamos un ticket correcto
        Ticket ticket = new Ticket("Impresora", "No funciona");

        // No se permite un identificador negativo
        assertThrows(IllegalArgumentException.class,
                () -> ticket.setId(-1));

        // Comprobamos que el objeto sigue teniendo sus datos
        assertEquals(0, ticket.getId());
        assertEquals("Impresora", ticket.getTitulo());
    }

    // PRUEBA 5: Recuperar una incidencia guardada
    @Test
    void comprobarTicketRecuperado() {

        // Fecha que tenia la incidencia originalmente
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 1, 9, 15);

        // Simulamos un ticket recuperado de la base de datos
        Ticket ticket = new Ticket(
                42,
                "Disco averiado",
                "El ordenador no arranca",
                TicketPriority.HIGH,
                TicketStatus.IN_PROGRESS,
                fecha);

        // Comprobamos que mantiene sus datos originales
        assertEquals(42, ticket.getId());
        assertEquals("Disco averiado", ticket.getTitulo());
        assertEquals(TicketPriority.HIGH, ticket.getPrioridad());
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getEstado());
        assertEquals(fecha, ticket.getFechaCreacion());
    }
}
