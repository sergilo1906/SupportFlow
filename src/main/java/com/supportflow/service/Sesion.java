package com.supportflow.service;

import com.supportflow.model.Usuario;
import java.time.Instant;

// Sessio en memoria: no es guarden tokens al disc
public class Sesion {
    private Usuario usuario;
    private boolean demo;
    private Instant caducidad;

    public Sesion(Usuario usuario, boolean demo, Instant caducidad) {
        this.usuario = usuario;
        this.demo = demo;
        this.caducidad = caducidad;
    }

    public Usuario getUsuario() { return usuario; }
    public boolean isDemo() { return demo; }

    public void comprobarActiva() {
        if(usuario == null) { throw new SecurityException("Inicia sesion de nuevo."); }
        if(demo != usuario.getFirebaseUid().startsWith("demo:")) {
            throw new SecurityException("Una cuenta DEMO no puede entrar en modo real.");
        }
        if(!demo && (caducidad == null || !Instant.now().isBefore(caducidad))) {
            throw new SecurityException("La sesion ha caducado. Cierra sesion y vuelve a entrar.");
        }
    }

    public void cerrarSesion() {
        usuario = null;
        caducidad = null;
    }
}

