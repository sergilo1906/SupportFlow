package com.supportflow.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

// Mostrar missatges i demanar confirmacio
public class Avisos {
    public static void error(Exception error) {
        String mensaje = error.getMessage();
        if(error instanceof java.sql.SQLException) {
            mensaje = "No se puede completar la operacion en SQLite. Comprueba datos unicos y relaciones con otros registros.";
        }
        if(mensaje == null || mensaje.isBlank()) { mensaje = "No se ha podido completar la operacion."; }
        Alert aviso = new Alert(Alert.AlertType.ERROR);
        aviso.setTitle("SupportFlow");
        aviso.setHeaderText("Revisa la operacion");
        aviso.setContentText(mensaje);
        aviso.showAndWait();
    }

    public static boolean confirmar(String mensaje) {
        Alert aviso = new Alert(Alert.AlertType.CONFIRMATION);
        aviso.setTitle("Confirmacion");
        aviso.setHeaderText(null);
        aviso.setContentText(mensaje);
        java.util.Optional<ButtonType> respuesta = aviso.showAndWait();
        if(respuesta.isPresent() && respuesta.get() == ButtonType.OK) { return true; }
        return false;
    }
}

