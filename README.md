# GadgetGo

GadgetGo is an electronics repair marketplace in development, designed to help customers explore repair options for the devices they use every day. The customer homepage and service catalogue are connected to the Spring Boot API and the existing PostgreSQL database. Catalogue content is general information; it does not confirm a diagnosis, repair offer, booking, availability, or price.

## Stack

- Frontend: React, JavaScript, Vite, Tailwind CSS 4
- Backend: Java 21, Spring Boot, Maven, REST/JSON
- Data: PostgreSQL, Spring Data JPA/Hibernate, Flyway
- API docs: Springdoc OpenAPI
- Tests: JUnit 5 and Spring Boot Test

## Prerequisites

- Git
- Node.js 20.19+ or 22.12+ and npm
- Java 21
- Maven 3.6.3+ (Maven 3.10 is used in the current development environment)
- A running PostgreSQL server with the existing database named `gadgetgo`

## Project layout

```text
frontend/   React + Vite app
backend/    Spring Boot application
docs/       Architecture and configuration notes
```

## Configure the existing database

The backend targets the existing `gadgetgo` database at `jdbc:postgresql://localhost:5432/gadgetgo` by default. It does not create, drop, rename, or reset a database. Hibernate schema generation is disabled; Flyway is the schema migration mechanism. V1 adds the service catalogue and V2 adds customer accounts and gadgets using additive changes.

Copy `.env.example` to `.env` in the repository root and set `DB_USERNAME` and `DB_PASSWORD` to credentials that already have access to `gadgetgo`. Keep `.env` local; it is ignored by Git. Do not put database credentials in `VITE_` variables. If the database uses another host or port, set `DB_URL` accordingly. No database role or permissions are changed by this project. The local credential used during verification has broad superuser privileges; configure a least-privilege application role before any non-local deployment.

The existing database and catalogue are preserved. On startup, Flyway validates history before applying pending migrations. If the database has unexpected objects or a history conflict, stop and inspect before changing anything; never clean, baseline, drop, or reset the database as a troubleshooting shortcut. Do not edit applied migrations.

To inspect the current database with `psql`, these read-only commands prompt for the existing user's password without putting it in the command:

```powershell
psql -h localhost -p 5432 -U your_existing_database_username -d gadgetgo -W -c "SELECT current_database();"
psql -h localhost -p 5432 -U your_existing_database_username -d gadgetgo -W -c "\dt"
```

When running Maven from `backend/`, Spring Boot imports `../.env` as a properties file when present; ordinary process environment variables also work and take precedence. Alternatively, set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the shell before starting the backend. The Vite config reads the root `.env` with `envDir`; only `VITE_API_BASE_URL` is exposed to browser code.

Account-related settings are `BCRYPT_STRENGTH` (default 12), `SESSION_COOKIE_SECURE` (false for local HTTP; set true for HTTPS), and `FRONTEND_ORIGIN` (default `http://localhost:5173`). Sessions use an HTTP-only, SameSite=Lax cookie with a 30-minute inactivity timeout. Never put server secrets in `VITE_` settings.

## Run locally

### Backend

From `backend/`:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.12' # Set to your Java 21 installation
mvn spring-boot:run
```

The server listens on `http://localhost:8080`. Its startup requires reachable PostgreSQL and valid application credentials because Flyway and JPA initialize against the configured database.

### Frontend

From `frontend/`:

```powershell
npm install
npm run dev
```

The development server uses `http://localhost:5173`. Set `VITE_API_BASE_URL` in the root `.env` if the API uses another URL; the default is `http://localhost:8080/api`.

## Catalogue API

- `GET /api/catalog/categories` — active device categories.
- `GET /api/catalog/repair-types` — repair types represented by active services.
- `GET /api/catalog/services` — paginated service list. Optional query parameters: `q`, `category`, `type`, `sort`, `page`, and `size` (maximum 50). Supported `sort` values: `name`, `name-desc`, `type`, `newest`, and `oldest`.
- `GET /api/catalog/services/{slug}` — service details by stable slug.

The catalogue currently contains five device categories and 24 general service descriptions, inserted by V1. Search uses PostgreSQL full-text search. The browser keeps search and filter state in the URL. Brand compatibility, prices, ratings, technician availability, bookings, and diagnosis are not represented in this catalogue.

The frontend routes are `/`, `/services`, `/services/{slug}`, `/health`, `/register`, `/login`, `/account/profile`, `/account/gadgets`, `/account/gadgets/new`, and `/account/gadgets/{id}/edit`. The existing `/api/health` response describes application status only.

## Customer accounts and gadgets

