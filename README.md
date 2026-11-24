# SupportFlow

A desktop helpdesk and asset inventory application built with Java 21, JavaFX (FXML), SQLite and JDBC. It models the day-to-day work of an IT support team: logging incidents, assigning tickets, tracking their status and keeping a record of equipment.

Tickets have priorities, comments and a history of changes. Inventory items can be assigned or retired. The application also includes local user roles, permission checks and a statistics view.

## Try the demo

Install JDK 21 and open a terminal in the cloned repository. Maven is included through the wrapper.

On Windows:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd javafx:run "-Dsupportflow.demo=true"
```

Choose a sample user on the login screen. No Firebase account or password is needed for the demo. Changes are stored in `data/supportflow-demo.db`.

## How the application is organised

The JavaFX controllers handle the screens, the service layer contains business rules and permissions, and the DAO layer handles SQL through JDBC. The database schema and FXML layouts are under `src/main/resources/`. Tests in `src/test/` cover the local database, permissions and simulated authentication.

Firebase Authentication is available through a REST client. To configure it on another machine, follow [the Firebase guide](docs/FIREBASE.md). Real registration, login and email delivery still need manual end-to-end verification.

This is a local desktop project, not a production multi-user helpdesk. SQLite files are not a security boundary. No installer or Linux/macOS validation is provided.

Further notes: [database](docs/BASE_DATOS.md), [tests](docs/PRUEBAS.md), [development guide](docs/GUIA_ESTUDIO.md). Originally developed as a DAM project prototype with AI-assisted development; see the academic documentation.
