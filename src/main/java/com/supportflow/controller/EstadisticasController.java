package com.supportflow.controller;

import com.supportflow.model.Estadisticas;
import com.supportflow.service.EstadisticasService;
import com.supportflow.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

// Mostrar comptadors calculats amb SQL
public class EstadisticasController {
    @FXML private Label lbTotalTickets;
    @FXML private Label lbAbiertos;
    @FXML private Label lbEnProgreso;
    @FXML private Label lbResueltos;
    @FXML private Label lbCerrados;
    @FXML private Label lbTotalEquipos;
    @FXML private Label lbActivos;
    @FXML private Label lbBaja;
    private ContextoApp contexto;

    public void configurar(ContextoApp contexto) {
        this.contexto = contexto;
        onActualizar();
    }

    @FXML
    private void onActualizar() {
        try {
            Estadisticas datos = new EstadisticasService(contexto.getBaseDatos(), contexto.getSesion()).obtenerEstadisticas();
            lbTotalTickets.setText(String.valueOf(datos.getTotalTickets()));
            lbAbiertos.setText(String.valueOf(datos.getAbiertos()));
            lbEnProgreso.setText(String.valueOf(datos.getEnProgreso()));
            lbResueltos.setText(String.valueOf(datos.getResueltos()));
            lbCerrados.setText(String.valueOf(datos.getCerrados()));
            lbTotalEquipos.setText(String.valueOf(datos.getTotalEquipos()));
            lbActivos.setText(String.valueOf(datos.getEquiposActivos()));
            lbBaja.setText(String.valueOf(datos.getEquiposBaja()));
        } catch(Exception error) { Avisos.error(error); }
    }

    @FXML private void onVolver() {
        try { contexto.mostrar("principal"); } catch(Exception error) { Avisos.error(error); }
    }
}

