# Guía de estudio y defensa de SupportFlow

Esta guía explica el código que existe en este proyecto, no una aplicación imaginaria. Se ha desarrollado con asistencia de IA y debe estudiarse, revisarse y declararse según las normas del centro. No se han atribuido horas personales al alumno ni se afirma aprobación del tutor. Los ejercicios externos citados en el encargo no estaban en el workspace: se ha tomado como referencia el código de Ticket, TicketDAO y las reglas indicadas.

## 1. Qué problema resuelve

SupportFlow organiza incidencias informáticas y equipos de una empresa ficticia. Un empleado registra un problema, un administrador lo asigna a un técnico y ese técnico cambia su estado. Los comentarios permiten hablar del problema y el historial permite consultar quién hizo cada cambio. El inventario relaciona un equipo con su usuario y con las incidencias que le afectan.

Hay dos formas de abrir la aplicación. En modo real la identidad se comprueba mediante Firebase y se exige correo verificado. En DEMO se elige expresamente un usuario ficticio, sin contraseña y sin autenticar con Firebase. Cada modo utiliza su propio archivo SQLite. Ambos muestran las mismas pantallas de gestión y ejecutan sus operaciones mediante los mismos servicios y DAO.

La aplicación es local. No incluye un servidor propio, no sincroniza automáticamente ordenadores y no incorpora IA. Firebase identifica personas; no convierte el archivo SQLite en una base de datos empresarial protegida frente al propietario del ordenador.

## 2. Cómo está organizado el código

El recorrido habitual es:

```text
Botón de un archivo FXML
  -> método @FXML del controlador
  -> servicio: comprueba sesión, permisos y reglas
  -> DAO: ejecuta SQL mediante JDBC
  -> archivo SQLite
  -> objetos Java que vuelven a la tabla o los Labels
```

Cada grupo tiene una responsabilidad concreta. Los modelos representan datos. Los DAO saben guardar y leer esos datos. Los servicios comprueban quién puede hacer cada operación. Los controladores leen los campos de la ventana y llaman al servicio. El archivo FXML describe los componentes visuales. El CSS solo cambia su aspecto.

Esta separación evita poner toda la aplicación en SupportFlowApp. No se utilizan interfaces de repositorio, frameworks de inyección de dependencias, clases abstractas propias ni un ORM. Se crean objetos con `new` y se pasan referencias mediante constructores o el método `configurar()`.

## 3. Modelos: atributos, constructores y accesores

### Ticket

Representa una incidencia. Sus atributos originales son `id`, `titulo`, `descripcion`, `prioridad`, `estado` y `fechaCreacion`. Se conservan y se añaden `usuarioCreador`, `tecnicoAsignado` y `equipoId`.

Todos los atributos son privados. Desde otra clase no se escribe directamente `ticket.titulo`: se llama a `setTitulo()` o se consulta con `getTitulo()`. Esto permite validar el dato antes de modificar el objeto.

El constructor de dos parámetros recibe título y descripción. Llama al constructor de tres parámetros y utiliza prioridad MEDIUM. El constructor de tres parámetros prepara una incidencia nueva: id cero, estado OPEN y fecha actual. No inventa el identificador definitivo de SQLite.

El constructor que recibe id, título, descripción, prioridad, estado y fecha reconstruye una incidencia ya guardada. Exige id positivo y conserva la fecha original. Por eso leer un ticket antiguo no le cambia su fecha a la de hoy.

El título debe tener contenido y no superar 150 caracteres; la descripción tampoco puede estar vacía. Una prioridad nula se convierte en MEDIUM y un estado nulo en OPEN. Esas reglas se comprueban con `if`, como en los ejercicios de clase.

`Integer` se usa para las relaciones opcionales porque admite `null`. Un técnico nulo significa «sin asignar»; no significa usuario número cero. El id del propio ticket es `long` y cero significa «todavía no guardado». `toString()` devuelve un resumen legible para depurar o explicar el objeto, pero no lo guarda en SQLite.

### TicketPriority y TicketStatus

Son enumeraciones. `TicketPriority` permite LOW, MEDIUM, HIGH y CRITICAL. `TicketStatus` permite OPEN, IN_PROGRESS, RESOLVED y CLOSED. Se conservan los valores del código anterior.

