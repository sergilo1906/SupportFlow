package com.supportflow.controller;

import com.supportflow.model.*;
import com.supportflow.service.UsuarioService;
import com.supportflow.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

// Administracio dels usuaris locals
public class UsuariosController {
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private ComboBox<UsuarioRol> cbRol;
    @FXML private TextField tfNombre;
    @FXML private Label lbMensaje;
    private ContextoApp contexto;
    private UsuarioService servicio;

    @FXML
    private void initialize() {
        UsuarioRol[] roles = UsuarioRol.values();
        for(int i = 0; i < roles.length; i++) { cbRol.getItems().add(roles[i]); }
    }

    public void configurar(ContextoApp contexto) {
        this.contexto = contexto;
        servicio = new UsuarioService(contexto.getBaseDatos(), contexto.getSesion());
        recargar();
    }

    private void recargar() {
        try { tablaUsuarios.getItems().setAll(servicio.obtenerUsuarios()); }
        catch(Exception error) { Avisos.error(error); }
    }

    private Usuario seleccionado() {
        Usuario usuario = tablaUsuarios.getSelectionModel().getSelectedItem();
        if(usuario == null) { Avisos.error(new IllegalArgumentException("Selecciona un usuario.")); }
        return usuario;
    }

    @FXML
    private void onEditar() {
        Usuario usuario = seleccionado();
        if(usuario != null) { tfNombre.setText(usuario.getNombre()); cbRol.setValue(usuario.getRol()); }
    }

    @FXML
    private void onCambiarRol() {
        Usuario usuario = seleccionado();
        if(usuario != null && Avisos.confirmar("Cambiar el rol local de " + usuario.getNombre() + "?")) {
            try { servicio.cambiarRol(usuario.getId(), cbRol.getValue()); recargar(); lbMensaje.setText("Rol actualizado."); }
            catch(Exception error) { Avisos.error(error); }
        }
    }

    @FXML
    private void onGuardarNombre() {
        Usuario usuario = seleccionado();
        if(usuario != null) {
            try { servicio.cambiarNombre(usuario.getId(), tfNombre.getText()); recargar(); lbMensaje.setText("Nombre actualizado."); }
            catch(Exception error) { Avisos.error(error); }
        }
    }

    @FXML
    private void onEliminar() {
        Usuario usuario = seleccionado();
        if(usuario != null && Avisos.confirmar("Eliminar solo el registro local? La cuenta Firebase no se elimina. No se permite borrar autores con datos asociados.")) {
            try { servicio.eliminarUsuario(usuario.getId()); recargar(); lbMensaje.setText("Registro local eliminado."); }
            catch(Exception error) { Avisos.error(error); }
        }
    }

    @FXML private void onVolver() {
        try { contexto.mostrar("principal"); } catch(Exception error) { Avisos.error(error); }
    }
}

