package com.supportflow.controller;

import com.supportflow.model.Usuario;
import com.supportflow.util.*;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

// Pantalla d'acces real o demostracio
public class LoginController {
    @FXML private VBox panel;
    @FXML private VBox accesoReal;
    @FXML private VBox accesoDemo;
    @FXML private TextField tfEmail;
    @FXML private PasswordField pfContrasena;
    @FXML private ComboBox<Usuario> cbUsuarioDemo;
    @FXML private Label lbMensaje;
    private ContextoApp contexto;

    public void configurar(ContextoApp contexto) {
        this.contexto = contexto;
        accesoDemo.setVisible(contexto.isDemo());
        accesoDemo.setManaged(contexto.isDemo());
        accesoReal.setVisible(!contexto.isDemo());
        accesoReal.setManaged(!contexto.isDemo());
        try {
            if(contexto.isDemo()) {
                cbUsuarioDemo.getItems().setAll(contexto.usuariosDemo());
                cbUsuarioDemo.getSelectionModel().selectFirst();
                lbMensaje.setText("DEMO: usuarios ficticios y SQLite separado. No autentica con Firebase.");
            } else if(!contexto.getFirebase().estaConfigurado()) {
                lbMensaje.setText("Falta firebase.apiKey en config/firebase.properties. Consulta docs/FIREBASE.md o inicia la DEMO.");
            } else {
                lbMensaje.setText("Accede con una cuenta de Firebase y correo verificado.");
            }
        } catch(Exception error) { Avisos.error(error); }
    }

    @FXML
    private void onIniciarSesion() {
        final String email = tfEmail.getText();
        final String contrasena = pfContrasena.getText();
        pfContrasena.clear();
        Task<Void> tarea = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                contexto.autenticar(email, contrasena);
                return null;
            }
        };
        ejecutarTarea(tarea, true, "");
    }

    @FXML
    private void onRecuperar() {
        final String email = tfEmail.getText();
        Task<Void> tarea = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                contexto.getFirebase().recuperarContrasena(email);
                return null;
            }
        };
        ejecutarTarea(tarea, false, "Solicitud de recuperacion enviada. Revisa tu correo.");
    }

    @FXML
    private void onReenviar() {
        Task<Void> tarea = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                contexto.getFirebase().enviarVerificacion();
                return null;
            }
        };
        ejecutarTarea(tarea, false, "Solicitud de verificacion enviada. Revisa tu correo.");
    }

    @FXML
    private void onComprobar() {
        Task<Void> tarea = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                contexto.comprobarCorreoYEntrar();
                return null;
            }
        };
        ejecutarTarea(tarea, true, "");
    }

    @FXML
    private void onRegistro() {
        try { contexto.mostrar("registro"); } catch(Exception error) { Avisos.error(error); }
    }

    @FXML
    private void onEntrarDemo() {
        try {
            contexto.entrarDemo(cbUsuarioDemo.getValue());
            contexto.mostrar("principal");
        } catch(Exception error) { Avisos.error(error); }
    }

    private void ejecutarTarea(Task<Void> tarea, final boolean entrar, final String mensaje) {
        panel.setDisable(true);
        lbMensaje.setText("Conectando...");
        tarea.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent evento) {
                panel.setDisable(false);
                lbMensaje.setText(mensaje);
                if(entrar) {
                    try { contexto.mostrar("principal"); } catch(Exception error) { Avisos.error(error); }
                }
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
        // La xarxa treballa fora del fil de JavaFX
        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }
}