Una enumeración evita escribir cualquier texto como prioridad. Al guardar, `ticket.getEstado().name()` produce el texto OPEN, por ejemplo. Al leer, `TicketStatus.valueOf(texto)` recupera la enumeración. El texto guardado tiene que coincidir con uno de sus valores.

### Usuario y UsuarioRol

Usuario contiene id, firebaseUid, nombre, email, rol y fechaRegistro. Su constructor recibe UID, nombre, email y rol; la fecha se prepara al crear el objeto. Al reconstruir desde SQLite el DAO asigna el id y la fecha que ya estaban guardados.

Los getters permiten mostrar los datos y los setters permiten actualizar sus valores. Nombre, email, UID y rol no pueden faltar. `UsuarioRol` contiene EMPLEADO, TECNICO y ADMIN. No existe un atributo contraseña: esa información pertenece a Firebase y no se guarda en SQLite.

Un UID es el identificador de Firebase. El id es el identificador numérico local de SQLite. No son lo mismo. Las relaciones de tablas utilizan el id local; la asociación con la cuenta autenticada utiliza el UID.

### Equipo

Contiene id, nombre, tipo, marca, modelo, numeroSerie, estado y usuarioAsignado. El constructor recibe nombre, tipo, marca, modelo y serie; empieza con estado ACTIVO. El usuario asignado es opcional y se indica con su id local.

Sus setters rechazan campos de texto vacíos y estados distintos de ACTIVO o BAJA. SQLite exige que el número de serie sea único. Dar de baja no destruye el objeto ni elimina sus tickets históricos: cambia el estado del registro.

`toString()` muestra id, nombre, serie y estado. Esa representación se utiliza en el ComboBox de equipos del formulario de incidencia. Así se selecciona un objeto Equipo completo, aunque la ventana muestre una línea de texto.

### Comentario

Contiene id, ticketId, usuarioId, texto y fecha. Su constructor recibe ticket, autor y texto; prepara la fecha actual. El texto no puede estar vacío. El DAO asigna los valores originales al leerlo. No es una simple cadena suelta: queda relacionado con un ticket y con un autor.

### HistorialTicket

Contiene id, ticketId, usuarioId, accion y fecha. El constructor recibe ticket, autor y acción y prepara la fecha. Se crea automáticamente cuando se registra una incidencia, se cambia su estado o se asigna un técnico. El usuario no escribe directamente esas anotaciones desde una ventana.

### Estadisticas

Tiene ocho contadores: totalTickets, abiertos, enProgreso, resueltos, cerrados, totalEquipos, equiposActivos y equiposBaja. Su constructor prepara un objeto cuyos enteros empiezan en cero. El DAO rellena los contadores con consultas COUNT y el controlador los muestra en Labels.

## 4. ArrayList, for, while e if/else

ArrayList es una lista que puede crecer. Se utiliza `add()` para añadir un objeto, `size()` para conocer el número de elementos y `get(i)` para consultar una posición. La primera posición es cero y la última es `size() - 1`.

Ejemplo real de TicketService: primero se obtienen los tickets del DAO y después se recorren para seleccionar los visibles. El método devuelve otra lista, no modifica la lista original:

```java
ArrayList<Ticket> todos = ticketDAO.obtenerTickets();
ArrayList<Ticket> visibles = new ArrayList<Ticket>();
for(int i = 0; i < todos.size(); i++) {
    Ticket ticket = todos.get(i);
    if(puedeConsultar(usuario, ticket)) {
        if((estado == null || ticket.getEstado() == estado) &&
                (prioridad == null || ticket.getPrioridad() == prioridad)) {
            visibles.add(ticket);
        }
    }
}
```

`i++` avanza a la siguiente posición. `ticket` es una variable intermedia que facilita leer el código. `&&` exige que se cumplan ambas condiciones; `||` admite cualquiera de ellas. Un filtro nulo significa «no limitar por ese campo».

En JDBC se utiliza `while(resultado.next())` porque no sabemos de antemano cuántas filas devolverá la consulta. Cada llamada a `next()` avanza una fila y devuelve true mientras haya datos. No se accede a los campos antes de llamar a next.

No se utilizan streams, filter o map para seleccionar datos. En las pruebas de excepciones aparece una lambda porque JUnit necesita recibir la operación que debe fallar. Los controladores utilizan clases anónimas de Task y EventHandler para las peticiones de red: son mecanismos normales de JavaFX y se explican más adelante.

