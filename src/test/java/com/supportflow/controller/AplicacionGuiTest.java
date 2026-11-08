package com.supportflow.controller;

import static org.junit.jupiter.api.Assertions.*;
import com.supportflow.dao.*;
import com.supportflow.model.*;
import com.supportflow.util.ContextoApp;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

// Polsar botons reals amb una BD DEMO temporal
class AplicacionGuiTest {
    @TempDir
    Path carpetaTemporal;
    private Stage ventana;
    private ContextoApp contexto;
    private String ultimaAlerta;
    private int numeroAccion;

    @BeforeAll
    static void iniciarJavaFX() throws Exception {
        CountDownLatch inicio = new CountDownLatch(1);
        Platform.startup(new Runnable() {
            @Override
            public void run() {
                Platform.setImplicitExit(false);
                inicio.countDown();
            }
        });
        assertTrue(inicio.await(20, TimeUnit.SECONDS), "JavaFX no se ha iniciado");
    }

    @BeforeEach
    void abrirDemoTemporal() throws Exception {
        ejecutarEnJavaFX(new Runnable() {
            @Override
            public void run() {
                try {
                    ventana = new Stage();
                    contexto = new ContextoApp(ventana, true, carpetaTemporal.resolve("gui-demo.db"));
                    contexto.mostrar("login");
                } catch(Exception error) { throw new RuntimeException(error); }
            }
        });
    }

    @AfterEach
    void cerrarVentana() throws Exception {
        ejecutarEnJavaFX(new Runnable() {
            @Override
            public void run() {
                if(contexto != null) { contexto.cerrarSesion(); }
                if(ventana != null) { ventana.close(); }
            }
        });
    }

    @AfterAll
    static void cerrarJavaFX() { Platform.exit(); }

    @Test
    void recorridoCompletoDeTicketsInventarioUsuariosYEstadisticas() throws Exception {
        ejecutarEnJavaFX(new Runnable() {
            @Override
            public void run() {
                elegirUsuarioDemo("demo:admin");
                pulsar("btnEntrarDemo", false);
                pulsar("btnTickets", false);
                pulsar("btnCrear", false);
                texto("tfTitulo").setText("Incidencia creada desde FXML");
                area("taDescripcion").setText("Descripcion escrita en la ventana");
                combo("cbPrioridad").setValue(TicketPriority.HIGH);
                combo("cbEquipo").getSelectionModel().select(1);
                pulsar("btnGuardar", false);
                TableView<Ticket> tabla = tablaTickets();
                assertEquals(3, tabla.getItems().size());
                tabla.getSelectionModel().selectFirst();
                assertEquals("Incidencia creada desde FXML", tabla.getSelectionModel().getSelectedItem().getTitulo());
                pulsar("btnConsultar", false);
                area("taComentario").setText("Comentario escrito desde JavaFX");
                pulsar("btnComentar", false);
                assertEquals(1, lista("listaComentarios").getItems().size());
                combo("cbTecnico").getSelectionModel().select(1);
                pulsar("btnAsignar", false);
                combo("cbEstado").setValue(TicketStatus.IN_PROGRESS);
                pulsar("btnCambiarEstado", false);
                assertEquals(3, lista("listaHistorial").getItems().size());
                assertTrue(etiqueta("lbDatos").getText().contains("IN_PROGRESS"));
                pulsar("btnVolver", false);
                tablaTickets().getSelectionModel().selectFirst();
                pulsar("btnEliminar", true);
                assertEquals(2, tablaTickets().getItems().size());
                pulsar("btnVolver", false);
                pulsar("btnInventario", false);
                texto("tfNombre").setText("Equipo GUI");
                texto("tfTipo").setText("Portatil");
                texto("tfMarca").setText("Marca");
                texto("tfModelo").setText("Modelo inicial");
                texto("tfSerie").setText("GUI-001");
                elegirUsuarioAsignado("demo:empleado");
                pulsar("btnGuardar", false);
                assertEquals(2, tablaEquipos().getItems().size());
                texto("tfModelo").setText("Modelo editado en la ventana");
                pulsar("btnGuardar", false);
                tablaEquipos().getSelectionModel().select(1);
                pulsar("btnEditar", false);
                assertEquals("Modelo editado en la ventana", texto("tfModelo").getText());
                pulsar("btnBaja", true);
                assertEquals("BAJA", tablaEquipos().getItems().get(1).getEstado());
                pulsar("btnVolver", false);
                pulsar("btnEstadisticas", false);
                assertEquals("2", etiqueta("lbTotalTickets").getText());
                assertEquals("2", etiqueta("lbTotalEquipos").getText());
                assertEquals("1", etiqueta("lbActivos").getText());
                assertEquals("1", etiqueta("lbBaja").getText());
                pulsar("btnVolver", false);
                pulsar("btnUsuarios", false);
                seleccionarUsuarioLocal("demo:otro");
                combo("cbRol").setValue(UsuarioRol.TECNICO);
                pulsar("btnCambiarRol", true);
                seleccionarUsuarioLocal("demo:otro");
                texto("tfNombre").setText("Nombre cambiado en GUI");
                pulsar("btnGuardarNombre", false);
                pulsar("btnVolver", false);
                pulsar("btnCerrarSesion", false);
                assertNotNull(ventana.getScene().lookup("#cbUsuarioDemo"));
                assertNull(ultimaAlerta);
            }
        });
        assertEquals(UsuarioRol.TECNICO, new UsuarioDAO(contexto.getBaseDatos()).obtenerPorUid("demo:otro").getRol());
        assertEquals("Nombre cambiado en GUI", new UsuarioDAO(contexto.getBaseDatos()).obtenerPorUid("demo:otro").getNombre());
        assertEquals(2, new TicketDAO(contexto.getBaseDatos()).obtenerTickets().size());
        assertNull(new TicketDAO(contexto.getBaseDatos()).obtenerPorId(3));
        assertEquals(0, new ComentarioDAO(contexto.getBaseDatos()).obtenerComentarios(3).size());
        ejecutarEnJavaFX(new Runnable() {
            @Override
            public void run() {
                try {
                    ventana.close();
                    ventana = new Stage();
                    contexto = new ContextoApp(ventana, true, carpetaTemporal.resolve("gui-demo.db"));
                    contexto.mostrar("login");
                    elegirUsuarioDemo("demo:admin");
                    pulsar("btnEntrarDemo", false);
                    pulsar("btnInventario", false);
                    assertEquals(2, tablaEquipos().getItems().size());
                    assertEquals("BAJA", tablaEquipos().getItems().get(1).getEstado());
                } catch(Exception error) { throw new RuntimeException(error); }
            }
        });
    }

