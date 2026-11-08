package com.supportflow.util;

import com.supportflow.auth.*;
import com.supportflow.controller.*;
import com.supportflow.dao.UsuarioDAO;
import com.supportflow.model.Usuario;
import com.supportflow.service.*;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Compartir la sessio i la finestra sense frameworks
public class ContextoApp {
    private Stage ventana;
    private ConexionBD baseDatos;
    private FirebaseAuth firebase;
    private Sesion sesion;
    private boolean demo;
    private String adminInicialUid;
    private int ticketSeleccionado;

    public ContextoApp(Stage ventana, boolean demo, Path archivoDemo) throws Exception {
        this.ventana = ventana;
        this.demo = demo;
        if(demo) {
            Path real = Path.of("data", "supportflow.db").toAbsolutePath().normalize();
            if(archivoDemo.toAbsolutePath().normalize().equals(real)) {
                throw new IllegalArgumentException("La DEMO no puede utilizar la base real.");
            }
            baseDatos = new ConexionBD(archivoDemo);
            DemoDatos.preparar(baseDatos);
            firebase = new FirebaseAuth("");
        } else {
            baseDatos = new ConexionBD();
            EsquemaBD.inicializar(baseDatos);
            Properties configuracion = new Properties();
            Path archivo = Path.of("config", "firebase.properties");
            if(Files.exists(archivo)) {
                try(InputStream entrada = Files.newInputStream(archivo)) { configuracion.load(entrada); }
            }
            firebase = new FirebaseAuth(configuracion.getProperty("firebase.apiKey", "").trim());
            adminInicialUid = configuracion.getProperty("admin.uid", "").trim();
        }
    }

    public boolean isDemo() { return demo; }
    public ConexionBD getBaseDatos() { return baseDatos; }
    public FirebaseAuth getFirebase() { return firebase; }
    public Sesion getSesion() { return sesion; }
    public int getTicketSeleccionado() { return ticketSeleccionado; }
    public void setTicketSeleccionado(int id) { ticketSeleccionado = id; }

    public Usuario usuarioActual() throws SQLException {
        return Permisos.usuarioActual(baseDatos, sesion);
    }

    public void autenticar(String email, String contrasena) throws Exception {
        if(demo) { throw new AuthException("La DEMO no utiliza Firebase."); }
        sesion = new AutenticacionService(baseDatos, firebase, adminInicialUid).iniciarSesion(email, contrasena);
    }

    public void comprobarCorreoYEntrar() throws Exception {
        if(demo) { throw new AuthException("La DEMO no utiliza Firebase."); }
        firebase.comprobarVerificacion();
        sesion = new AutenticacionService(baseDatos, firebase, adminInicialUid).crearSesionVerificada();
    }

    public ArrayList<Usuario> usuariosDemo() throws SQLException {
        if(!demo) { throw new SecurityException("Los usuarios DEMO no estan disponibles en modo real."); }
        return new UsuarioDAO(baseDatos).obtenerUsuarios();
    }

    public void entrarDemo(Usuario elegido) throws SQLException {
        if(!demo || elegido == null) { throw new SecurityException("Selecciona un usuario DEMO."); }
        Usuario usuario = new UsuarioDAO(baseDatos).obtenerPorId(elegido.getId());
        if(usuario == null || !usuario.getFirebaseUid().startsWith("demo:")) {
            throw new SecurityException("El usuario no pertenece a la DEMO.");
        }
        sesion = new Sesion(usuario, true, null);
    }

    public void cerrarSesion() {
        if(sesion != null) { sesion.cerrarSesion(); }
        sesion = null;
        firebase.cerrarSesion();
    }

    public void mostrar(String pantalla) throws IOException, SQLException {
        if(!pantalla.equals("login") && !pantalla.equals("registro")) {
            try {
                usuarioActual();
            } catch(SecurityException error) {
                cerrarSesion();
                mostrar("login");
                Avisos.error(error);
                return;
            }
        }
        FXMLLoader cargador = new FXMLLoader(getClass().getResource("/fxml/" + pantalla + ".fxml"));
        Parent raiz = cargador.load();
        Object controlador = cargador.getController();
        // Cada controlador rep la mateixa sessio
        if(controlador instanceof LoginController) { ((LoginController)controlador).configurar(this); }
        else if(controlador instanceof RegistroController) { ((RegistroController)controlador).configurar(this); }
        else if(controlador instanceof PrincipalController) { ((PrincipalController)controlador).configurar(this); }
        else if(controlador instanceof TicketController) { ((TicketController)controlador).configurar(this); }
        else if(controlador instanceof FormularioTicketController) { ((FormularioTicketController)controlador).configurar(this); }
        else if(controlador instanceof DetalleTicketController) { ((DetalleTicketController)controlador).configurar(this); }
        else if(controlador instanceof InventarioController) { ((InventarioController)controlador).configurar(this); }
        else if(controlador instanceof UsuariosController) { ((UsuariosController)controlador).configurar(this); }
        else if(controlador instanceof EstadisticasController) { ((EstadisticasController)controlador).configurar(this); }
        Scene escena = new Scene(raiz, 1120, 780);
        escena.getStylesheets().add(getClass().getResource("/css/supportflow.css").toExternalForm());
        ventana.setTitle(demo ? "SupportFlow - DEMO (datos separados)" : "SupportFlow");
        ventana.setScene(escena);
        ventana.setMinWidth(1000);
        ventana.setMinHeight(700);
        ventana.show();
    }
}

