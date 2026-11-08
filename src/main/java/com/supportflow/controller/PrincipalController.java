package com.supportflow.controller;

import com.supportflow.model.Usuario;
import com.supportflow.model.UsuarioRol;
import com.supportflow.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

// Navegar entre els moduls
public class PrincipalController {
    @FXML private Label lbUsuario;
    @FXML private Label lbModo;
    @FXML private Button btnUsuarios;
    @FXML private Button btnEstadisticas;
    private ContextoApp contexto;

    public void configurar(ContextoApp contexto) {
        this.contexto = contexto;
        try {
            Usuario usuario = contexto.usuarioActual();
            lbUsuario.setText(usuario.getNombre() + " - " + usuario.getRol());
            lbModo.setText(contexto.isDemo() ? "DEMO - datos ficticios separados. Sin autenticacion Firebase." : "Modo real - identidad verificada mediante Firebase");
            btnUsuarios.setDisable(usuario.getRol() != UsuarioRol.ADMIN);
            btnEstadisticas.setDisable(usuario.getRol() != UsuarioRol.ADMIN);
        } catch(Exception error) { Avisos.error(error); }
    }

    @FXML private void onTickets() { abrir("tickets"); }
    @FXML private void onInventario() { abrir("inventario"); }
    @FXML private void onUsuarios() { abrir("usuarios"); }
    @FXML private void onEstadisticas() { abrir("estadisticas"); }

    @FXML
    private void onCerrarSesion() {
        contexto.cerrarSesion();
        abrir("login");
    }

    private void abrir(String pantalla) {
        try { contexto.mostrar(pantalla); } catch(Exception error) { Avisos.error(error); }
    }
}