## 5. SQLite, ConexionBD y recursos

SQLite guarda tablas en un archivo local. No hay que arrancar un servicio de base de datos. `ConexionBD()` prepara la ruta `data/supportflow.db`; `ConexionBD(Path rutaBD)` permite utilizar otro archivo, como la DEMO o una prueba temporal. Ambos constructores guardan una ruta absoluta normalizada.

`abrirConexion()` crea la carpeta si hace falta, llama a DriverManager y devuelve un Connection. También activa `PRAGMA foreign_keys = ON` en cada conexión. Activarlo solo una vez al instalar no basta: esa configuración pertenece a cada conexión.

La clase conserva los comentarios existentes del alumno. No se ha sustituido por una herramienta de conexión más avanzada. Quien recibe un Connection debe cerrarlo. Por eso los DAO utilizan esta estructura:

```java
try(Connection conexion = baseDatos.abrirConexion();
    PreparedStatement consulta = conexion.prepareStatement(sql)) {
    // Preparar y ejecutar la consulta
}
```

Esto es try-with-resources. Al salir del bloque se cierran consulta y conexión, también si aparece una excepción. Un ResultSet se cierra en otro try cuando hace falta leerlo. SQLException indica un problema de JDBC; el servicio o el controlador pueden propagarlo o mostrar un mensaje comprensible.

`EsquemaBD` crea las tablas y amplía tickets conservando filas. Comprueba si las columnas ya existen para no repetir el ALTER. Antes de añadir columnas a una tabla existente guarda un respaldo consistente. La inicialización utiliza commit o rollback para no dejar el esquema a medias. `InicializarBD` conserva su punto de entrada de consola y llama a esa inicialización completa.

El detalle de tablas, claves y borrados está en BASE_DATOS.md. Estudia especialmente por qué los tickets antiguos tienen creador nulo y por qué no se les atribuye una persona inventada.

## 6. SQL: INSERT, SELECT, UPDATE y DELETE

### INSERT: guardar una fila nueva

`UsuarioDAO.guardarUsuario()` utiliza esta consulta:

```sql
INSERT INTO usuarios(firebase_uid,nombre,email,rol,fecha_registro) VALUES(?,?,?,?,?)
```

Los interrogantes se rellenan con setString y otros métodos de PreparedStatement. No se construye la consulta pegando lo escrito en un TextField. Esto evita que unas comillas de un nombre se interpreten como código SQL.

Las posiciones empiezan en uno, no en cero. El primer setString corresponde al primer interrogante. `executeUpdate()` ejecuta la inserción. Cuando se pide RETURN_GENERATED_KEYS, getGeneratedKeys permite leer el id que ha asignado SQLite.

### SELECT: consultar filas

`TicketDAO.obtenerPorId()` utiliza `SELECT * FROM tickets WHERE id=?`, coloca el id con setInt y ejecuta executeQuery. El resultado es un ResultSet. Si next devuelve false, no existe ese ticket y se devuelve null. Si devuelve true, se construye un Ticket con sus datos.

`obtenerTickets()` devuelve ArrayList de Ticket. Por cada fila se crea un objeto y se añade a la lista. Después el servicio comprueba permisos y filtros. La tabla JavaFX recibe esos objetos; no recibe el ResultSet abierto.

### UPDATE: cambiar una fila existente

El método original `cambiarEstado(int id, TicketStatus nuevoEstado)` conserva la consulta:

```sql
UPDATE tickets SET estado = ? WHERE id = ?
```

Se guarda el resultado de executeUpdate en filasModificadas. Si es mayor que cero, se ha encontrado el ticket. Si es cero, se informa de que el id no existe. No hay que confundir «SQL válido» con «se encontró una fila».

La aplicación gráfica usa el método con autor y transacción para registrar también el historial. El método de dos parámetros permanece para conservar los ejercicios y sus pruebas anteriores.

### DELETE: eliminar una fila

El método original eliminarTicket utiliza `DELETE FROM tickets WHERE id = ?`. También comprueba filasEliminadas. En la interfaz solo ADMIN puede solicitar esta operación y tiene que confirmarla. Al borrar un ticket se eliminan sus comentarios e historial; el diálogo lo advierte.

