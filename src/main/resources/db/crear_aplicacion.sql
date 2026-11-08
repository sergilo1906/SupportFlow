CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY,
    firebase_uid TEXT NOT NULL UNIQUE,
    nombre TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    rol TEXT NOT NULL CHECK(rol IN ('EMPLEADO','TECNICO','ADMIN')),
    fecha_registro TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS equipos (
    id INTEGER PRIMARY KEY,
    nombre TEXT NOT NULL,
    tipo TEXT NOT NULL,
    marca TEXT NOT NULL,
    modelo TEXT NOT NULL,
    numero_serie TEXT NOT NULL UNIQUE,
    estado TEXT NOT NULL CHECK(estado IN ('ACTIVO','BAJA')),
    usuario_asignado INTEGER REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS comentarios (
    id INTEGER PRIMARY KEY,
    ticket_id INTEGER NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    texto TEXT NOT NULL,
    fecha TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS historial_tickets (
    id INTEGER PRIMARY KEY,
    ticket_id INTEGER NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    accion TEXT NOT NULL,
    fecha TEXT NOT NULL
);
