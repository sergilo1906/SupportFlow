package com.supportflow.controller;

import com.supportflow.model.*;
import com.supportflow.service.*;
import com.supportflow.util.*;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

// Formulari de nova incidencia
public class FormularioTicketController {
    @FXML private TextField tfTitulo;
    @FXML private TextArea taDescripcion;
    @FXML private ComboBox<TicketPriority> cbPrioridad;
    @FXML private ComboBox<Equipo> cbEquipo;
    private ContextoApp contexto;

    @FXML
    private void initialize() {
        TicketPriority[] prioridades = TicketPriority.values();
        for(int i = 0; i < prioridades.length; i++) { cbPrioridad.getItems().add(prioridades[i]); }
        cbPrioridad.setValue(TicketPriority.MEDIUM);
    }

    public void configurar(ContextoApp contexto) {
        this.contexto = contexto;
        try {
            ArrayList<Equipo> equipos = new EquipoService(contexto.getBaseDatos(), contexto.getSesion()).obtenerEquipos();
            cbEquipo.getItems().add(null);
            for(int i = 0; i < equipos.size(); i++) {
                if("ACTIVO".equals(equipos.get(i).getEstado())) { cbEquipo.getItems().add(equipos.get(i)); }
            }
        } catch(Exception error) { Avisos.error(error); }
    }

    @FXML
    private void onGuardar() {
        try {
            Integer equipoId = null;
            if(cbEquipo.getValue() != null) { equipoId = cbEquipo.getValue().getId(); }
            new TicketService(contexto.getBaseDatos(), contexto.getSesion()).crearTicket(
                    tfTitulo.getText(), taDescripcion.getText(), cbPrioridad.getValue(), equipoId);
            contexto.mostrar("tickets");
        } catch(Exception error) { Avisos.error(error); }
    }

    @FXML private void onSinEquipo() { cbEquipo.setValue(null); }
    @FXML private void onVolver() {
        try { contexto.mostrar("tickets"); } catch(Exception error) { Avisos.error(error); }
    }
}

