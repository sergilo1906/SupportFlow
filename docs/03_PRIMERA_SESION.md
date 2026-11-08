# 03 · Sesión de estudio y comprobación

## Confirmación académica pendiente

Consultar al tutor del Institut Baix Camp la aceptación de JavaFX como aplicación de escritorio para M13, rúbrica, plazo, modalidad individual, idioma, formato y condiciones de asistencia de IA. La planificación de 100 horas no altera los requisitos oficiales.

## Abrir el proyecto existente

Abrir C:\Proyectos\SupportFlow en VS Code, aceptar la extensión Java recomendada y abrir una terminal nueva. Debe utilizar Java 21; los ajustes para una terminal externa están en el README.

```powershell
java -version
.\mvnw.cmd --version
.\mvnw.cmd clean test
.\mvnw.cmd javafx:run "-Dsupportflow.demo=true"
```

No crear de nuevo el repositorio ni las tablas a mano. La inicialización es repetible. Los informes están en target/surefire-reports y no se suben a Git.

## Recorrido para estudiar

1. Entrar en DEMO como empleado y observar solo sus datos.
2. Crear una incidencia, consultarla y añadir un comentario.
3. Cerrar sesión; entrar como ADMIN y asignarla al técnico.
4. Entrar como TECNICO, cambiar estado y revisar el historial.
5. Entrar como ADMIN, consultar estadísticas y probar inventario.
6. Cerrar la ventana y volver a abrir para comprobar persistencia de DEMO.

Para estudiar el borrado, usar solo datos creados en DEMO. La base real no sirve como archivo de pruebas. Las cuentas DEMO no están autenticadas por Firebase.

## Siguiente trabajo personal

Leer [GUIA_ESTUDIO.md](GUIA_ESTUDIO.md), seguir un botón hasta su servicio y DAO y explicar cada línea. Después configurar Firebase siguiendo [FIREBASE.md](FIREBASE.md) y registrar las pruebas reales sin confundirlas con las simuladas.

Registrar duración efectiva de estudio y programación. Revisar Git antes de cualquier commit y realizarlo solo cuando el alumno lo decida. Declarar la asistencia recibida conforme a las normas del centro.
