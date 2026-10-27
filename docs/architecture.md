# Initial architecture and configuration

## Runtime shape

The browser runs a React + Vite single-page application. Its minimal API client sends JSON requests to a Spring Boot REST service. The API is organized for future `controller → service → repository → database` layering; the initial health endpoint has no business or persistence logic.

PostgreSQL is the existing `gadgetgo` database. Spring Data JPA/Hibernate provides persistence support. Hibernate DDL generation is disabled, and Flyway is the only schema migration mechanism. Stage 3 migration V1 adds `device_categories` and `repair_services`; Stage 4 migration V2 additively adds `customer_accounts` and `customer_gadgets`. No booking, technician, quotation, or repair-tracking tables exist.

Before Stage 3 migration, the existing `gadgetgo` database was inspected read-only. The public schema contained only an empty Flyway history table. V1 was then applied once by Flyway and validated at startup. Preserve this database and history; do not baseline, clean, drop, or reset it to resolve migration errors.

## Local configuration

`DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` are server-side settings. Spring Boot reads process environment variables and optionally imports the repository root `.env` file while running from `backend/`. The Vite config also reads root `.env`, but only exposes `VITE_` variables; database values are never referenced by frontend code. `.env` is ignored by Git and `.env.example` contains placeholders only. The local credential used for verification had PostgreSQL superuser and `CREATEDB` privileges; no roles or grants were changed. Use a separate least-privilege application role before deployment.

The backend permits only the configured `FRONTEND_ORIGIN` (default `http://localhost:5173`) and uses credentialed CORS. The frontend API base URL is `VITE_API_BASE_URL`; database settings remain server-only. Sessions use an HTTP-only, SameSite=Lax cookie, 30-minute inactivity timeout, and configurable Secure flag (`SESSION_COOKIE_SECURE`, false only for local HTTP). BCrypt strength is configurable (`BCRYPT_STRENGTH`, default 12).

## API endpoints

`GET /api/health` returns application status and deliberately does not claim database health.

The catalogue API is read-only:

- `GET /api/catalog/categories` returns active device categories.
- `GET /api/catalog/repair-types` returns repair types present in active services.
- `GET /api/catalog/services` supports `q`, `category`, `type`, `page`, `size`, and the whitelisted sort values `name`, `name-desc`, `type`, `newest`, and `oldest`.
- `GET /api/catalog/services/{slug}` returns one active service.

Responses use DTO records. Service pages are bounded to 50 results, filter out inactive services/categories, return stable ID tie-breaks, and include page metadata. Search uses PostgreSQL full-text search over service text and category text. Error responses use Spring `ProblemDetail` with a readable `detail`; invalid filters return 400 and missing/inactive details return 404.

## Customer accounts and gadget access

Cookie-based server-side sessions use Spring Security. The frontend requests `/api/auth/csrf` and sends the returned token in `X-XSRF-TOKEN` for state-changing requests. Registration and login are public but CSRF-protected. Successful login changes the session ID, stores the security context, and clears the previous CSRF cookie. Logout invalidates the server session and deletes its cookies. The frontend includes credentials and stores no session identifier or token in browser storage.

Registration uses an explicit DTO without a role field, normalizes emails by trimming and lowercasing with `Locale.ROOT`, and hashes passwords with BCrypt. Profile responses expose ID, full name, email, and role; only the full name is editable. Gadget ownership comes from `CustomerPrincipal`; list/read/update/delete queries scope each gadget by owner and ID. Another customer's gadget returns 404. Active service categories are required for new registrations and a foreign key preserves referential integrity. The gadget record has no serial number, booking, diagnosis, technician, or repair state.

Migration `V2__create_customer_accounts_and_gadgets.sql` is additive after V1. Email normalization and uniqueness are enforced by application normalization and a normalized database unique index/checks. Gadget constraints cover nonblank brand/model and dates; customer/category foreign keys and customer/category indexes support integrity and owner lists. Do not edit applied migrations; evolve through the next additive Flyway migration.