Registration normalizes email by trimming and lowercasing with `Locale.ROOT`, assigns `CUSTOMER` server-side, and stores a BCrypt password hash. Login creates a server-side session. Session IDs and credentials are not stored in browser storage. Before state-changing requests, the frontend gets a CSRF token from `GET /api/auth/csrf` and sends it in `X-XSRF-TOKEN`. Login rotates the session and clears the previous CSRF token; logout invalidates the session. Local HTTP is not production-equivalent transport security.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/auth/csrf` | Obtain CSRF token/cookie |
| POST | `/api/auth/register` | Register customer |
| POST | `/api/auth/login` | Start session |
| POST | `/api/auth/logout` | Invalidate session |
| GET | `/api/auth/me` | Current signed-in customer |
| GET / PUT | `/api/customers/me` | Read/update own full name |
| GET / POST | `/api/gadgets` | List/register own gadgets |
| GET / PUT / DELETE | `/api/gadgets/{id}` | Read/update/delete owned gadget |

All gadget ownership is resolved from the authenticated backend principal; owner IDs are not accepted as writable fields. Reads and mutations are scoped by both gadget and customer IDs, and other customers' gadgets return 404. New gadgets must reference an active device category. Email changes, password recovery, email verification, bookings, and repair records are intentionally outside this stage.

## Technician and supplier access (Stage 5A)

Authentication remains one server-side Spring Security session system backed by `customer_accounts`; `JSESSIONID`, CSRF protection, and the customer/gadget foreign keys are preserved. The account role is restricted by the database to `CUSTOMER`, `TECHNICIAN`, `SUPPLIER`, or reserved `ADMIN`. Public registration still assigns `CUSTOMER` server-side and has no role field. Provider login uses the shared password encoder/session mechanism, with role-specific endpoints and Spring Security role checks for `/api/technician/**` and `/api/supplier/**`. Provider profiles must also be active for login and are rechecked on each authenticated request. No public provider provisioning endpoint exists.

Migration `V3__extend_roles_and_add_provider_profiles.sql` additively extends the role constraint and creates `technician_profiles`, `technician_service_modes`, `technician_skills`, and `supplier_profiles`. Technician skill rows reference the existing repair-service catalogue. Supplier inventory is deliberately deferred: the project does not yet define part compatibility, units, supplier pricing, or stock change rules, so there are no inventory tables or fake stock summaries in this stage. No applied migration was edited.

Technician endpoints are `POST /api/auth/login/technician`, `GET /api/technician/me`, and `GET /api/technician/skills`. Supplier endpoints are `POST /api/auth/login/supplier` and `GET /api/supplier/me`. Shared `/api/auth/logout`, `/api/auth/csrf`, and `/api/auth/me` continue to apply. Provider profile data is read-only until a controlled administration process exists. Frontend routes are `/technician/login`, `/technician/dashboard`, `/technician/profile`, `/supplier/login`, `/supplier/dashboard`, and `/supplier/profile`.

### Optional local demo provider accounts

Demo provisioning is disabled by default and only loads under the `local` Spring profile. For a local demo, set `DEMO_ACCOUNTS_ENABLED=true`, `SPRING_PROFILES_ACTIVE=local`, and environment-supplied `DEMO_TECHNICIAN_*` and `DEMO_SUPPLIER_*` values in the ignored root `.env` or process environment. Required values are each provider's `EMAIL`, `PASSWORD` (10–72 UTF-8 bytes), and `NAME`; also configure `DEMO_SUPPLIER_BUSINESS_NAME`. Technician service-area/mode/repair-service IDs and supplier contact email are optional. Modes are `HOME_VISIT` and/or `REPAIR_CENTRE`; service IDs must reference active catalogue rows. Use unique local demo email addresses and non-production passwords. Provisioning hashes passwords and never logs them. It is idempotent and does not reactivate or overwrite an existing profile. Do not enable this mechanism in production.

If provisioning is enabled, missing required settings abort application startup rather than creating a partial account. `.env.example` lists variable names and placeholders only; replace them locally and never commit `.env`.

## Checks

```powershell
# Frontend, from frontend/
npm test
npm run build

# Backend, from backend/
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.12'
mvn test
mvn package
```

Backend integration tests run against the separate `gadgetgo_stage3_test` database by default (`TEST_DB_URL` can override the URL). A test initializer blocks the protected `gadgetgo` database name before Flyway starts. Configure any override only to a disposable isolated database; tests do not provision or drop databases.

With both services running, open `http://localhost:5173/services` to browse the API-backed catalogue. Search, filters, pagination, and detail pages use the local backend. Open `/health` to view `GET /api/health`, or request it directly:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

The response reports application status only; it does not claim that the database is healthy. OpenAPI UI is available at `http://localhost:8080/swagger-ui.html` when the backend is running.

## Troubleshooting

- **Backend cannot connect to PostgreSQL:** confirm the PostgreSQL server is running and listening at `DB_URL`, then verify that `DB_USERNAME` and `DB_PASSWORD` are valid and have access to the existing `gadgetgo` database. A connection-refused error usually means the host/port is unavailable; an authentication error means the server was reached but rejected credentials.
- **Database name error:** ensure the configured URL ends with `/gadgetgo`. The application does not provision databases.
- **Flyway reports a failed or conflicting migration:** preserve existing objects and migration history. Inspect the error and schema; do not baseline, clean, drop, or reset them as a shortcut.
- **Backend tests report `gadgetgo_stage3_test` missing:** create that isolated test database or set `TEST_DB_URL` to a disposable test database. Do not point tests at user data.
- **Frontend reports API unavailable:** start the backend successfully first and check `VITE_API_BASE_URL`; local CORS permits the exact origin `http://localhost:5173`.
- **Java version mismatch:** ensure Maven uses a Java 21 JDK. On Windows, set `JAVA_HOME` to the actual JDK directory before invoking Maven.

## Phase boundary

Implemented in Phase 0 and Phase 1 Stages 2–5A: React/Vite/Tailwind homepage and catalogue, database-backed search/filter/sort/pagination and details, customer registration/login/logout with server-side sessions and CSRF, customer profile basics and owned gadget CRUD, additive provider profile migrations, controlled local provider provisioning, shared role-aware authentication, backend provider authorization, and read-only technician/supplier dashboards. The application-only health endpoint is preserved.

Deferred features include repair bookings, technician matching, assignment offers, diagnoses and quotations, supplier inventory/parts procurement, payments, repair tracking/delivery, and password reset/email verification. Technician service area, modes, capacity, and availability in Stage 5A are configuration only; they are not live matching or workload data.
