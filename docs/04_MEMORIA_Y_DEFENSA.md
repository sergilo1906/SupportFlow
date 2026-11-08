# 04 · Memoria, pruebas y defensa

Adaptar el documento a las instrucciones oficiales del tutor. Sigue pendiente confirmar la adecuación académica de JavaFX al M13.

## Índice de memoria

1. Objetivo, contexto y problema de la empresa ficticia.
2. Requisitos, roles, casos de uso y exclusiones.
3. Java 21, JavaFX/FXML, SQLite/JDBC y Firebase REST.
4. Diagrama de clases y cinco tablas con sus relaciones.
5. Desarrollo: modelos, DAO, servicios, autenticación y pantallas.
6. Pruebas ejecutadas y resultados, separando simulaciones de conexión real.
7. Instalación y manual de usuario con capturas propias.
8. Horas efectivas, dificultades, conclusiones y mejoras.
9. Fuentes y declaración de asistencia de IA.

## Evidencias conservadas

El 12/10/2026 se verificó la base inicial con ocho pruebas y ventana JavaFX. El 01/11/2026 se completaron UPDATE y DELETE del DAO y se comunicaron doce pruebas superadas. El 07/11/2026 se han verificado 47 casos, incluidos servicios, migración, Firebase simulado y botones FXML. Los resultados actuales se reproducen siguiendo [PRUEBAS.md](PRUEBAS.md).

La guía técnica para estudiar cada clase está en [GUIA_ESTUDIO.md](GUIA_ESTUDIO.md). No sustituye las conclusiones y explicaciones personales de la memoria. La preparación y ampliación se han realizado con asistencia de IA y deben declararse; no son horas personales medidas del alumno.

## Manual y demo de 8–10 minutos

Usar el modo DEMO identificado y su archivo separado. Presentar el problema y tecnologías; registrar ticket como empleado; asignar como ADMIN; comentar y resolver como TECNICO; mostrar inventario, baja sin pérdida histórica, usuarios y estadísticas; enseñar una prueba y concluir con las limitaciones.

La DEMO permite ese recorrido local, pero no autentica con Firebase. La entrega real de correos y acceso al proyecto Firebase siguen pendientes de configuración y prueba manual. No afirmar su éxito durante la defensa sin evidencia posterior.

## Preguntas clave

- ¿Cómo se conectan FXML, controlador, servicio y DAO?
- ¿Qué diferencia hay entre id local y Firebase UID?
- ¿Dónde se comprueban rol y acceso al ticket?
- ¿Qué protegen PreparedStatement y las claves foráneas?
- ¿Por qué ticket e historial utilizan una transacción?
- ¿Qué limita la seguridad de SQLite local?
- ¿Qué se ha probado realmente y qué es una simulación?
- ¿Qué código se ha desarrollado con asistencia y qué ha estudiado o modificado personalmente el alumno?

Preparar respuestas propias, tiempos reales y capturas. El tutor no ha aprobado el proyecto por el hecho de que compile o pase pruebas.
