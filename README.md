# SupportFlow · Proyecto M13 de DAM

Aplicación de escritorio de incidencias e inventario IT: **Java 21, JavaFX con FXML, SQLite, JDBC y Firebase Authentication**. No incorpora inteligencia artificial.

## Qué funciona

Tickets con creador, técnico, equipo, prioridades, estados y filtros; comentarios e historial; inventario con asignación y baja; usuarios locales y roles; ocho contadores estadísticos. Las operaciones se ejecutan desde las pantallas y comprueban permisos en los servicios Java.

La autenticación REST incluye registro, acceso, comprobación y reenvío de verificación, recuperación y cierre de sesión. **La configuración local y la conexión pública con el proyecto Firebase están comprobadas. El registro, el acceso y la entrega de correos reales siguen pendientes de la primera prueba manual.** Las pruebas automáticas utilizan respuestas simuladas.

## Abrir y ejecutar

Repositorio: [sergilo1906/SupportFlow](https://github.com/sergilo1906/SupportFlow).

```powershell
git clone https://github.com/sergilo1906/SupportFlow.git
cd SupportFlow
```

Abre esta carpeta en VS Code, instala la extensión Java recomendada y utiliza una terminal nueva. Fuera de VS Code:

```powershell
cd C:\Proyectos\SupportFlow
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot'
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
.\mvnw.cmd clean test
```

En otro equipo, adapta la ruta del JDK 21 en el comando y en `.vscode/settings.json` a su instalación.

**Demostración sin Firebase:**

```powershell
.\mvnw.cmd javafx:run "-Dsupportflow.demo=true"
```

También está en VS Code: **Terminal > Ejecutar tarea > SupportFlow: DEMO**. Selecciona un usuario ficticio y entra; no hay contraseñas demo. La ventana identifica el modo DEMO y utiliza `data/supportflow-demo.db`. Conserva sus cambios al cerrar. Para otra demostración independiente se puede indicar un archivo nuevo con `"-Dsupportflow.demo.db=data/otra-demo.db"`.

**Modo real:**

```powershell
.\mvnw.cmd javafx:run "-Dsupportflow.demo=false"
```

En el equipo de desarrollo, el archivo local `config/firebase.properties` ya contiene la configuración proporcionada para Support IT (`support-it-ac6a3`). No sobrescribas ese archivo con el ejemplo. Ese archivo y las bases de datos locales no se incluyen en GitHub. Después de clonar en otro equipo, sigue la sección «Configuración en otro equipo» de la [guía Firebase](docs/FIREBASE.md); la DEMO funciona sin configurar Firebase. La guía también explica el primer registro, la verificación y cómo indicar después el UID del primer ADMIN. Sin clave no se permite entrar en modo real. La cuenta debe tener el correo verificado. Cerrar la ventana termina el programa. Cierra las ventanas de la aplicación antes de ejecutar `clean test`.

## Datos y seguridad

El modo real usa `data/supportflow.db`. Se conserva el ticket anterior; la migración añade tablas y relaciones sin sustituir registros. Antes de modificar columnas de una tabla existente, se guarda una copia SQLite en `data/backups/`. La inicialización se puede repetir.

SQLite y la configuración local están ignorados por Git. No se almacenan contraseñas ni tokens en archivos o en SQLite. Los roles controlan el uso normal de la aplicación; quien pueda modificar el archivo local puede saltarse esos controles. **No es un servidor empresarial multiusuario.**

## Código y estudio

- `model/`: objetos y enumeraciones.
- `dao/`: SQL y JDBC.
- `service/`: reglas, sesión y permisos.
- `auth/`: API REST de Firebase con HttpClient y Gson.
- `controller/`: controladores JavaFX; `util/`: conexión, esquema, DEMO, avisos y navegación.
- `resources/fxml/`, `css/`, `db/`: pantallas, estilos y SQL.
- `src/test/`: pruebas del modelo, DAO, permisos, migración, DEMO, Firebase simulado y botones FXML.

Lee [GUIA_ESTUDIO](docs/GUIA_ESTUDIO.md), [BASE_DATOS](docs/BASE_DATOS.md), [FIREBASE](docs/FIREBASE.md) y [PRUEBAS](docs/PRUEBAS.md). Se conserva la documentación académica anterior, actualizada.

Versiones: JavaFX 21.0.12, SQLite JDBC 3.53.4.0, Gson 2.15.0, JUnit 5.14.4, Maven 3.9.16 mediante Wrapper 3.3.4. No se necesita Maven global.

Pendientes: primer registro, verificación y acceso reales con Firebase, UID del administrador inicial, revisión del tutor, memoria/defensa personal y tiempos efectivos. No se ha generado instalador ni probado Linux/macOS. La asistencia de IA en el desarrollo debe declararse según las normas del centro.