En equipos se prefiere un UPDATE a BAJA. Eliminar físicamente un equipo relacionado está impedido por una clave foránea. Así se conserva el contexto de tickets antiguos.

## 7. Qué hace cada DAO

Los seis DAO reciben ConexionBD en su constructor. TicketDAO también conserva su constructor sin argumentos de los ejercicios. El atributo privado baseDatos indica en qué archivo trabajan.

| Clase | Métodos y responsabilidad |
|---|---|
| TicketDAO | Conserva guardarTicket, mostrarTickets, cambiarEstado y eliminarTicket; añade lectura de objetos, consulta por id, guardado completo y cambios con autor e historial |
| UsuarioDAO | Guarda usuarios locales, busca por UID o id, lista, cambia nombre o rol, elimina registros locales y cuenta administradores |
| EquipoDAO | Guarda, lee y modifica equipos; convierte las columnas en objetos Equipo |
| ComentarioDAO | Inserta comentarios y obtiene los de un ticket, ordenados por id |
| HistorialTicketDAO | Inserta usando una conexión recibida y obtiene el historial de un ticket |
| EstadisticasDAO | Ejecuta ocho COUNT y rellena un objeto Estadisticas |

El DAO no decide qué botón puede usar una persona. Es acceso de bajo nivel. Las ventanas llaman a los servicios, que comprueban la sesión y el rol antes de usar el DAO. No se debe añadir un botón nuevo que salte directamente al DAO para modificar datos.

### Transacciones de ticket e historial

Una operación puede necesitar dos escrituras: cambiar el ticket y añadir su historial. Si se confirmase la primera y fallase la segunda, faltaría la explicación del cambio. Por eso se usa una sola conexión:

```java
conexion.setAutoCommit(false);
try {
    // Modificar el ticket y guardar el historial
    conexion.commit();
} catch(SQLException error) {
    conexion.rollback();
    throw error;
}
```

El esquema anterior resume el bloque que existe en los métodos completos del DAO. Commit confirma todo; rollback deshace lo que todavía no se había confirmado. HistorialTicketDAO recibe esa conexión y no abre otra para insertar, porque otra conexión tendría una transacción diferente.

Una prueba crea un disparador que rechaza el historial y verifica que estado y asignación permanecen como antes. Eso demuestra el comportamiento funcional, no solo que se han escrito las palabras commit y rollback.

## 8. Servicios, sesión y permisos

### Sesion y Permisos

Sesion contiene usuario, indicador demo y caducidad. En modo real comprueba que no haya vencido el plazo recibido de Firebase. En modo DEMO no finge que exista un token. También evita utilizar un UID de demostración como sesión real. Cerrar la sesión elimina su referencia al usuario.

Permisos vuelve a consultar el usuario en SQLite en cada operación y comprueba que el UID siga coincidiendo. No se confía únicamente en el rol que quedó guardado al abrir la pantalla. Si ADMIN cambia TECNICO a EMPLEADO, las siguientes operaciones del técnico ya utilizan su nuevo rol.

### TicketService

Su constructor recibe ConexionBD y Sesion y crea el TicketDAO. `obtenerTickets()` aplica filtros y visibilidad; `consultarTicket()` impide consultar uno ajeno. El creador ve sus incidencias, TECNICO ve además sus asignadas y ADMIN ve todas.

`crearTicket()` valida el modelo y el equipo, obtiene el creador de la sesión y guarda el registro completo. No utiliza un creador enviado libremente por la ventana. Un empleado solo relaciona sus equipos activos.

`cambiarEstado()` comprueba que el usuario sea ADMIN o el técnico asignado. Permite el recorrido OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED; ADMIN puede reabrir resueltos o cerrados a OPEN. Un empleado no puede cambiarlo aunque manipule el controlador para llamar al método.

`asignarTecnico()` exige ADMIN y comprueba que el usuario escogido sea TECNICO. Un valor nulo elimina la asignación. `eliminarTicket()` exige ADMIN. Añadir y leer comentarios o historial exige poder consultar ese ticket. `nombreUsuario()` permite mostrar los autores de los registros ya autorizados.

### EquipoService

