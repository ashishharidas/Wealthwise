# Repository Overview

## Tech Stack
- **Language**: Java (Maven compiler target `release` 24)
- **UI**: JavaFX (`javafx-controls`, `javafx-fxml`, `javafx-graphics`)
- **Persistence**: SQLite via `sqlite-jdbc`
- **Utilities**: OpenCSV for CSV export, `org.json` for JSON handling, FormsFX and FontAwesomeFX for UI components
- **Testing**: JUnit 5 (Jupiter API + Engine)
- **Notification Hooks**: `Model.notifyClientDataRefreshed()` available for controllers that need refresh callbacks (e.g., `TransactionController`, `profileController`).

## Build & Run
1. **Install prerequisites**: JDK 24 (or compatible) and Maven 3.9+
2. **Build project**:
   ```bash
   mvn clean install
   ```
3. **Run desktop app (JavaFX)**:
   ```bash
   mvn clean javafx:run
   ```
4. **Execute automated tests**:
   ```bash
   mvn clean test
   ```

## Key Modules & Packages
- **`com.smartfinance.App`**: JavaFX entry point specified in `pom.xml`.
- **`com.smartfinance.Controller`**: UI controllers for client/admin dashboards and authentication flows.
- **`com.smartfinance.Models`**: Domain models (e.g., `Client`, `Budget`, `Transaction`) and data access helpers.
- **`com.smartfinance.Views`**: Custom UI components, cell factories, and enumerations for view logic.
- **`src/main/resources/Fxml`**: JavaFX layout descriptors for screens (login, dashboard, etc.).
- **`src/main/resources/Styles`**: CSS stylesheets for theming.

## Data & Configuration Notes
- SQLite database usage is implied; ensure the database file and schema are accessible before running.
- Review utility documents (e.g., `FIX_DATABASE_SCHEMA.md`, `FOREIGN_KEY_FIX.md`) for schema maintenance guidelines.

## Testing Utilities
- Custom test harness scripts reside in the `tests/` directory (PowerShell/Bat and Java helpers) for client list and transaction scenarios.