    @Test
    void empleadoSoloVeSusDatosYNoTieneBotonesDeAdministracion() throws Exception {
        ejecutarEnJavaFX(new Runnable() {
            @Override
            public void run() {
                elegirUsuarioDemo("demo:empleado");
                pulsar("btnEntrarDemo", false);
                assertTrue(boton("btnUsuarios").isDisabled());
                assertTrue(boton("btnEstadisticas").isDisabled());
                pulsar("btnTickets", false);
                assertEquals(1, tablaTickets().getItems().size());
                assertTrue(boton("btnEliminar").isDisabled());
                combo("cbPrioridad").setValue(TicketPriority.LOW);
                pulsar("btnFiltrar", false);
                assertEquals(0, tablaTickets().getItems().size());
                pulsar("btnLimpiar", false);
                tablaTickets().getSelectionModel().selectFirst();
                pulsar("btnConsultar", false);
                assertTrue(boton("btnCambiarEstado").isDisabled());
                assertTrue(boton("btnAsignar").isDisabled());
                area("taComentario").setText("Comentario propio");
                pulsar("btnComentar", false);
                assertEquals(2, lista("listaComentarios").getItems().size());
                pulsar("btnVolver", false);
                pulsar("btnVolver", false);
                pulsar("btnInventario", false);
                assertEquals(1, tablaEquipos().getItems().size());
                assertTrue(boton("btnGuardar").isDisabled());
                assertTrue(boton("btnBaja").isDisabled());
                assertNull(ultimaAlerta);
            }
        });
    }

    @Test
    void formularioDeRegistroCargaYValidaConfirmacionSinRed() throws Exception {
        ejecutarEnJavaFX(new Runnable() {
            @Override
            public void run() {
                try { contexto.mostrar("registro"); } catch(Exception error) { throw new RuntimeException(error); }
                ((PasswordField)ventana.getScene().lookup("#pfContrasena")).setText("solo-prueba");
                ((PasswordField)ventana.getScene().lookup("#pfConfirmacion")).setText("distinta");
                pulsar("btnRegistrar", false);
                assertEquals("Las contrasenas no coinciden.", ultimaAlerta);
            }
        });
    }

    @Test
    void tecnicoGestionaAsignadosEInventarioSinPermisosDeAdmin() throws Exception {
        ejecutarEnJavaFX(new Runnable() {
            @Override
            public void run() {
                elegirUsuarioDemo("demo:tecnico");
                pulsar("btnEntrarDemo", false);
                assertTrue(boton("btnUsuarios").isDisabled());
                pulsar("btnTickets", false);
                assertEquals(1, tablaTickets().getItems().size());
                assertTrue(boton("btnEliminar").isDisabled());
                tablaTickets().getSelectionModel().selectFirst();
                pulsar("btnConsultar", false);
                assertFalse(boton("btnCambiarEstado").isDisabled());
                assertTrue(boton("btnAsignar").isDisabled());
                combo("cbEstado").setValue(TicketStatus.IN_PROGRESS);
                pulsar("btnCambiarEstado", false);
                assertTrue(etiqueta("lbDatos").getText().contains("IN_PROGRESS"));
                pulsar("btnVolver", false);
                pulsar("btnVolver", false);
                pulsar("btnInventario", false);
                tablaEquipos().getSelectionModel().selectFirst();
                pulsar("btnEditar", false);
                assertFalse(boton("btnGuardar").isDisabled());
                assertTrue(boton("btnNuevo").isDisabled());
                assertTrue(boton("btnBaja").isDisabled());
                texto("tfModelo").setText("Revisado por tecnico");
                pulsar("btnGuardar", false);
                assertEquals("Revisado por tecnico", tablaEquipos().getItems().get(0).getModelo());
                assertNull(ultimaAlerta);
            }
        });
    }