Su constructor recibe conexión y sesión y crea EquipoDAO. Los empleados solo consultan sus equipos. ADMIN da de alta, asigna, modifica, reactiva y da de baja. TECNICO modifica los datos descriptivos del inventario, pero el servicio rechaza cambios de asignación o de estado realizados por ese rol. La baja conserva la relación con incidencias históricas.

### UsuarioService

Permite a ADMIN listar usuarios, cambiar nombre y rol o eliminar registros locales sin referencias protegidas. Evita degradar al último administrador y eliminar el propio registro. Los técnicos pueden consultar el directorio necesario para mostrar asignaciones del inventario, pero no administrar roles.

### EstadisticasService

Comprueba ADMIN antes de solicitar los contadores. No basta con deshabilitar el botón Estadísticas en el panel: el método que obtiene la información también aplica el permiso.

### AutenticacionService

Une la identidad de Firebase con los datos de SQLite. Solo crea una sesión después de comprobar correo verificado. Un usuario local nuevo recibe EMPLEADO. El primer ADMIN solo se configura mediante el UID explícito del archivo de instalación y si no existe ya otro ADMIN. La contraseña nunca se pasa al UsuarioDAO.

## 9. FirebaseAuth y AuthException

FirebaseAuth contiene la clave pública de configuración, dirección del servicio, HttpClient y datos de sesión en memoria: token, UID, email, nombre, verificación y caducidad. Su constructor normal usa la dirección HTTPS oficial. Un segundo constructor, accesible solo desde el mismo paquete, permite dirigir las pruebas a un servidor local simulado.

RegistrarUsuario valida los campos, solicita el alta, guarda el nombre del perfil y solicita la verificación. IniciarSesion envía las credenciales y consulta el perfil para comprobar emailVerified. EnviarVerificacion también sirve para reenviar el correo. RecuperarContrasena solicita el correo de recuperación. CerrarSesion descarta los datos en memoria.

El JSON se construye con JsonObject y se lee con Gson. No se corta una cadena para localizar el token. Los errores se convierten a mensajes legibles mediante AuthException; no se enseña al usuario la respuesta cruda ni se imprimen contraseñas o tokens.

No se almacena refresh token. Al caducar la sesión se exige volver a entrar; no hay una renovación automática oculta. Los datos reales de configuración faltantes y los pasos manuales están en FIREBASE.md. Las pruebas simuladas no demuestran que un proveedor haya entregado un correo real.

## 10. FXML, componentes y controladores

SupportFlowApp extiende Application. Su método start crea ContextoApp y muestra login. Stop cierra la sesión. La clase principal no contiene los formularios de inventario ni consultas SQL.

ContextoApp guarda Stage, conexión seleccionada, FirebaseAuth, sesión, modo y ticket seleccionado. Su constructor prepara exclusivamente el archivo del modo elegido. Mostrar carga un FXML con FXMLLoader, obtiene el controlador, le pasa el contexto mediante configurar y coloca el Parent en una Scene. Los instanceof sirven para reconocer cada controlador, una técnica ya conocida de DAM. No hay un framework de navegación.

Antes de abrir una pantalla de gestión comprueba la sesión. Si ya no es válida, cierra la sesión y vuelve al acceso. Si un servicio detecta caducidad mientras estás en un formulario, el botón Volver permite regresar al acceso para entrar de nuevo.

### Relación entre FXML y Java

En formulario_ticket.fxml existe este botón:

```xml
<Button fx:id="btnGuardar" text="Guardar ticket" onAction="#onGuardar"/>
```

`fx:controller` indica la clase FormularioTicketController. Un campo `@FXML private TextField tfTitulo` se conecta con el TextField cuyo fx:id es tfTitulo. FXMLLoader crea los controles y asigna esas referencias. No debemos intentar usarlas antes de que se haya cargado el FXML.

`initialize()` prepara componentes que no necesitan la sesión: valores de un ComboBox, por ejemplo. `configurar(contexto)` se ejecuta después para cargar datos y permisos. Un método `@FXML private void onGuardar()` responde al botón. Lee los campos, llama al servicio y navega a la tabla o muestra el error.

TableView recibe objetos. PropertyValueFactory, el mecanismo estándar de JavaFX usado en los FXML, enlaza una columna con un getter: propiedad titulo significa getTitulo. Por eso mantener nombres coherentes es importante. No hemos escrito un sistema de reflection propio.

