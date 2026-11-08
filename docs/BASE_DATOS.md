# Base de datos de SupportFlow

## Archivos independientes

| Uso | Archivo |
|---|---|
| Datos reales | `data/supportflow.db` |
| Demostración | `data/supportflow-demo.db` |
| Pruebas | Directorios temporales de JUnit |
| Copia previa a cambios de columnas | `data/backups/*.antes-v2-*.db` |

La DEMO rechaza expresamente la ruta real y los usuarios cuyo UID no empiece por `demo:`. No se copian los usuarios ficticios al modo real. Los archivos locales no forman parte de Git.

## Cinco tablas

| Tabla | Columnas | Relaciones y restricciones |
|---|---|---|
| usuarios | id, firebase_uid, nombre, email, rol, fecha_registro | UID y email UNIQUE; rol EMPLEADO/TECNICO/ADMIN |
| equipos | id, nombre, tipo, marca, modelo, numero_serie, estado, usuario_asignado | Serie UNIQUE; estado ACTIVO/BAJA; usuario opcional |
| tickets | id, titulo, descripcion, prioridad, estado, fecha_creacion, usuario_creador, tecnico_asignado, equipo_id | Las tres relaciones nuevas pueden ser NULL |
| comentarios | id, ticket_id, usuario_id, texto, fecha | Ticket y autor obligatorios |
| historial_tickets | id, ticket_id, usuario_id, accion, fecha | Ticket y autor obligatorios |

Las claves primarias INTEGER las asigna SQLite. El id cero de un objeto nuevo indica que no se ha guardado. Los DAO nuevos recuperan la clave generada al insertar. Los campos de fechas son TEXT en formato ISO: `LocalDateTime.toString()` al guardar y `LocalDateTime.parse()` al leer. Son fechas locales sin zona horaria.

Las contraseñas son responsabilidad de Firebase y no aparecen en este esquema. Tampoco se guardan tokens.

## Migración del esquema anterior

El esquema original tenía los seis primeros campos de tickets. `EsquemaBD.inicializar()` hace lo siguiente:

1. Abre una conexión con las claves foráneas activadas.
2. Comprueba las columnas mediante `PRAGMA table_info(tickets)`.
3. Si faltan relaciones en una tabla existente, guarda una copia consistente mediante `VACUUM INTO ?`.
4. Empieza una transacción JDBC.
5. Ejecuta los CREATE TABLE IF NOT EXISTS de los recursos SQL.
6. Añade solo las columnas que falten mediante ALTER TABLE.
7. Confirma con commit; ante un error ejecuta rollback.

No se borra ni se reconstruye la tabla tickets. No se atribuye un creador inventado al ticket antiguo: las relaciones nuevas quedan en NULL. ADMIN lo puede consultar y asignar a un técnico. Un empleado no obtiene acceso a tickets antiguos sin creador.

La migración se ha probado dos veces sobre una copia temporal de la base real: cero diferencias en los seis campos originales y cero registros perdidos. La prueba automática adicional usa una tabla antigua creada en un archivo temporal. La copia privada anterior al desarrollo está en `C:\Proyectos\SupportFlow_Backup_Final_20261107`.

## Qué sucede al eliminar o dar de baja

- Un equipo se da de baja cambiando su estado a BAJA; sus tickets permanecen relacionados.
- La relación ticket-equipo usa RESTRICT: borrar un equipo relacionado provoca error.
- Las relaciones de creador, comentarios e historial protegen a sus autores con RESTRICT.
- Eliminar un usuario local sin referencias de autoría puede dejar sus asignaciones vacías mediante SET NULL.
- Eliminar un ticket es una acción explícita de ADMIN con confirmación. Sus comentarios e historial se eliminan mediante CASCADE; el diálogo lo explica.
- Eliminar un registro local de usuario no elimina su cuenta Firebase. Si vuelve a iniciar sesión verificado, se puede crear de nuevo su registro como EMPLEADO.

## Operaciones y transacciones

`TicketDAO.guardarTicketCompleto()` inserta el ticket y su primer registro de historial juntos. Los métodos con autor `cambiarEstado(id, estado, autorId)` y `asignarTecnico(id, tecnicoId, autorId)` guardan el cambio y su historial en la misma conexión y transacción. Una prueba provoca un fallo de historial y comprueba que ni el estado ni la asignación quedan cambiados a medias.

Los métodos originales de TicketDAO se conservan por compatibilidad con los ejercicios y pruebas. Las ventanas utilizan TicketService y las operaciones completas con autor. Los DAO son acceso de bajo nivel; las reglas y permisos se comprueban antes en los servicios.

Toda entrada que se utiliza como parámetro SQL se pasa mediante PreparedStatement. Las consultas sin parámetros y los CREATE/ALTER constantes pueden utilizar Statement.

## Estados, permisos y límites

Estados: OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED. ADMIN puede reabrir un RESOLVED o CLOSED a OPEN. Se conservan las prioridades LOW, MEDIUM, HIGH y CRITICAL.

Solo el técnico asignado o ADMIN cambia el estado; solo ADMIN asigna o elimina tickets. El creador puede consultar y comentar. Los servicios vuelven a leer el rol local antes de operar, por lo que un cambio de rol afecta a las sesiones abiertas.

Este control sirve dentro de la aplicación. No impide que una persona con permisos sobre el archivo SQLite lo modifique directamente. Los ordenadores no comparten automáticamente datos: cada archivo contiene sus propios registros.

