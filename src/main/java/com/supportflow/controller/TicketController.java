package com.supportflow.controller;

import com.supportflow.model.*;
import com.supportflow.service.TicketService;
import com.supportflow.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

// Taula d'incidencies autoritzades
public class TicketController {
    @FXML private TableView<Ticket> tablaTickets;
    @FXML private ComboBox<TicketStatus> cbEstado;
    @FXML private ComboBox<TicketPriority> cbPrioridad;
    @FXML private Button btnEliminar;
    @FXML private Label lbMensaje;
    private ContextoApp contexto;
    private TicketService servicio;

    @FXML
    private void initialize() {
        cbEstado.getItems().add(null);
        TicketStatus[] estados = TicketStatus.values();
        for(int i = 0; i < estados.length; i++) { cbEstado.getItems().add(estados[i]); }
        cbPrioridad.getItems().add(null);
        TicketPriority[] prioridades = TicketPriority.values();
        for(int i = 0; i < prioridades.length; i++) { cbPrioridad.getItems().add(prioridades[i]); }
    }

    public void configurar(ContextoApp contexto) {
        this.contexto = contexto;
        servicio = new TicketService(contexto.getBaseDatos(), contexto.getSesion());
        try { btnEliminar.setDisable(contexto.usuarioActual().getRol() != UsuarioRol.ADMIN); }
        catch(Exception error) { Avisos.error(error); }
        onFiltrar();
    }

    @FXML
    private void onFiltrar() {
        try {
            tablaTickets.getItems().setAll(servicio.obtenerTickets(cbEstado.getValue(), cbPrioridad.getValue()));
            lbMensaje.setText("Tickets visibles: " + tablaTickets.getItems().size());
        } catch(Exception error) { Avisos.error(error); }
    }

    @FXML
    private void onLimpiarFiltros() {
        cbEstado.setValue(null);
        cbPrioridad.setValue(null);
        onFiltrar();
    }

    @FXML private void onCrear() { abrir("formulario_ticket"); }

    @FXML
    private void onConsultar() {
        Ticket ticket = seleccionado();
        if(ticket != null) {
            contexto.setTicketSeleccionado(Math.toIntExact(ticket.getId()));
            abrir("detalle_ticket");
        }
    }

    @FXML
    private void onEliminar() {
        Ticket ticket = seleccionado();
        if(ticket != null && Avisos.confirmar("Eliminar el ticket " + ticket.getId() + " y sus comentarios e historial?")) {
            try {
                servicio.eliminarTicket(Math.toIntExact(ticket.getId()));
                onFiltrar();
                lbMensaje.setText("Ticket eliminado correctamente.");
            } catch(Exception error) { Avisos.error(error); }
        }
    }

    private Ticket seleccionado() {
        Ticket ticket = tablaTickets.getSelectionModel().getSelectedItem();
        if(ticket == null) { Avisos.error(new IllegalArgumentException("Selecciona un ticket.")); }
        return ticket;
    }

    @FXML private void onVolver() { abrir("principal"); }

    private void abrir(String pantalla) {
        try { contexto.mostrar(pantalla); } catch(Exception error) { Avisos.error(error); }
    }
}

