package com.supportflow.controller;

import com.supportflow.model.*;
import com.supportflow.service.*;
import com.supportflow.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

// Consultar i editar l'inventari
public class InventarioController {
    @FXML private TableView<Equipo> tablaEquipos;
    @FXML private TextField tfNombre;
    @FXML private TextField tfTipo;
    @FXML private TextField tfMarca;
    @FXML private TextField tfModelo;
    @FXML private TextField tfSerie;
    @FXML private ComboBox<String> cbEstado;
    @FXML private ComboBox<Usuario> cbUsuario;
    @FXML private Button btnGuardar;
    @FXML private Button btnBaja;
    @FXML private Button btnNuevo;
    @FXML private Button btnSinUsuario;
    @FXML private Label lbMensaje;
    private ContextoApp contexto;
    private EquipoService servicio;
    private int equipoId;

    @FXML
    private void initialize() {
        cbEstado.getItems().add("ACTIVO");
        cbEstado.getItems().add("BAJA");
        cbEstado.setValue("ACTIVO");
    }

    public void configurar(ContextoApp contexto) {
        this.contexto = contexto;
        servicio = new EquipoService(contexto.getBaseDatos(), contexto.getSesion());
        try {
            UsuarioRol rol = contexto.usuarioActual().getRol();
            btnGuardar.setDisable(rol == UsuarioRol.EMPLEADO);
            btnNuevo.setDisable(rol != UsuarioRol.ADMIN);
            btnBaja.setDisable(rol != UsuarioRol.ADMIN);
            cbUsuario.setDisable(rol != UsuarioRol.ADMIN);
            btnSinUsuario.setDisable(rol != UsuarioRol.ADMIN);
            cbEstado.setDisable(rol != UsuarioRol.ADMIN);
            cbUsuario.getItems().add(null);
            if(rol != UsuarioRol.EMPLEADO) {
                cbUsuario.getItems().addAll(new UsuarioService(contexto.getBaseDatos(), contexto.getSesion()).obtenerAsignables());
            }
            recargar();
        } catch(Exception error) { Avisos.error(error); }
    }

    private void recargar() throws Exception {
        tablaEquipos.getItems().setAll(servicio.obtenerEquipos());
    }

    @FXML
    private void onEditar() {
        Equipo equipo = tablaEquipos.getSelectionModel().getSelectedItem();
        if(equipo == null) { Avisos.error(new IllegalArgumentException("Selecciona un equipo.")); return; }
        equipoId = equipo.getId();
        tfNombre.setText(equipo.getNombre());
        tfTipo.setText(equipo.getTipo());
        tfMarca.setText(equipo.getMarca());
        tfModelo.setText(equipo.getModelo());
        tfSerie.setText(equipo.getNumeroSerie());
        cbEstado.setValue(equipo.getEstado());
        cbUsuario.setValue(null);
        for(int i = 0; i < cbUsuario.getItems().size(); i++) {
            Usuario usuario = cbUsuario.getItems().get(i);
            if(usuario != null && Integer.valueOf(usuario.getId()).equals(equipo.getUsuarioAsignado())) { cbUsuario.setValue(usuario); }
        }
        lbMensaje.setText("Equipo seleccionado: " + equipoId);
    }

    @FXML
    private void onNuevo() {
        equipoId = 0;
        tfNombre.clear();
        tfTipo.clear();
        tfMarca.clear();
        tfModelo.clear();
        tfSerie.clear();
        cbEstado.setValue("ACTIVO");
        cbUsuario.setValue(null);
        lbMensaje.setText("Nuevo equipo");
    }

    @FXML
    private void onGuardar() {
        try {
            Equipo equipo = new Equipo(tfNombre.getText(), tfTipo.getText(), tfMarca.getText(), tfModelo.getText(), tfSerie.getText());
            equipo.setId(equipoId);
            equipo.setEstado(cbEstado.getValue());
            if(cbUsuario.getValue() != null) { equipo.setUsuarioAsignado(cbUsuario.getValue().getId()); }
            if(equipoId == 0) { servicio.guardarEquipo(equipo); }
            else { servicio.modificarEquipo(equipo); }
            equipoId = equipo.getId();
            recargar();
            lbMensaje.setText("Equipo guardado correctamente.");
        } catch(Exception error) { Avisos.error(error); }
    }

    @FXML
    private void onBaja() {
        Equipo equipo = tablaEquipos.getSelectionModel().getSelectedItem();
        if(equipo == null) { Avisos.error(new IllegalArgumentException("Selecciona un equipo.")); return; }
        if(Avisos.confirmar("Dar de baja el equipo sin borrar sus tickets historicos?")) {
            try { servicio.darDeBaja(equipo.getId()); recargar(); onNuevo(); lbMensaje.setText("Equipo dado de baja."); }
            catch(Exception error) { Avisos.error(error); }
        }
    }

    @FXML private void onSinUsuario() { cbUsuario.setValue(null); }
    @FXML private void onVolver() {
        try { contexto.mostrar("principal"); } catch(Exception error) { Avisos.error(error); }
    }
}

