package com.supportflow.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

// Peticions REST oficials; contrasenyes i tokens nomes en memoria
public class FirebaseAuth {
    private String apiKey;
    private URI direccion;
    private HttpClient cliente;
    private String idToken;
    private String uid;
    private String email;
    private String nombre;
    private boolean verificado;
    private Instant caducidad;

    public FirebaseAuth(String apiKey) {
        this(apiKey, URI.create("https://identitytoolkit.googleapis.com/v1/"));
    }

    // Direccio alternativa nomes per a les proves del mateix paquet
    FirebaseAuth(String apiKey, URI direccion) {
        this.apiKey = apiKey;
        this.direccion = direccion;
        this.cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getNombre() { return nombre; }
    public boolean isVerificado() { return verificado; }
    public Instant getCaducidad() { return caducidad; }
    public boolean estaConfigurado() { return apiKey != null && !apiKey.isBlank(); }

    public void registrarUsuario(String nombre, String email, String contrasena) throws AuthException {
        validarEmail(email);
        if(nombre == null || nombre.isBlank()) { throw new AuthException("Escribe tu nombre."); }
        if(contrasena == null || contrasena.length() < 6) {
            throw new AuthException("La contrasena debe tener al menos seis caracteres y cumplir la politica de Firebase.");
        }
        cerrarSesion();
        JsonObject datos = credenciales(email, contrasena);
        cargarSesion(peticion("accounts:signUp", datos));
        this.nombre = nombre.trim();
        try {
            JsonObject perfil = new JsonObject();
            perfil.addProperty("idToken", idToken);
            perfil.addProperty("displayName", this.nombre);
            peticion("accounts:update", perfil);
            enviarVerificacion();
        } catch(AuthException error) {
            throw new AuthException("La cuenta esta creada, pero falta completar el perfil o enviar la verificacion. Inicia sesion y reenvia el correo.");
        }
    }

    public void iniciarSesion(String email, String contrasena) throws AuthException {
        validarEmail(email);
        if(contrasena == null || contrasena.isBlank()) { throw new AuthException("Escribe la contrasena."); }
        cerrarSesion();
        cargarSesion(peticion("accounts:signInWithPassword", credenciales(email, contrasena)));
        comprobarVerificacion();
    }

    public void enviarVerificacion() throws AuthException {
        comprobarToken();
        JsonObject datos = new JsonObject();
        datos.addProperty("requestType", "VERIFY_EMAIL");
        datos.addProperty("idToken", idToken);
        peticion("accounts:sendOobCode", datos);
    }

    public boolean comprobarVerificacion() throws AuthException {
        comprobarToken();
        JsonObject datos = new JsonObject();
        datos.addProperty("idToken", idToken);
        JsonObject respuesta = peticion("accounts:lookup", datos);
        try {
            JsonArray usuarios = respuesta.getAsJsonArray("users");
            if(usuarios == null || usuarios.isEmpty()) { throw new IllegalStateException(); }
            JsonObject usuario = usuarios.get(0).getAsJsonObject();
            if(!uid.equals(usuario.get("localId").getAsString())) { throw new IllegalStateException(); }
            if(usuario.has("disabled") && usuario.get("disabled").getAsBoolean()) {
                cerrarSesion();
                throw new AuthException("La cuenta esta deshabilitada.");
            }
            email = usuario.get("email").getAsString();
            verificado = usuario.has("emailVerified") && usuario.get("emailVerified").getAsBoolean();
            if(usuario.has("displayName")) { nombre = usuario.get("displayName").getAsString(); }
            if(nombre == null || nombre.isBlank()) { nombre = email; }
            return verificado;
        } catch(RuntimeException error) {
            cerrarSesion();
            throw new AuthException("Firebase no ha devuelto un perfil valido.");
        }
    }

    public void recuperarContrasena(String email) throws AuthException {
        validarEmail(email);
        JsonObject datos = new JsonObject();
        datos.addProperty("requestType", "PASSWORD_RESET");
        datos.addProperty("email", email.trim());
        peticion("accounts:sendOobCode", datos);
    }

    public void cerrarSesion() {
        idToken = null;
        uid = null;
        email = null;
        nombre = null;
        verificado = false;
        caducidad = null;
    }

    private JsonObject credenciales(String email, String contrasena) {
        JsonObject datos = new JsonObject();
        datos.addProperty("email", email.trim());
        datos.addProperty("password", contrasena);
        datos.addProperty("returnSecureToken", true);
        return datos;
    }

    private void cargarSesion(JsonObject respuesta) throws AuthException {
        try {
            idToken = respuesta.get("idToken").getAsString();
            uid = respuesta.get("localId").getAsString();
            email = respuesta.get("email").getAsString();
            long segundos = respuesta.get("expiresIn").getAsLong();
            if(idToken.isBlank() || uid.isBlank() || segundos <= 0) { throw new IllegalStateException(); }
            caducidad = Instant.now().plusSeconds(segundos);
            verificado = false;
        } catch(RuntimeException error) {
            cerrarSesion();
            throw new AuthException("Firebase no ha devuelto una sesion valida.");
        }
    }

    private void comprobarToken() throws AuthException {
        if(idToken == null || caducidad == null || !Instant.now().isBefore(caducidad)) {
            cerrarSesion();
            throw new AuthException("La sesion ha caducado. Inicia sesion de nuevo.");
        }
    }

    private void validarEmail(String email) throws AuthException {
        if(email == null || !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new AuthException("El correo electronico no tiene un formato valido.");
        }
    }

    private JsonObject peticion(String operacion, JsonObject datos) throws AuthException {
        if(!estaConfigurado()) { throw new AuthException("Falta firebase.apiKey en config/firebase.properties. Consulta docs/FIREBASE.md."); }
        String clave = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        URI url = URI.create(direccion.toString() + operacion + "?key=" + clave);
        HttpRequest solicitud = HttpRequest.newBuilder(url).timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json").header("X-Firebase-Locale", "es")
                .POST(HttpRequest.BodyPublishers.ofString(datos.toString(), StandardCharsets.UTF_8)).build();
        try {
            HttpResponse<String> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonObject contenido = JsonParser.parseString(respuesta.body()).getAsJsonObject();
            if(respuesta.statusCode() != 200) {
                String codigo = "";
                if(contenido.has("error")) {
                    JsonObject error = contenido.getAsJsonObject("error");
                    if(error.has("message")) { codigo = error.get("message").getAsString(); }
                }
                throw new AuthException(mensajeError(codigo));
            }
            return contenido;
        } catch(IOException error) {
            throw new AuthException("No se puede conectar con Firebase. Comprueba Internet e intentalo de nuevo.");
        } catch(InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AuthException("Se ha cancelado la peticion.");
        } catch(RuntimeException error) {
            throw new AuthException("La respuesta de Firebase no es valida. Intentalo de nuevo.");
        }
    }

    private String mensajeError(String codigo) {
        if(codigo.startsWith("EMAIL_EXISTS")) { return "Ya existe una cuenta con ese correo."; }
        if(codigo.startsWith("INVALID_EMAIL")) { return "El correo electronico no es valido."; }
        if(codigo.startsWith("WEAK_PASSWORD") || codigo.startsWith("PASSWORD_DOES_NOT_MEET_REQUIREMENTS")) { return "La contrasena es demasiado debil para la politica del proyecto."; }
        if(codigo.startsWith("INVALID_PASSWORD") || codigo.startsWith("EMAIL_NOT_FOUND") || codigo.startsWith("INVALID_LOGIN_CREDENTIALS")) { return "Correo o contrasena incorrectos."; }
        if(codigo.startsWith("USER_DISABLED")) { return "La cuenta esta deshabilitada."; }
        if(codigo.startsWith("INVALID_ID_TOKEN") || codigo.startsWith("TOKEN_EXPIRED") || codigo.startsWith("USER_NOT_FOUND")) {
            cerrarSesion();
            return "La sesion ha caducado. Inicia sesion de nuevo.";
        }
        if(codigo.startsWith("TOO_MANY_ATTEMPTS")) { return "Demasiados intentos. Espera antes de volver a probar."; }
        if(codigo.startsWith("OPERATION_NOT_ALLOWED")) { return "Activa Email/Password en Firebase Authentication."; }
        if(codigo.startsWith("API_KEY_INVALID")) { return "La clave de configuracion de Firebase no es valida."; }
        return "Firebase ha rechazado la solicitud. Revisa la configuracion y vuelve a intentarlo.";
    }
}

