package com.supportflow.controller;

import com.supportflow.model.*;
import com.supportflow.service.*;
import com.supportflow.util.*;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

// Informacio, comentaris i historial d'un ticket
public class DetalleTicketController {
    @FXML private Label lbDatos;
    @FXML private ListView<String> listaComentarios;
    @FXML private ListView<String> listaHistorial;
    @FXML private TextArea taComentario;
    @FXML private ComboBox<TicketStatus> cbEstado;
    @FXML private ComboBox<Usuario> cbTecnico;
    @FXML private Button btnCambiarEstado;
    @FXML private Button btnAsignar;
    @FXML private Button btnSinTecnico;
    private ContextoApp contexto;
    private TicketService servicio;

    @FXML
    private void initialize() {
        TicketStatus[] estados = TicketStatus.values();
        for(int i = 0; i < estados.length; i++) { cbEstado.getItems().add(estados[i]); }
    }

    public void configurar(ContextoApp contexto) {
        this.contexto = contexto;
        servicio = new TicketService(contexto.getBaseDatos(), contexto.getSesion());
        cargarDetalle();
    }

    private void cargarDetalle() {
        try {
            int id = contexto.getTicketSeleccionado();
            Ticket ticket = servicio.consultarTicket(id);
            Usuario usuario = contexto.usuarioActual();
            String equipo = "Sin equipo";
            if(ticket.getEquipoId() != null) {
                Equipo dato = new com.supportflow.dao.EquipoDAO(contexto.getBaseDatos()).obtenerPorId(ticket.getEquipoId());
                if(dato != null) { equipo = dato.toString(); }
            }
            lbDatos.setText("#" + id + " - " + ticket.getTitulo() + "\n" + ticket.getDescripcion() +
                    "\nPrioridad: " + ticket.getPrioridad() + " | Estado: " + ticket.getEstado() + " | Fecha: " + ticket.getFechaCreacion() +
                    "\nCreador: " + servicio.nombreUsuario(ticket.getUsuarioCreador()) + " | Tecnico: " + servicio.nombreUsuario(ticket.getTecnicoAsignado()) +
                    "\nEquipo: " + equipo);
            cbEstado.setValue(ticket.getEstado());
            btnCambiarEstado.setDisable(usuario.getRol() == UsuarioRol.EMPLEADO ||
                    (usuario.getRol() == UsuarioRol.TECNICO && !Integer.valueOf(usuario.getId()).equals(ticket.getTecnicoAsignado())));
            cbEstado.setDisable(btnCambiarEstado.isDisabled());
            cbTecnico.getItems().clear();
            cbTecnico.getItems().add(null);
            if(usuario.getRol() == UsuarioRol.ADMIN) {
                ArrayList<Usuario> usuarios = new UsuarioService(contexto.getBaseDatos(), contexto.getSesion()).obtenerUsuarios();
                for(int i = 0; i < usuarios.size(); i++) {
                    Usuario tecnico = usuarios.get(i);
                    if(tecnico.getRol() == UsuarioRol.TECNICO) {
                        cbTecnico.getItems().add(tecnico);
                        if(Integer.valueOf(tecnico.getId()).equals(ticket.getTecnicoAsignado())) { cbTecnico.setValue(tecnico); }
                    }
                }
            }
            cbTecnico.setDisable(usuario.getRol() != UsuarioRol.ADMIN);
            btnAsignar.setDisable(usuario.getRol() != UsuarioRol.ADMIN);
            btnSinTecnico.setDisable(usuario.getRol() != UsuarioRol.ADMIN);
            listaComentarios.getItems().clear();
            ArrayList<Comentario> comentarios = servicio.obtenerComentarios(id);
            for(int i = 0; i < comentarios.size(); i++) {
                Comentario comentario = comentarios.get(i);
                listaComentarios.getItems().add(comentario.getFecha() + " - " + servicio.nombreUsuario(comentario.getUsuarioId()) + ": " + comentario.getTexto());
            }
            listaHistorial.getItems().clear();
            ArrayList<HistorialTicket> registros = servicio.obtenerHistorial(id);
            for(int i = 0; i < registros.size(); i++) {
                HistorialTicket registro = registros.get(i);
                listaHistorial.getItems().add(registro.getFecha() + " - " + servicio.nombreUsuario(registro.getUsuarioId()) + ": " + registro.getAccion());
            }
        } catch(Exception error) { Avisos.error(error); }
    }

    @FXML
    private void onComentar() {
        try {
            servicio.anadirComentario(contexto.getTicketSeleccionado(), taComentario.getText());
            taComentario.clear();
            cargarDetalle();
        } catch(Exception error) { Avisos.error(error); }
    }

    @FXML
    private void onCambiarEstado() {
        try {
            servicio.cambiarEstado(contexto.getTicketSeleccionado(), cbEstado.getValue());
            cargarDetalle();
        } catch(Exception error) { Avisos.error(error); }
    }

    @FXML
    private void onAsignar() {
        try {
            Integer tecnicoId = null;
            if(cbTecnico.getValue() != null) { tecnicoId = cbTecnico.getValue().getId(); }
            servicio.asignarTecnico(contexto.getTicketSeleccionado(), tecnicoId);
            cargarDetalle();
        } catch(Exception error) { Avisos.error(error); }
    }

    @FXML private void onSinTecnico() { cbTecnico.setValue(null); }
    @FXML private void onVolver() {
        try { contexto.mostrar("tickets"); } catch(Exception error) { Avisos.error(error); }
    }
}