Customer endpoints: `GET /api/auth/csrf`, `POST /api/auth/register`, `POST /api/auth/login`, authenticated `POST /api/auth/logout`, authenticated `GET /api/auth/me`, `GET/PUT /api/customers/me`, `GET/POST /api/gadgets`, and `GET/PUT/DELETE /api/gadgets/{id}`. Every mutation requires CSRF. Security independently enforces authentication for private paths. Controllers use DTOs, services own rules/transactions, repositories handle persistence, and safe `ProblemDetail` responses carry errors. Email edits, email verification, and password recovery are not implemented.

## Customer homepage foundation

The React entry renders a responsive customer-facing homepage at `/`. Shared header, footer, buttons, section headings, icons, and an informational dialog are componentized. The existing API health view remains available at `/health` and calls the unchanged `/api/health` endpoint. Tailwind CSS 4 design tokens are defined in the CSS-first theme in `frontend/src/styles.css`.

The customer homepage uses the API for categories and featured services. `/services` reads URL parameters for search, category, repair type, sorting, and page; service cards link to `/services/{slug}`. Service detail pages provide only catalogue descriptions and avoid promises about diagnosis, price, availability, or booking. `/register` and `/login` provide account flows; protected account pages display profile and customer-owned gadgets. The API client centralizes JSON, cookies, CSRF, and error handling. The original GadgetGo repair-workbench concept image is stored locally at `frontend/public/images/repair-hero.png`.

## Current verification boundary

Frontend tests use Vitest, React Testing Library, and jsdom. Backend integration tests apply Flyway and exercise the API against `gadgetgo_stage3_test` by default; an initializer blocks use of the protected database name `gadgetgo`. A production build verifies compilation and bundling, not visual quality. Review routes at desktop and mobile widths. The `/health` page calls the real backend endpoint.

## Compatibility choices

The project targets Java 21 with Spring Boot 3.5.x and Maven. The frontend uses Vite 7 and Tailwind CSS 4 through the official Vite plugin. Versions are pinned in the Maven/npm manifests and npm lockfile for reproducible installs.

## Stage 5A provider identity extension

Customer accounts remain the single authentication identity table. The `customer_accounts.id` primary key and gadget ownership FK are unchanged. Migration `V3__extend_roles_and_add_provider_profiles.sql` adds an account active flag, expands the role check for `TECHNICIAN`, `SUPPLIER`, and reserved `ADMIN`, and adds one-to-one technician/supplier profiles. Technician service modes use a constrained collection table; technician skills link to the existing `repair_services` table. Supplier inventory is omitted until its part, compatibility, unit, and stock semantics are defined.

All roles authenticate through the existing `UserDetailsService`, BCrypt encoder, Spring Security session, CSRF token repository, and auth controller. Customer registration has no role input and creates only `CUSTOMER`. Provider login endpoints share authentication logic and require the expected role; protected provider APIs also enforce `ROLE_TECHNICIAN` or `ROLE_SUPPLIER` server-side. Provider active status is checked during authentication and on each authenticated request, invalidating sessions after deactivation. Existing customer sessions and gadgets are not migrated to new identity IDs.

The only provider provisioning mechanism is `DemoAccountProvisioner`, conditional on both the `local` profile and `DEMO_ACCOUNTS_ENABLED=true`. It requires credentials from local environment variables, hashes them with the configured BCrypt encoder, refuses role collisions, and leaves existing records unchanged. Provisioning is disabled by default. It does not support production administration. Profile endpoints are read-only in this stage; no public mutation endpoints can alter role, account linkage, active status, skills, or permissions.

Provider endpoints: `POST /api/auth/login/technician`, `POST /api/auth/login/supplier`, `GET /api/technician/me`, `GET /api/technician/skills`, and `GET /api/supplier/me`. Shared auth endpoints and customer API contracts remain. Frontend portal routes exist for each provider's login, dashboard, and profile. Supplier dashboard explicitly reports that inventory functionality is not implemented. Technician capacity and availability are configured fields, not a matching algorithm or measured workload.

Test migration V3 applies to the isolated `gadgetgo_stage3_test` database. The test initializer rejects the protected `gadgetgo` database name before Flyway starts. Keep all future migrations additive and do not edit V1/V2 or alter Flyway history.