    private void elegirUsuarioDemo(String uid) {
        ComboBox<Usuario> usuarios = combo("cbUsuarioDemo");
        for(int i = 0; i < usuarios.getItems().size(); i++) {
            if(usuarios.getItems().get(i).getFirebaseUid().equals(uid)) { usuarios.getSelectionModel().select(i); return; }
        }
        fail("Falta el usuario DEMO " + uid);
    }

    private void elegirUsuarioAsignado(String uid) {
        ComboBox<Usuario> usuarios = combo("cbUsuario");
        for(int i = 0; i < usuarios.getItems().size(); i++) {
            Usuario usuario = usuarios.getItems().get(i);
            if(usuario != null && usuario.getFirebaseUid().equals(uid)) { usuarios.getSelectionModel().select(i); return; }
        }
        fail("Falta el usuario asignable");
    }

    @SuppressWarnings("unchecked")
    private void seleccionarUsuarioLocal(String uid) {
        TableView<Usuario> usuarios = (TableView<Usuario>)ventana.getScene().lookup("#tablaUsuarios");
        for(int i = 0; i < usuarios.getItems().size(); i++) {
            if(usuarios.getItems().get(i).getFirebaseUid().equals(uid)) { usuarios.getSelectionModel().select(i); return; }
        }
        fail("Falta el usuario local");
    }

    private void pulsar(String id, final boolean confirmar) {
        numeroAccion++;
        final int accionActual = numeroAccion;
        // Cerrar solo los dialogos de este proceso de pruebas
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(accionActual != numeroAccion) { return; }
                ObservableList<Window> abiertas = Window.getWindows();
                ArrayList<Window> copia = new ArrayList<Window>(abiertas);
                for(int i = 0; i < copia.size(); i++) {
                    Window otra = copia.get(i);
                    if(otra != ventana && otra.getScene() != null && otra.getScene().getRoot() instanceof DialogPane) {
                        DialogPane dialogo = (DialogPane)otra.getScene().getRoot();
                        if(dialogo.getButtonTypes().contains(ButtonType.CANCEL)) {
                            if(confirmar) { ((Button)dialogo.lookupButton(ButtonType.OK)).fire(); }
                            else { ((Button)dialogo.lookupButton(ButtonType.CANCEL)).fire(); }
                        } else {
                            ultimaAlerta = dialogo.getContentText();
                            ((Button)dialogo.lookupButton(ButtonType.OK)).fire();
                        }
                    }
                }
            }
        });
        boton(id).fire();
    }

    private Button boton(String id) { return (Button)ventana.getScene().lookup("#" + id); }
    private TextField texto(String id) { return (TextField)ventana.getScene().lookup("#" + id); }
    private TextArea area(String id) { return (TextArea)ventana.getScene().lookup("#" + id); }
    private Label etiqueta(String id) { return (Label)ventana.getScene().lookup("#" + id); }
    @SuppressWarnings("unchecked")
    private <T> ComboBox<T> combo(String id) { return (ComboBox<T>)ventana.getScene().lookup("#" + id); }
    @SuppressWarnings("unchecked")
    private TableView<Ticket> tablaTickets() { return (TableView<Ticket>)ventana.getScene().lookup("#tablaTickets"); }
    @SuppressWarnings("unchecked")
    private TableView<Equipo> tablaEquipos() { return (TableView<Equipo>)ventana.getScene().lookup("#tablaEquipos"); }
    @SuppressWarnings("unchecked")
    private ListView<String> lista(String id) { return (ListView<String>)ventana.getScene().lookup("#" + id); }

    private void ejecutarEnJavaFX(final Runnable accion) throws Exception {
        CountDownLatch fin = new CountDownLatch(1);
        AtomicReference<Throwable> fallo = new AtomicReference<Throwable>();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try { accion.run(); }
                catch(Throwable error) { fallo.set(error); }
                finally { fin.countDown(); }
            }
        });
        assertTrue(fin.await(30, TimeUnit.SECONDS), "La accion JavaFX no ha terminado");
        if(fallo.get() != null) { throw new AssertionError("Fallo en la ventana JavaFX", fallo.get()); }
    }
}
