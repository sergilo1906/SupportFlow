
CREATE TABLE IF NOT EXISTS tickets (

    id INTEGER PRIMARY KEY,
    titulo TEXT NOT NULL,
    descripcion TEXT NOT NULL,
    prioridad TEXT NOT NULL,
    estado TEXT NOT NULL,
    fecha_creacion TEXT NOT NULL

);
