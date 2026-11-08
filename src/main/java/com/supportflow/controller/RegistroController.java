package com.supportflow.controller;

import com.supportflow.util.*;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

// Registrar el correu sense escollir rols
public class RegistroController {
    @FXML private VBox panel;
    @FXML private TextField tfNombre;
    @FXML private TextField tfEmail;
    @FXML private PasswordField pfContrasena;
    @FXML private PasswordField pfConfirmacion;
    @FXML private Label lbMensaje;
    private ContextoApp contexto;

    public void configurar(ContextoApp contexto) {
        this.contexto = contexto;
    }

    @FXML
    private void onRegistrar() {
        final String nombre = tfNombre.getText();
        final String email = tfEmail.getText();
        final String contrasena = pfContrasena.getText();
        if(!contrasena.equals(pfConfirmacion.getText())) {
            Avisos.error(new IllegalArgumentException("Las contrasenas no coinciden."));
            return;
        }
        if(contexto.isDemo()) {
            Avisos.error(new IllegalArgumentException("El registro Firebase no esta disponible en DEMO."));
            return;
        }
        pfContrasena.clear();
        pfConfirmacion.clear();
        panel.setDisable(true);
        lbMensaje.setText("Creando cuenta...");
        Task<Void> tarea = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                contexto.getFirebase().registrarUsuario(nombre, email, contrasena);
                return null;
            }
        };
        tarea.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent evento) {
                panel.setDisable(false);
                lbMensaje.setText("Cuenta creada. Verifica el correo recibido antes de entrar. Despues vuelve al acceso.");
            }
        });
        tarea.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent evento) {
                panel.setDisable(false);
                lbMensaje.setText(tarea.getException().getMessage());
                Avisos.error((Exception)tarea.getException());
            }
        });
        // Evitar que la peticio bloquegi la finestra
        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    private void onVolver() {
        try { contexto.mostrar("login"); } catch(Exception error) { Avisos.error(error); }
    }
}

