# Emergency SOS and Contact Alert Simulator

A web-based emergency assistance **simulator** built with core Java (backend) and HTML, CSS and
JavaScript (frontend). A user registers, saves emergency contacts, triggers an SOS alert, chooses an
emergency category, records the incident and keeps a history of every alert.
**It is a simulation: no real SMS or call is ever sent.**

Course: Programming in Java (25CSE0202), 3rd semester group project.
Stage 1 (this folder): complete frontend + backend, data saved in CSV files.
Stage 2: the CSV repositories are replaced by JDBC + MySQL repositories.

## Features
Backend (core Java)
- User registration and login (sessions)
- Emergency contact management: add, edit, delete, priority 1-5, duplicate phone check, max 10
- **Searching contacts** by name, relation or phone (Java search, case-insensitive)
- SOS trigger with emergency category, location and incident details
- **Alert message** generated per category (this is the text "sent" to every contact)
- Contacts are notified in priority order by a background thread: PENDING -> DELIVERED
- **Alert log** with date, time, status (Pending / Delivered / Active / Resolved) and message
- "I am safe": mark an alert as resolved
- Incident history: category filter, keyword search, last N, per-category statistics
- Data saved in CSV files (`data/`), reloaded when the program starts

Frontend (HTML, CSS, JavaScript only)
- Responsive layout (sidebar on desktop, bottom navigation on mobile), light and dark theme
- Dashboard with readiness status, stat cards, category bars and recent alerts
- SOS page: category cards, safety tip, use my location, quick-text chips, who will be notified,
  5-second cancel countdown, live delivery tracker, message preview, copy summary
- Contacts page: search, add/edit popup, priority selector, call button, delete confirmation
- History page: timeline with date and time, filters, search, mark resolved, export CSV
- Helplines page and safety checklist, toasts, loading skeletons, password strength meter

## How to run
Needs only a JDK (version 11 or newer). Check with `javac -version`.
- Windows: double-click `run.bat`
- Linux / Mac: `./run.sh`
Then open http://localhost:8080 in a browser.

Manual way (from this folder):
    javac -d out -sourcepath src src/com/sos/Main.java
    java -cp out com.sos.Main

Always start the program from this project folder (it reads `web/` and writes `data/` there). If the folder
is wrong the program now stops with a clear message. In an IDE, set the run configuration's
"Working directory" to this folder.

## Architecture
    Browser (HTML / CSS / JS)
            |  form requests (fetch)
            v
    server/      small HTTP bridge: AppServer, BaseHandler, Auth/Contact/Alert/StaticFile handlers
            |
            v
    service/     business rules: AuthService, ContactService, AlertService, SessionManager,
                 SharedQueue<T> + DispatcherThread (background notifications)
            |
            v
    repository/  Repository<T> (generic interface) -> CsvRepository<T> -> File*Repository
                 (Stage 2: Jdbc*Repository implements the same interfaces)
            |
            v
    model/       User, Contact, Alert, Delivery, Emergency (abstract) + 7 subclasses, EmergencyFactory

    util/        Json, CsvUtil, Validator          exception/   AppException + 4 custom exceptions

## Project structure
    src/com/sos/Main.java            creates every object and connects them (manual dependency injection)
    src/com/sos/model|repository|service|server|util|exception
    web/                             index.html, dashboard.html, css/style.css, js/common.js, index.js, dashboard.js
    data/                            CSV files (created automatically)

## API
    POST   /api/auth/register | /api/auth/login | /api/auth/logout
    GET    /api/contacts?q=word     POST /api/contacts
    PUT    /api/contacts/{id}       DELETE /api/contacts/{id}
    GET    /api/alerts?category=X&q=word&limit=N      POST /api/alerts
    PUT    /api/alerts/{id}/resolve
    GET    /api/alerts/categories   GET /api/alerts/stats

## Stage 2 plan (JDBC)
Tables: `users`, `contacts` (user_id -> users), `alerts` (user_id -> users, category),
`alert_deliveries` (alert_id -> alerts). Write `JdbcUserRepository`, `JdbcContactRepository` and
`JdbcAlertRepository` (PreparedStatement CRUD, ResultSet mapping to objects) implementing the same
three interfaces, then change the 3 repository lines in `Main.java`. Services, threads, server and the
whole `web/` folder stay unchanged.

## Resume description
**Emergency SOS and Contact Alert Simulator** | Java, HTML, CSS, JavaScript, CSV file storage (JDBC + MySQL planned for Stage 2)
- Built a full-stack emergency alert simulator with a layered architecture (model, repository,
  service, web layer) using core Java and OOP: abstraction, inheritance, polymorphism, interfaces, generics.
- Designed an `Emergency` class hierarchy (7 categories) with category-specific severity, safety advice
  and contact instructions, created through a factory.
- Implemented priority-based alert dispatch with `PriorityQueue`, a producer-consumer buffer using
  `wait()/notifyAll()` and a background daemon thread that updates delivery status in real time.
- Implemented contact search, alert history with Stream API filtering/sorting, per-category statistics
  (`TreeMap`) and date/time handling.
- Persisted data with file handling (CSV, UTF-8) behind a generic repository interface so the storage can be
  swapped to JDBC (Stage 2) without touching business logic.
- Created a responsive, accessible dashboard in vanilla JavaScript: live status tracking, dark mode,
  modal dialogs, search, CSV export.

## Note
The browser cannot call Java directly, so the JDK's built-in `com.sun.net.httpserver.HttpServer` is used
inside the `server` package only. No external server, framework or library is used anywhere.
