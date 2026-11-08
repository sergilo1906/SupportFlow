package com.supportflow;

import com.supportflow.util.Avisos;
import com.supportflow.util.ContextoApp;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.stage.Stage;

// Iniciar la finestra i carregar el primer FXML
public class SupportFlowApp extends Application {
    private ContextoApp contexto;

    @Override
    public void start(Stage ventana) {
        try {
            boolean demo = Boolean.parseBoolean(System.getProperty("supportflow.demo", "false"));
            Path archivoDemo = Path.of(System.getProperty("supportflow.demo.db", "data/supportflow-demo.db"));
            contexto = new ContextoApp(ventana, demo, archivoDemo);
            contexto.mostrar("login");
        } catch(Exception error) {
            Avisos.error(error);
            javafx.application.Platform.exit();
        }
    }

    @Override
    public void stop() {
        if(contexto != null) { contexto.cerrarSesion(); }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
