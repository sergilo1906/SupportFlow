package com.supportflow.auth;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.supportflow.dao.UsuarioDAO;
import com.supportflow.model.UsuarioRol;
import com.supportflow.service.AutenticacionService;
import com.supportflow.service.Sesion;
import com.supportflow.util.ConexionBD;
import com.supportflow.util.EsquemaBD;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

// Servidor local de proves: mai es connecta a Firebase real
class FirebaseAuthTest {
    @TempDir
    Path carpetaTemporal;
    private HttpServer servidor;
    private FirebaseAuth firebase;
    private ArrayList<String> respuestas;
    private ArrayList<JsonObject> solicitudes;
    private ArrayList<String> rutas;
    private int codigo;
    private ConexionBD baseDatos;

    @BeforeEach
    void prepararServidorSimulado() throws Exception {
        respuestas = new ArrayList<String>();
        solicitudes = new ArrayList<JsonObject>();
        rutas = new ArrayList<String>();
        codigo = 200;
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange llamada) throws IOException {
                solicitudes.add(JsonParser.parseString(new String(llamada.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject());
                rutas.add(llamada.getRequestURI().getPath());
                String texto = "{}";
                if(!respuestas.isEmpty()) { texto = respuestas.remove(0); }
                byte[] contenido = texto.getBytes(StandardCharsets.UTF_8);
                llamada.getResponseHeaders().add("Content-Type", "application/json");
                llamada.sendResponseHeaders(codigo, contenido.length);
                llamada.getResponseBody().write(contenido);
                llamada.close();
            }
        });
        servidor.start();
        firebase = new FirebaseAuth("clave-simulada", URI.create("http://127.0.0.1:" + servidor.getAddress().getPort() + "/"));
        baseDatos = new ConexionBD(carpetaTemporal.resolve("auth.db"));
        EsquemaBD.inicializar(baseDatos);
    }

    @AfterEach
    void cerrarServidor() {
        if(servidor != null) { servidor.stop(0); }
    }

    private void respuestaAcceso(boolean verificado) {
        respuestas.add("{\"idToken\":\"token-simulado\",\"localId\":\"uid-prueba\",\"email\":\"prueba@example.invalid\",\"expiresIn\":\"3600\"}");
        respuestas.add("{\"users\":[{\"localId\":\"uid-prueba\",\"email\":\"prueba@example.invalid\",\"displayName\":\"Persona de prueba\",\"emailVerified\":" + verificado + "}]}");
    }

    @Test
    void detectaConfiguracionAusenteSinLlamadasReales() {
        FirebaseAuth sinConfigurar = new FirebaseAuth("");
        AuthException error = assertThrows(AuthException.class, () -> sinConfigurar.recuperarContrasena("prueba@example.invalid"));
        assertTrue(error.getMessage().contains("firebase.apiKey"));
        assertEquals(0, solicitudes.size());
    }

    @Test
    void validaEmailYContrasenaAntesDeEnviar() {
        assertThrows(AuthException.class, () -> firebase.iniciarSesion("incorrecto", "clave"));
        assertThrows(AuthException.class, () -> firebase.iniciarSesion("prueba@example.invalid", ""));
        assertThrows(AuthException.class, () -> firebase.registrarUsuario("Nombre", "prueba@example.invalid", "123"));
        assertEquals(0, solicitudes.size());
    }

    @Test
    void registraPerfilYEnviaVerificacionConJson() throws Exception {
        respuestas.add("{\"idToken\":\"token-simulado\",\"localId\":\"uid-prueba\",\"email\":\"prueba@example.invalid\",\"expiresIn\":3600}");
        respuestas.add("{}");
        respuestas.add("{}");
        firebase.registrarUsuario("Nombre con \"comillas\"", "prueba@example.invalid", "clave-solo-de-prueba");
        assertEquals("/accounts:signUp", rutas.get(0));
        assertEquals("/accounts:update", rutas.get(1));
        assertEquals("Nombre con \"comillas\"", solicitudes.get(1).get("displayName").getAsString());
        assertEquals("VERIFY_EMAIL", solicitudes.get(2).get("requestType").getAsString());
        assertFalse(firebase.isVerificado());
        assertEquals(0, new UsuarioDAO(baseDatos).obtenerUsuarios().size());
    }

    @Test
    void usuarioVerificadoEntraComoEmpleadoSinDuplicarse() throws Exception {
        respuestaAcceso(true);
        AutenticacionService servicio = new AutenticacionService(baseDatos, firebase, "");
        Sesion sesion = servicio.iniciarSesion("prueba@example.invalid", "clave-de-prueba");
        assertEquals(UsuarioRol.EMPLEADO, sesion.getUsuario().getRol());
        assertFalse(sesion.isDemo());
        respuestaAcceso(true);
        servicio.iniciarSesion("prueba@example.invalid", "clave-de-prueba");
        assertEquals(1, new UsuarioDAO(baseDatos).obtenerUsuarios().size());
    }

    @Test
    void noEntraNiCreaRegistroLocalSiNoEstaVerificado() throws Exception {
        respuestaAcceso(false);
        AutenticacionService servicio = new AutenticacionService(baseDatos, firebase, "uid-prueba");
        assertThrows(AuthException.class, () -> servicio.iniciarSesion("prueba@example.invalid", "clave-de-prueba"));
        assertEquals(0, new UsuarioDAO(baseDatos).obtenerUsuarios().size());
        respuestas.add("{}");
        firebase.enviarVerificacion();
        assertEquals("VERIFY_EMAIL", solicitudes.get(2).get("requestType").getAsString());
    }

    @Test
    void primerAdminRequiereSuUidExplicito() throws Exception {
        respuestaAcceso(true);
        Sesion normal = new AutenticacionService(baseDatos, firebase, "otro-uid").iniciarSesion("prueba@example.invalid", "clave-de-prueba");
        assertEquals(UsuarioRol.EMPLEADO, normal.getUsuario().getRol());
        respuestaAcceso(true);
        Sesion admin = new AutenticacionService(baseDatos, firebase, "uid-prueba").iniciarSesion("prueba@example.invalid", "clave-de-prueba");
        assertEquals(UsuarioRol.ADMIN, admin.getUsuario().getRol());
    }

    @Test
    void solicitaRecuperacionDeContrasena() throws Exception {
        respuestas.add("{}");
        firebase.recuperarContrasena("prueba@example.invalid");
        assertEquals("PASSWORD_RESET", solicitudes.get(0).get("requestType").getAsString());
        assertEquals("prueba@example.invalid", solicitudes.get(0).get("email").getAsString());
    }

    @Test
    void traduceErroresSinMostrarTokensNiRespuestasCrudas() {
        String[] errores = {"EMAIL_EXISTS", "INVALID_EMAIL", "WEAK_PASSWORD : minimo", "INVALID_PASSWORD", "INVALID_LOGIN_CREDENTIALS", "USER_DISABLED", "TOO_MANY_ATTEMPTS_TRY_LATER", "OPERATION_NOT_ALLOWED", "API_KEY_INVALID"};
        for(int i = 0; i < errores.length; i++) {
            codigo = 400;
            JsonObject detalle = new JsonObject();
            detalle.addProperty("message", errores[i]);
            JsonObject respuesta = new JsonObject();
            respuesta.add("error", detalle);
            respuestas.add(respuesta.toString());
            AuthException error = assertThrows(AuthException.class, () -> firebase.recuperarContrasena("prueba@example.invalid"));
            assertFalse(error.getMessage().contains("token-simulado"));
            assertFalse(error.getMessage().contains("{\"error\""));
        }
    }

    @Test
    void cerrarSesionDescartaLosDatosEnMemoria() throws Exception {
        respuestaAcceso(true);
        firebase.iniciarSesion("prueba@example.invalid", "clave-de-prueba");
        firebase.cerrarSesion();
        assertNull(firebase.getUid());
        assertNull(firebase.getCaducidad());
        assertFalse(firebase.isVerificado());
        assertThrows(AuthException.class, () -> firebase.enviarVerificacion());
    }

    @Test
    void respuestaDePerfilIncoherenteNoAbreSesion() {
        respuestas.add("{\"idToken\":\"token-simulado\",\"localId\":\"uid-prueba\",\"email\":\"prueba@example.invalid\",\"expiresIn\":3600}");
        respuestas.add("{\"users\":[{\"localId\":\"uid-distinto\",\"emailVerified\":true}]}");
        assertThrows(AuthException.class, () -> firebase.iniciarSesion("prueba@example.invalid", "clave-de-prueba"));
        assertNull(firebase.getUid());
    }

    @Test
    void informaDeRespuestaInvalidaYDeErrorDeConexion() {
        respuestas.add("Esto no es JSON");
        assertThrows(AuthException.class, () -> firebase.recuperarContrasena("prueba@example.invalid"));
        servidor.stop(0);
        AuthException error = assertThrows(AuthException.class, () -> firebase.recuperarContrasena("prueba@example.invalid"));
        assertTrue(error.getMessage().contains("conectar"));
    }

    @Test
    void tokenCaducadoDelServidorCierraLaSesion() throws Exception {
        respuestaAcceso(true);
        firebase.iniciarSesion("prueba@example.invalid", "clave-de-prueba");
        codigo = 400;
        respuestas.add("{\"error\":{\"message\":\"TOKEN_EXPIRED\"}}");
        assertThrows(AuthException.class, () -> firebase.enviarVerificacion());
        assertNull(firebase.getUid());
    }
}
