# 01 · Especificación de SupportFlow

Revisión del 07/11/2026. Aplicación de escritorio para gestionar incidencias e inventario de una empresa ficticia, con Java 21, JavaFX/FXML, SQLite/JDBC y Firebase Authentication. No incorpora IA.

## Alcance implementado y comprobado

- Registro y acceso REST preparados, verificación/reenvío y recuperación; probados mediante respuestas simuladas. Conexión y correos reales pendientes de configuración.
- Usuarios locales vinculados a UID; roles EMPLEADO, TECNICO y ADMIN. Primer ADMIN mediante UID explícito, sin contraseña predeterminada.
- Alta, consulta, filtros, asignación, cambio de estado y eliminación autorizada de tickets.
- Equipo opcional en incidencia; inventario con alta, modificación, asignación y baja.
- Comentarios con autor/fecha e historial de creación, estado y asignación.
- Transacciones para guardar ticket e historial conjuntamente.
- Ocho contadores reales de tickets y equipos.
- Nueve pantallas FXML conectadas a servicios y DAO.
- DEMO independiente y persistente, identificada y sin autenticación Firebase.
- Migración repetible que conserva el ticket anterior y crea respaldo antes de añadir columnas.

## Permisos

| Operación | EMPLEADO | TECNICO | ADMIN |
|---|---|---|---|
| Crear ticket y ver/comentar propios | Sí | Sí | Sí |
| Ver/comentar tickets asignados | No | Sí | Sí |
| Consultar todos los tickets | No | No | Sí |
| Cambiar estado | No | Solo asignados | Sí |
| Asignar técnico o eliminar ticket | No | No | Sí |
| Consultar equipos | Solo asignados | Todos | Todos |
| Modificar datos descriptivos de equipos | No | Sí | Sí |
| Alta, asignación, estado/baja de equipos | No | No | Sí |
| Gestionar usuarios y roles | No | No | Sí |
| Estadísticas | No | No | Sí |

Los servicios comprueban permisos además de adaptar botones. Las contraseñas no se guardan en SQLite. El archivo local sigue siendo modificable por quien tenga acceso al sistema operativo: no se presenta como un sistema empresarial multiusuario seguro.

## Reglas y datos

Estados OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED; ADMIN puede reabrir resueltos/cerrados a OPEN. Prioridades LOW, MEDIUM, HIGH y CRITICAL. No se han retirado valores de las enumeraciones anteriores.

Tablas: usuarios, equipos, tickets, comentarios e historial_tickets. Relaciones, migración y borrados se explican en [BASE_DATOS.md](BASE_DATOS.md). Los equipos se dan de baja sin perder sus tickets. El borrado explícito de ticket elimina sus comentarios e historial y requiere confirmación de ADMIN.

## Requisitos no funcionales y exclusiones

Código tradicional de DAM, ArrayList y bucles, getters/setters, JDBC, FXML y CSS básico. Java 21 y Maven Wrapper. No hay servidor propio, ORM, IA, chat, adjuntos, pagos ni gráficos externos. No se ha generado instalador ni probado Linux/macOS.

## Evidencia y límites de finalización

47 pruebas automatizadas superadas, incluidos recorridos reales de botones FXML sobre SQLite temporal. No equivalen a pruebas de correo real. Consultar [PRUEBAS.md](PRUEBAS.md) y [FIREBASE.md](FIREBASE.md).

La planificación conserva el máximo provisional de 100 horas; no son horas personales ya medidas. Sigue pendiente confirmar la adecuación de JavaFX a M13 con el tutor, estudiar y declarar la asistencia recibida, preparar la memoria/defensa personal y probar Firebase con configuración válida.
