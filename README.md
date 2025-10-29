# Wealth-Wise: Smart Personal Finance & Investment Manager

## Overview
Wealth-Wise is a **Java 17** desktop application built with **JavaFX** that helps individuals understand, track, and grow their finances. The app combines personal budgeting, transaction management, and investment insights into a single, user-friendly dashboard. With rich data visualizations, automated alerts, and tailored investment guidance, users can make confident financial decisions every day.

## Key Features
1. **Secure Authentication** — JavaFX login and registration screens backed by hashed password storage.
2. **Transaction Management** — Add, edit, delete, and categorize income and expense records with calendar-aware forms.
3. **Budget Planning & Alerts** — Define monthly budgets per category and receive alerts when spending drifts off-plan.
4. **Intelligent Dashboards** — Visualize cash flow, spending trends, and savings with interactive JavaFX charts.
5. **Recurring Expense Detection** — Identify repeating payees and frequency patterns automatically.
6. **Investment Toolkit**
   - Risk profiling via in-app questionnaire.
   - Personalized stock and ETF recommendations with momentum and volatility analytics.
   - Trending market movers with suitability scores for each risk profile.
7. **Data Export** — Generate CSV and PDF reports for taxes, audits, or advisors.
8. **Smart Notifications** — Desktop alerts and in-app reminders for budgets, investments, and portfolio changes.

## Tech Stack
- **Language**: Java 17+ (Maven compiler target `release 24`).
- **UI**: JavaFX (`javafx-controls`, `javafx-fxml`, `javafx-graphics`).
- **Persistence**: SQLite via `sqlite-jdbc` (local). Optional cloud deployment with MySQL/PostgreSQL.
- **Data Processing**: `org.json`, custom analytics (volatility, momentum scoring), optional Weka integration.
- **Reporting**: OpenCSV (CSV export), Apache PDFBox / iText (PDF reports).
- **Scheduling & Alerts**: `ScheduledExecutorService`, JavaFX `Alert`, and `SystemTray` notifications.
- **Build Tool**: Maven 3.9+.

## Project Structure
```
wealth-wise/
├── src/main/java/com/smartfinance/
│   ├── App.java                      # JavaFX entry point
│   ├── auth/                         # Login & registration controllers
│   ├── dashboard/                    # Dashboard controllers & chart helpers
│   ├── model/                        # Domain models (User, Transaction, RiskProfile, ...)
│   ├── service/                      # Finance, investment, scheduling, export services
│   └── util/                         # Database helpers, CSV/PDF utilities, shared logic
├── src/main/resources/
│   ├── Fxml/                         # FXML layouts for scenes
│   ├── Styles/                       # CSS themes
│   └── application.properties        # App-level configuration
├── tests/                            # Functional & regression harnesses (PowerShell/BAT + Java)
└── pom.xml                           # Maven configuration
```

## Getting Started
1. **Install prerequisites**
   - JDK 17 or later (tested with JDK 24).
   - Maven 3.9 or newer.
   - SQLite (optional CLI for inspecting the database).
2. **Clone the repository**
   ```powershell
   git clone https://github.com/<your-org>/wealthwise.git
   ```
3. **Install dependencies & build**
   ```powershell
   mvn clean install
   ```
4. **Run the JavaFX application**
   ```powershell
   mvn clean javafx:run
   ```
5. **Execute unit & integration tests**
   ```powershell
   mvn clean test
   ```

## Configuration
- **Database**: Default configuration uses a local SQLite file. See `FIX_DATABASE_SCHEMA.md` for schema maintenance tips and `FOREIGN_KEY_FIX.md` for migration guidance.
- **API Keys**: Investment features fetch market data (e.g., Yahoo Finance, Finnhub). Store API credentials in environment variables or a secured properties file before running those modules.
- **Logging**: Configured via SLF4J + Logback. Adjust logging levels in `src/main/resources/logback.xml`.

## Financial Intelligence Modules
- **Risk Analyzer** — Evaluates user responses to assign Conservative, Moderate, or Aggressive profiles.
- **Investment Service** — Calculates annualized volatility, momentum, and suitability for curated watchlists.
- **Auto-Invest Planner** — Uses surplus calculations to suggest monthly investment allocations based on risk profile.

## Roadmap
1. **Phase 1: Core Personal Finance**
   - Solidify transaction CRUD, budgeting, and dashboards.
2. **Phase 2: Advanced Automation**
   - Expand recurring expense detection, add richer notifications, improve exports.
3. **Phase 3: Investment Intelligence**
   - Integrate real-time alerts, enhance recommendation engine, introduce educational content.

## Contributing
1. Fork the repository and create a feature branch (`feature/my-improvement`).
2. Follow the existing code style and add JUnit coverage for new features.
3. Submit a pull request referencing any related issues.

## Troubleshooting
- **Database migration issues**: Run scripts in `tests/` or follow the steps in `FIX_DATABASE_SCHEMA.md` and `QUICK_FIX_GUIDE.md`.
- **JavaFX runtime errors**: Ensure `javafx.controls`, `javafx.fxml`, and `javafx.graphics` modules are present in your Java runtime path.
- **API rate limits**: Use cached responses or configure scheduling intervals via `ScheduledExecutorService` to prevent throttling.

---

**Wealth-Wise empowers users to master budgeting and investment planning through actionable intelligence.** Contributions, suggestions, and feedback are always welcome!

