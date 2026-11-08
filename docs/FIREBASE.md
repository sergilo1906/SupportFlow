# Configuración y prueba real de Firebase

**Estado al 08/11/2026:** configuración local creada para Support IT (`support-it-ac6a3`). Java lee la clave proporcionada y la consulta pública oficial de Firebase responde HTTP 200 con el dominio esperado. Esto comprueba configuración y conectividad; el registro, acceso y entrega de correos requieren la primera prueba manual con una cuenta que el alumno elija.

## Configuración actual

`config/firebase.properties` ya existe y está excluido de Git. No hay que copiar la clave manualmente en este equipo.

- `firebase.apiKey`: contiene la clave suministrada; es la propiedad que lee ContextoApp para el cliente REST.
- `firebase.projectId`: contiene `support-it-ac6a3` como referencia del proyecto. La integración actual no necesita leer esta propiedad para autenticar: la API key selecciona el proyecto.
- `admin.uid`: permanece vacío. Falta el UID exacto de la cuenta verificada que se elija como primer ADMIN.

No se necesita configurar authDomain en este cliente REST de escritorio, ni instalar Firebase Hosting, Firestore, Analytics, el SDK JavaScript o Admin SDK. No se han creado cuentas ni solicitado correos durante la comprobación.

## Configuración en otro equipo

Estos pasos sirven para reproducir la configuración en otro equipo. En el actual ya está creada; no reemplaces el archivo local por la plantilla.

1. En Firebase Console, crea o selecciona tu propio proyecto.
2. En Authentication > Sign-in method, habilita Email/Password.
3. Revisa la política de contraseñas y las plantillas de verificación y recuperación.
4. En Configuración del proyecto > General, obtén la clave de API web de la aplicación registrada.
5. Desde la raíz del proyecto ejecuta:

```powershell
if(!(Test-Path config/firebase.properties)) {
    Copy-Item config/firebase.properties.example config/firebase.properties
}
```

6. Edita el archivo local:

```properties
firebase.apiKey=
admin.uid=
```

Solo en una instalación nueva, completa la clave con la configuración de tu propio proyecto. En este equipo ya está guardada. La plantilla conserva los valores vacíos; no contiene la clave real. Deja admin.uid vacío hasta escoger expresamente la cuenta administradora.

7. Ejecuta `.\mvnw.cmd javafx:run`, registra tu cuenta, abre el correo de verificación y vuelve a iniciar sesión.

Las claves de configuración Firebase identifican el proyecto; no son credenciales privadas de administrador. Revisa sus restricciones de API en la consola. No añadas credenciales de servicio ni Admin SDK al escritorio. [Información oficial sobre claves](https://firebase.google.com/docs/projects/api-keys).

## Primer registro en este equipo

1. Abre la carpeta raíz en VS Code y ejecuta la tarea **SupportFlow: ejecutar**, o usa `.\mvnw.cmd javafx:run "-Dsupportflow.demo=false"` en una terminal nueva con Java 21.
2. Comprueba que el título sea SupportFlow, sin DEMO, y que el acceso indique Firebase y correo verificado.
3. Pulsa Registrarse y escribe tu nombre, un correo al que tengas acceso y la contraseña dos veces. No compartas la contraseña en el chat ni en archivos.
4. Pulsa Registrarse, revisa tu bandeja de entrada y spam y abre el enlace de verificación.
5. Vuelve al acceso e inicia sesión con ese correo y contraseña. Si intentaste entrar antes de verificar, puedes pulsar Reenviar verificación y después Ya he verificado mi correo.
6. La primera cuenta normal entra como EMPLEADO. Cerrar sesión vuelve al acceso y descarta los datos de sesión en memoria.

El registro, la recepción del correo, el enlace y el acceso verificado se deben comprobar manualmente. La consulta HTTP 200 no demuestra esas operaciones.

## Instalar el primer ADMIN de forma explícita

1. Registra y verifica la cuenta que vayas a administrar personalmente.
2. En Authentication > Usuarios copia su UID, no su correo.
3. Cierra SupportFlow y escribe ese UID exacto en `admin.uid` del archivo local.
4. Reinicia en modo real e inicia sesión con esa misma cuenta.
5. Solo si Firebase confirma su identidad y correo verificado, y no existe ningún ADMIN local, se le concede ADMIN.
6. Comprueba el rol en el panel y después retira el UID del archivo de configuración.

Una primera cuenta desconocida cuyo UID no coincida sigue siendo EMPLEADO. No hay contraseña predeterminada ni elección de ADMIN durante el registro. Si ya hay un ADMIN, los cambios de rol se hacen desde Usuarios. No se permite degradar al último administrador.

## Flujo de autenticación

FirebaseAuth envía JSON mediante HttpClient y lee las respuestas con Gson:

| Operación | Endpoint REST |
|---|---|
| Registrar cuenta | accounts:signUp |
| Guardar nombre de perfil | accounts:update |
| Acceder | accounts:signInWithPassword |
| Consultar perfil y emailVerified | accounts:lookup |
| Verificar correo | accounts:sendOobCode con VERIFY_EMAIL |
| Recuperar contraseña | accounts:sendOobCode con PASSWORD_RESET |

La base de las peticiones reales es `https://identitytoolkit.googleapis.com/v1/`. Las contraseñas solo se envían por HTTPS a Firebase y se mantienen de forma transitoria en memoria. El cliente no permite configurar un servidor alternativo desde el archivo local. El constructor alternativo tiene acceso de paquete y sirve exclusivamente para las pruebas locales.

El correo no verificado impide crear la sesión local. Tras un intento de acceso puedes reenviar la verificación o pulsar «Ya he verificado mi correo». Un usuario verificado nuevo se guarda en SQLite como EMPLEADO, con su UID y datos complementarios.

Cerrar sesión descarta la sesión local y los datos de Firebase en memoria. La aplicación no conserva refresh tokens y exige volver a entrar cuando vence el token. Este cierre no revoca las sesiones de otros dispositivos.

Las peticiones se ejecutan con Task y Thread fuera del hilo gráfico. Existen tiempos de espera y mensajes para correo inválido, cuenta existente, contraseña débil o incorrecta, cuenta deshabilitada, exceso de intentos, red y sesión caducada.

Referencia de los endpoints: [API REST oficial](https://firebase.google.com/docs/reference/rest/auth).

## Comprobaciones manuales pendientes

La configuración ya está escrita. Con un correo y una contraseña que el alumno introduzca personalmente:

- Registrar una cuenta nueva y recibir su verificación.
- Confirmar que antes de verificar no entra ni se crea el registro local.
- Reenviar el correo, abrir el enlace y entrar como EMPLEADO.
- Comprobar contraseña incorrecta y cuenta ya existente.
- Solicitar recuperación, abrir el correo y entrar con la nueva contraseña.
- Instalar el ADMIN por UID y comprobar que otro usuario no recibe ese rol.
- Cerrar sesión y comprobar que ya no puede operar.
- Comprobar expiración y errores reales de red.
- Revisar remitente, idioma y carpeta de correo no deseado.

No uses cuentas o contraseñas personales en pruebas automáticas. Estas utilizan un servidor HTTP local de prueba con respuestas ficticias; no envían correos ni llaman a Google. El modo DEMO no realiza autenticación Firebase.

