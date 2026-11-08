# Pruebas y evidencia de SupportFlow

## Ejecutar

Desde la raíz, con Java 21 y todas las ventanas de SupportFlow cerradas:

```powershell
.\mvnw.cmd clean test
```

Las pruebas gráficas necesitan una sesión de escritorio: crean ventanas JavaFX reales, pulsan sus botones y las cierran. No requieren una librería de automatización adicional. No se ha probado un entorno Linux sin pantalla.

## Casos automatizados

| Clase | Casos | Qué comprueba |
|---|---:|---|
| TicketTest | 5 | Creación, setters, valores por defecto, validación y reconstrucción |
| ConexionBDTest | 3 | Consulta real SQLite, parámetros, cierre de recursos, claves foráneas y error de carpeta |
| TicketDAOTest | 4 | UPDATE y DELETE originales, ID existente/inexistente y mensajes |
| EsquemaBDTest | 2 | Migración repetible sin pérdida y protección de tickets ante borrado de equipo |
| AplicacionServiceTest | 14 | CRUD, permisos, estados, historial transaccional, comentarios, inventario, usuarios y estadísticas |
| FirebaseAuthTest | 12 | JSON y respuestas simuladas, verificación, errores, registro local, ADMIN por UID y cierre |
| AplicacionGuiTest | 4 | Botones FXML, CRUD desde ventanas, todos los roles, filtros, inventario, usuarios, estadísticas y reapertura |
| DemoDatosTest | 3 | Inicialización repetible, persistencia y separación del modo real |
| **Total** | **47** | **0 fallos, 0 errores y 0 omitidas en la comprobación del 07/11/2026** |

Los tres archivos originales de pruebas se han conservado. No se han eliminado casos para conseguir que Maven pase.

## Pruebas funcionales de SQLite

Los archivos temporales contienen datos ficticios. Se ejecutan INSERT, SELECT, UPDATE y DELETE reales, no solo se comprueba que el código compila. Después de escribir se consulta desde otra conexión para verificar el resultado persistido.

El caso de rollback crea un disparador temporal que rechaza la inserción en historial. Comprueba que el cambio de estado y la asignación se revierten. La prueba de migración comprueba campos originales, relaciones nuevas vacías y que repetir la inicialización no duplica datos.

Además se ha migrado dos veces una copia temporal del SQLite real. Se compararon los seis campos originales en ambas direcciones mediante EXCEPT: cero diferencias, cero registros perdidos e integridad correcta. No se han ejecutado pruebas de borrado o cambios de estado sobre el ticket real.

Después se arrancó el modo real y se aplicó la misma migración, con copia previa. La comparación posterior con el respaldo volvió a dar cero diferencias en los campos originales: se conserva el único ticket, no se han añadido usuarios ni equipos ficticios y las comprobaciones de integridad y claves foráneas son correctas.

## Recorridos de ventanas

El test gráfico usa un archivo DEMO distinto en cada caso. Carga los nueve FXML y realiza:

- ADMIN: crear, consultar, comentar, asignar, cambiar estado y eliminar ticket con confirmación; alta, modificación, asignación y baja de equipo; rol y nombre de usuario; estadísticas.
- EMPLEADO: solo sus tickets/equipos, filtro, comentario y controles administrativos deshabilitados.
- TECNICO: sus asignados, cambio de estado e inventario; sin asignación, baja ni administración.
- Registro: comprobación local de contraseñas distintas sin llamadas de red.
- Cierre y reapertura del archivo DEMO para verificar persistencia.

Los diálogos de confirmación solo se aceptan automáticamente dentro del proceso de prueba y sobre datos temporales. La aplicación normal siempre pide la confirmación al usuario.

## Firebase: simulación y prueba real

FirebaseAuthTest utiliza HttpServer de Java en 127.0.0.1 con puerto aleatorio. Las respuestas y credenciales son expresamente ficticias. Este servidor solo pertenece a los tests; la aplicación distribuida no levanta un servidor.

Estas pruebas demuestran la construcción de peticiones, lectura de respuestas y tratamiento de errores. **No demuestran entrega de correo ni acceso a tu proyecto Firebase.** Las pruebas reales pendientes están en [FIREBASE.md](FIREBASE.md).

## Informes y límites

Maven escribe XML y resúmenes en `target/surefire-reports/`. La advertencia de JavaFX sobre clases en unnamed module durante JUnit corresponde a la ejecución en classpath de las pruebas. No es un fallo de los casos; el arranque normal usa el plugin JavaFX.

La revisión visual de las ventanas se realiza aparte del resultado JUnit. No se han generado instaladores, probado otros sistemas operativos ni validado académicamente el proyecto. Documenta las futuras pruebas reales sin convertir una respuesta simulada en una prueba de conexión.

Se arrancaron con javafx:run el modo real y la DEMO. Se inspeccionaron capturas reales del acceso, panel DEMO, tickets e inventario. El acceso real muestra correctamente que falta firebase.apiKey; no se realizó autenticación remota. El modo real se cerró normalmente y la DEMO se dejó abierta. Las capturas se conservan en el respaldo privado externo.