Los ComboBox guardan enumeraciones o modelos, no solo textos. `getValue()` devuelve el objeto seleccionado; con un if se comprueba si es null. Las TableView utilizan getSelectionModel para saber qué fila ha seleccionado la persona.

### Los nueve controladores

| Controlador | Campos principales y métodos |
|---|---|
| LoginController | Email, PasswordField, selector DEMO y mensaje; iniciar, recuperar, reenviar, comprobar, registro y entrada DEMO |
| RegistroController | Nombre, email, contraseñas y mensaje; compara confirmación y ejecuta el registro |
| PrincipalController | Nombre, rol y botones; navega y cierra sesión |
| TicketController | Tabla y filtros; crear, consultar, eliminar, filtrar y limpiar filtros |
| FormularioTicketController | Título, descripción, prioridad y equipo; crea la incidencia mediante TicketService |
| DetalleTicketController | Datos, listas, comentario, estado y técnico; comenta, cambia estado y asigna |
| InventarioController | Tabla y formulario; nuevo, consultar/editar, guardar, asignar y baja |
| UsuariosController | Tabla, nombre y rol; edita nombre o rol y elimina registro local con confirmación |
| EstadisticasController | Ocho Labels; solicita contadores y los muestra |

Los controladores se crean mediante el constructor sin argumentos que Java proporciona al no declarar otro. No necesitan parámetros en el constructor porque FXML todavía no ha asignado los componentes. El contexto se proporciona expresamente después. Los modelos, DAO y servicios sí tienen sus constructores con los parámetros que necesitan.

### Red sin congelar la ventana

JavaFX necesita mantener libre su hilo gráfico. Una conexión a Internet puede tardar. LoginController y RegistroController crean un Task y lo ejecutan en un Thread. Dentro de call se realiza la petición; en los eventos de éxito o fallo se vuelve a actualizar la ventana.

```java
Thread hilo = new Thread(tarea);
hilo.setDaemon(true);
hilo.start();
```

Estas líneas están en los dos controladores. El hilo daemon no mantiene la aplicación abierta al cerrar su última ventana. Mientras se realiza la petición se deshabilita el panel para evitar pulsaciones duplicadas. El Task no manipula directamente TextFields desde su método call: toma los valores antes y actualiza controles en sus eventos JavaFX.

### Otras utilidades

Avisos muestra errores con Alert y confirmaciones con CONFIRMATION. El borrado no se ejecuta hasta que la persona acepta. Los textos de errores SQL se resumen para no mostrar detalles técnicos de consultas.

DemoDatos crea usuarios, equipo, tickets y comentario ficticios en una base separada. Si ya contiene usuarios DEMO no reinicia ni duplica esos datos. Rechaza la ruta real antes de abrirla. EsquemaBD prepara las tablas tanto en DEMO como en real, pero los ejemplos solo se crean desde DemoDatos.

## 11. Cómo funcionan las pruebas

JUnit ejecuta métodos marcados con @Test. assertEquals compara el resultado con lo esperado; assertTrue comprueba una condición y assertThrows comprueba que una operación inválida produzca una excepción. Que compile significa que Java acepta el código; una prueba funcional ejecuta la operación y revisa qué ha ocurrido.

@TempDir crea una carpeta independiente para cada prueba. @BeforeEach prepara el archivo y los datos; @AfterEach cierra recursos de prueba o restaura la salida de consola. Ningún caso usa cuentas personales ni elimina el ticket real.

TicketTest y ConexionBDTest conservan los casos anteriores. TicketDAOTest comprueba las operaciones de los ejercicios. AplicacionServiceTest llama a las reglas de negocio con distintos roles y consulta lo persistido. EsquemaBDTest comprueba migración y relaciones; DemoDatosTest comprueba aislamiento e inicialización repetible.

FirebaseAuthTest usa un servidor local de prueba con respuestas ficticias. No es un servidor que se entregue como parte de SupportFlow. Comprueba el JSON enviado y el resultado que el cliente produce al recibir una respuesta. No envía correos reales.

AplicacionGuiTest carga ventanas reales, rellena controles y pulsa Button.fire. Comprueba datos en las tablas y en SQLite. Para ello utiliza Platform.runLater, CountDownLatch y una referencia de error: sirven para ejecutar en el hilo de JavaFX y esperar el resultado desde JUnit. Son mecanismos necesarios para estas pruebas gráficas, no una arquitectura nueva de la aplicación. Las confirmaciones automáticas existen solo en el código de prueba temporal.

