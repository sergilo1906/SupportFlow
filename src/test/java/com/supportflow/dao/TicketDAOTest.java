package com.supportflow.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.supportflow.model.Ticket;
import com.supportflow.model.TicketStatus;
import com.supportflow.util.ConexionBD;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketDAOTest {

    @TempDir
    private Path carpetaTemporal;

    private ConexionBD baseDatos;
    private TicketDAO ticketDAO;
    private ByteArrayOutputStream salida;
    private PrintStream salidaOriginal;
    private PrintStream salidaPrueba;

    @BeforeEach
    void prepararBDTemporal() throws Exception {

        // Crear una BD nova per a cada prova
        baseDatos = new ConexionBD(carpetaTemporal.resolve("tickets.db"));
        ticketDAO = new TicketDAO(baseDatos);

        String sql = Files.readString(Path.of("src/main/resources/db/crear_tickets.sql"));
        try(Connection conexion = baseDatos.abrirConexion();
            Statement consulta = conexion.createStatement()) {
            consulta.execute(sql);
        }

        // Guardar dos tickets nomes a la BD temporal
        ticketDAO.guardarTicket(new Ticket("Impresora", "No imprime"));
        ticketDAO.guardarTicket(new Ticket("Pantalla", "No muestra imagen"));

        // Recollir els missatges sense canviar els metodes del DAO
        salida = new ByteArrayOutputStream();
        salidaOriginal = System.out;
        salidaPrueba = new PrintStream(salida, true, StandardCharsets.UTF_8);
        System.setOut(salidaPrueba);
    }

    @AfterEach
    void restaurarSalida() {
        if(salidaOriginal != null) {
            System.setOut(salidaOriginal);
        }
        if(salidaPrueba != null) {
            salidaPrueba.close();
        }
    }

    @Test
    void cambiarEstadoModificaSoloElTicketIndicado() throws SQLException {
        ticketDAO.cambiarEstado(1, TicketStatus.IN_PROGRESS);

        // Llegir de nou les dades guardades
        assertEquals("IN_PROGRESS", consultarEstado(1));
        assertEquals("OPEN", consultarEstado(2));
        assertEquals("Estado del ticket modificado correctamente", leerMensaje());
    }

    @Test
    void cambiarEstadoInformaSiNoExisteElId() throws SQLException {
        ticketDAO.cambiarEstado(999, TicketStatus.RESOLVED);

        assertNull(consultarEstado(999));
        assertEquals("OPEN", consultarEstado(1));
        assertEquals("OPEN", consultarEstado(2));
        assertEquals("No existe un ticket con este ID", leerMensaje());
    }

    @Test
    void eliminarTicketBorraSoloElTicketIndicado() throws SQLException {
        ticketDAO.eliminarTicket(1);

        assertNull(consultarEstado(1));
        assertEquals("OPEN", consultarEstado(2));
        assertEquals("Ticket eliminado correctamente", leerMensaje());
    }

    @Test
    void eliminarTicketInformaSiNoExisteElId() throws SQLException {
        ticketDAO.eliminarTicket(999);

        assertNull(consultarEstado(999));
        assertEquals("OPEN", consultarEstado(1));
        assertEquals("OPEN", consultarEstado(2));
        assertEquals("No existe un ticket con este ID", leerMensaje());
    }

    // Consultar l'estat real des d'una altra connexio
    private String consultarEstado(int id) throws SQLException {
        String sql = "SELECT estado FROM tickets WHERE id = ?";

        try(Connection conexion = baseDatos.abrirConexion();
            PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setInt(1, id);
            try(ResultSet resultado = consulta.executeQuery()) {
                if(resultado.next()) {
                    return resultado.getString("estado");
                } else {
                    return null;
                }
            }
        }
    }

    private String leerMensaje() {
        return salida.toString(StandardCharsets.UTF_8).trim();
    }
}