La distribución exacta de los 47 casos y la evidencia reproducible están en PRUEBAS.md. Estudia primero los tests del modelo y del DAO, después los servicios y finalmente los de red y ventanas.

## 12. Preguntas y respuestas para la defensa

**¿Por qué no se guarda la contraseña en Usuario?** Porque Firebase gestiona esa credencial. SQLite solo guarda el UID y los datos complementarios locales.

**¿Por qué hay id y firebaseUid?** El primero identifica una fila local; el segundo relaciona esa fila con una identidad confirmada por Firebase.

**¿Puede registrarse alguien como ADMIN?** No hay ese campo en el formulario. El rol inicial es EMPLEADO; el primer administrador requiere un UID configurado expresamente y verificado. Después solo ADMIN administra roles.

**¿Ocultar botones protege los datos?** Ayuda al uso de la interfaz, pero no basta. Cada servicio comprueba sesión, rol y propiedad/asignación del registro antes de operar.

**¿Los roles son seguros si alguien edita SQLite directamente?** No. Esa persona puede modificar datos y roles del archivo. El alcance es una aplicación local educativa, no un sistema multiusuario con autorización de servidor.

**¿Por qué un equipo no se elimina al darlo de baja?** Porque los tickets históricos deben seguir indicando qué equipo tuvo el problema. BAJA mantiene la fila y la clave foránea.

**¿Qué diferencia hay entre PreparedStatement y ResultSet?** El primero prepara y ejecuta una consulta con parámetros; el segundo permite recorrer las filas devueltas por un SELECT.

**¿Qué significa que executeUpdate devuelva cero?** La consulta se ejecutó, pero no afectó a ninguna fila; puede significar que el id no existe.

**¿Qué evita una transacción?** Que un ticket cambie sin su historial si la segunda escritura falla. Commit confirma el conjunto y rollback lo deshace ante error.

**¿Por qué el ticket anterior no tiene creador?** No existía ese dato. La migración lo conserva sin inventar una identidad; ADMIN puede verlo y asignarlo.

**¿Qué hace @FXML?** Permite conectar campos y métodos privados del controlador con componentes y acciones definidos en el FXML.

**¿Qué diferencia hay entre initialize y configurar?** Initialize prepara componentes al cargar el FXML; configurar recibe después la sesión compartida y carga los datos autorizados.

**¿Por qué no se hace la petición Firebase directamente en el botón?** Porque bloquearía el hilo gráfico. Un Task en otro Thread permite seguir dibujando la ventana.

**¿Qué demuestra la DEMO?** El funcionamiento local de pantallas, SQL y roles con datos ficticios. No demuestra que Firebase haya autenticado a alguien.

**¿Qué sigue pendiente de prueba?** Configurar el proyecto Firebase y verificar manualmente acceso y entrega real de correos. También la preparación personal de memoria/defensa y la revisión académica del tutor.

**¿Quién ha hecho el trabajo y cuántas horas?** Hay código anterior del proyecto y desarrollo con asistencia de IA. Debe explicarse esa asistencia con sinceridad. Las previsiones de 100 horas no son horas reales y las duraciones personales no medidas no se inventan.

## 13. Orden recomendado para estudiar

1. Ejecutar DEMO como empleado, técnico y administrador y explicar qué cambia.
2. Leer Ticket, Equipo y Usuario y dibujar sus atributos.
3. Seguir una consulta SELECT desde ConexionBD hasta un objeto de la tabla.
4. Seguir Guardar ticket desde su botón FXML hasta INSERT e historial.
5. Ejecutar una prueba del DAO y una de permisos y explicar sus afirmaciones.
6. Provocar una validación en la ventana y explicar el try/catch y Alert.
7. Estudiar la transacción y la prueba de rollback.
8. Leer FirebaseAuth y distinguir el servidor simulado del proyecto real.
9. Preparar capturas propias, registrar tiempo efectivo y redactar conclusiones personales.

La guía está escrita en Markdown para poder exportarla posteriormente a PDF. No se ha generado ni revisado un PDF en esta tarea.
