# GadgetGo Phase 1, Stage 1 — Repository Audit and UX Plan

**Status:** planning only. This report records verified repository facts separately from recommendations. No Phase 1 feature, database migration, or application-code change was made for this audit.

## 1. Executive summary

GadgetGo is a clean Phase 0 foundation, not yet a marketplace. The repository contains a minimal React/Vite/Tailwind health page and a Spring Boot JSON health endpoint. It has database configuration, CORS for the current local frontend origin, Flyway, JPA, OpenAPI UI, root engineering instructions, and setup documentation. It contains no customer accounts, catalogue, service search, routes, gadgets, business entities, repositories, or migrations.

The current foundation has been exercised against the existing `gadgetgo` database: Spring imports the root `.env` while launched from `backend/`, Flyway starts and creates its metadata table, the web app starts, and the React health check reaches the running backend. The `public` schema had no application tables at inspection time. There are no Phase 0 migration scripts. Phase 1 should preserve these working behaviors while adding the customer marketplace in small checkpoints.

**Recommended product direction:** a calm, editorial consumer-tech marketplace: warm neutral canvas, deep navy text and navigation, a distinctive lime accent used sparingly, clear repair-service discovery, and evidence-based trust copy. Keep the current React, JavaScript, Tailwind CSS 4, Spring Boot, Maven, PostgreSQL, and Flyway stack. Use a modular monolith and a stateful, secure Spring Security session for the first-party web app unless a mobile or third-party API requirement changes that decision.

## 2. Verified repository status

### Git and instructions

- The project root is `C:\GadgetGo` and Git is initialized on `master` with **no commits yet**.
- `git status --short --branch` reports the Phase 0 root files and project directories as untracked. There is no tracked baseline to restore; retain all current files and treat the existing working tree as user work.
- There is one instruction file, root `AGENTS.md`; no nested `AGENTS.md` was found. It requires incremental changes, preserved behavior, Java 21/Spring/React/PostgreSQL, clean layering, DTOs, validation, backend authorization when added, tests, and secret hygiene.
- `.gitignore` ignores `.env`, environment variants except `.env.example`, Maven `target`, Node dependencies and output, IDE/OS files, and logs. The actual `.env` is ignored. The report does not reveal its values.

### Verified installed and declared versions

| Area | Verified version |
|---|---|
| Node.js / npm | 22.20.0 / 10.9.3 |
| React / React DOM | 19.1.1 / 19.1.1 |
| Vite | 7.3.7 |
| Tailwind CSS / Vite plugin | 4.1.14 / 4.1.14 |
| React Vite plugin | 5.0.4 |
| Java / Maven | Java 21.0.12 / Maven 3.10.0 |
| Spring Boot | 3.5.6, set in `backend/pom.xml` and observed at runtime |
| Springdoc OpenAPI | 2.8.13 |
| Flyway core / PostgreSQL module | 11.7.2 / managed PostgreSQL module |
| Hibernate ORM / Spring Data JPA | 6.6.29.Final / 3.5.4 |
| PostgreSQL JDBC driver | 42.7.7 |

The Maven dependency versions above are from the local resolved artifacts and Phase 0 runtime logs; Spring Boot manages versions for dependencies that are not explicitly versioned in the POM. `frontend/package-lock.json` is present, and `npm ls --depth=0` confirms the installed direct frontend dependencies. Do not upgrade these versions as part of the Phase 1 implementation without a separate compatibility reason.

### Files actually present

```text
AGENTS.md
README.md
.gitignore
.env.example
docs/architecture.md
frontend/package.json
frontend/package-lock.json
frontend/index.html
frontend/vite.config.js
frontend/src/{main.jsx,App.jsx,api.js,styles.css}
backend/pom.xml
backend/src/main/resources/application.properties
backend/src/main/java/com/gadgetgo/GadgetGoApplication.java
backend/src/main/java/com/gadgetgo/config/WebConfig.java
backend/src/main/java/com/gadgetgo/api/health/HealthController.java
backend/src/test/java/com/gadgetgo/api/health/HealthControllerTest.java
```

No frontend router, additional page, reusable marketplace component, backend service/repository/entity, application migration, or frontend test suite exists.

## 3. Existing functionality versus placeholders

| Area | Verified current behavior | Placeholder or absent |
|---|---|---|
| React app | One temporary “Development setup” view. `App.jsx` fetches `/api/health`, shows loading/error/ready states, and says this is not a database-health check. | No homepage, consumer navigation, route handling, catalogue, account state, profile, or gadget UI. |
| Frontend API | `src/api.js` uses `VITE_API_BASE_URL`, falling back to `http://localhost:8080/api`, and performs JSON health fetch. | No common API error format, auth/session handling, catalogue client, caching, or form API. |
| Styling | Tailwind CSS 4 is loaded via `@import "tailwindcss"` and `@tailwindcss/vite`; utility classes are used for the temporary view. | No design tokens, component variants, Tailwind config file, typography scale, or branded marketplace system. |
| Routing | None; `main.jsx` renders `App` directly. | Every planned customer route. |
| Backend API | `GET /api/health` returns `{ "application": "GadgetGo API", "status": "UP" }`. | No catalogue or customer API, request DTOs, service layer, repository, or business error handler. |
| Persistence | PostgreSQL JDBC, Spring Data JPA/Hibernate, and Flyway are configured. `spring.jpa.hibernate.ddl-auto=none`; Flyway is enabled. | No JPA entities, repositories, business tables, or migration SQL. The current Flyway history table is infrastructure metadata, not an application model. |
| CORS | Exact local origin `http://localhost:5173`, methods `GET` and `OPTIONS`, request headers `Accept` and `Content-Type`; no credentialed CORS. | Future write methods, auth cookie credentials, and CSRF-aware browser API setup. |
| Authentication | No Spring Security dependency or auth behavior. | Registration, login, logout, customer identity, sessions, authorization, and ownership checks. |
| OpenAPI | Springdoc WebMVC UI dependency; configured Swagger UI path `/swagger-ui.html`. | No business API contracts to document yet. |
| Tests | One backend health-controller JSON test using standalone MockMvc; no frontend tests. | Service, repository, auth, ownership, DB migration, and browser/responsive/accessibility coverage. |

### What has actually been verified

During the Phase 0 database verification, the configured application connected to PostgreSQL 16.9 and confirmed the database name `gadgetgo`. Before normal startup, a read-only inspection found no application tables and no Flyway history table. Normal startup established a Hikari connection; Flyway validated zero migrations, created `public.flyway_schema_history`, and reported no migration necessary; JPA and Spring Boot then started. A browser loaded the React view and displayed `GadgetGo API: UP` from the database-enabled backend. The backend test completed with 1 test, 0 failures, and 0 errors. The Vite production build succeeded.

These checks verify the foundation only. No visual-quality review of a marketplace UI has occurred because that UI does not exist.

## 4. Phase 0 components to preserve

- Existing database `gadgetgo`; never reset or replace it.
- `spring.config.import=optional:file:../.env[.properties]` and the current server-side `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` settings. When launched from `backend/`, this explicitly imports the root `.env`; Spring also accepts process environment variables. Keep all values secret.
- The PostgreSQL driver, Flyway PostgreSQL support, and `spring.jpa.hibernate.ddl-auto=none`. Flyway remains the schema evolution mechanism; schema changes must be additive, reviewed migrations.
- `GET /api/health` and its application-only status contract. It must not imply database health.
- Vite `envDir: '..'` and `VITE_API_BASE_URL`. The Vite default exposes only names with the `VITE_` prefix; never put server credentials under that prefix.
- Existing frontend and backend commands, lockfile, dependencies, root `AGENTS.md`, and secret/build-output ignore rules.
- Local CORS origin while changing it deliberately for Phase 1 API methods/auth, not by using a wildcard.

## 5. Current architecture and dependency assessment

### Frontend

The frontend is a single React entry point (`main.jsx` → `App.jsx`) with one small API utility and a global stylesheet. There are no routing, design-system, form, icon, animation, or state-management packages. This is a suitably small base. React StrictMode is enabled. There is no TypeScript migration.

React 19, Vite 7, Tailwind 4, and their Vite plugins are directly declared and installed. Tailwind CSS and its Vite plugin are complementary, not duplicate dependencies. No state library is justified yet. A router becomes justified as soon as the planned routes are introduced.

### Backend

The backend is a Spring Boot 3.5.6 Java 21 Maven app in `com.gadgetgo`. Existing packages are `api.health` and `config`. The health controller directly returns a small response record; since it has no business logic or persistence, adding service/repository layers to health would be unnecessary. The project is a modular-monolith foundation, not a complete layered business API yet.

Direct dependencies cover the requested foundation: Web, Validation, Data JPA, PostgreSQL JDBC, Flyway PostgreSQL, Springdoc, and Spring Boot Test. Spring Security is intentionally absent because authentication is not implemented. No current direct dependency is obviously redundant for the planned work. Keep adding libraries only when a concrete screen or API needs them.

### Configuration and security observations

- `.env` is read by Spring because of an explicit config import, not because Spring Boot automatically recognizes `.env`.
- Vite reads the same root file as its environment directory, but its default `VITE_` prefix is the boundary that exposes values to browser code. Keep DB settings unprefixed.
- `ddl-auto=none` is correct for Flyway ownership.
- Current CORS is narrowly scoped and suitable for the current GET-only health integration. It will not support `POST`, `PATCH`, `PUT`, or `DELETE` as currently written; authenticated cookie requests also need credentials enabled for the exact origin and a CSRF design.
- No backend security is currently present. That is expected for Phase 0, but backend authorization must land with auth and gadget APIs—not as a frontend-only route guard.
- `.env.example` contains a blank password placeholder and `DB_USERNAME=postgres`. This is not a revealed local credential, but future setup documentation should prefer a dedicated least-privilege app role placeholder rather than encouraging a PostgreSQL superuser. Do not alter existing DB roles or grants without the user's authorization.
- No evidence of unnecessary framework duplication, microservices, or speculative base abstractions.

## 6. Recommended premium visual direction

### Brand personality

**Capable, calm, transparent, and practical.** GadgetGo should reduce the uncertainty of getting a device repaired. Use confident plain language, clear service information, and restrained visual polish. Keep promises tied to implemented facts: do not claim “certified technicians,” warranties, fixed turnaround, or guaranteed quotes unless those capabilities and data are genuinely established later.

### Recommended visual concept

Use an editorial consumer-tech look: warm off-white page surfaces, deep ink/navy for high-confidence hierarchy, and a fresh lime accent for a small number of high-intent actions or status highlights. Pair this with crisp device/service photography, restrained dividers, generous whitespace, and consistent iconography. The accent should make actions recognizable without turning every card, section, and icon bright green. Avoid gradients as a default surface and avoid treating the homepage as an admin dashboard.

The supplied tagline, **“Your Gadget. Fixed. Simple.”**, can appear in a small brand statement or hero support line. It should not imply a booking or repair is completed in Phase 1.

### Proposed design tokens

These are implementation recommendations, not existing styles. Define semantic CSS variables once and connect them to Tailwind CSS 4 with `@theme`/`@theme inline` in the existing CSS-first setup. Do not introduce a Tailwind 3 `tailwind.config.js` convention without a specific reason.

| Token role | Recommendation |
|---|---|
| Brand / primary ink | `#17243A` deep navy for headings, nav, and primary controls |
| Primary hover | `#223653` |
| Accent | `#B7D84B` restrained lime; use dark ink text on lime controls |
| Accent hover | `#A5C63E` |
| Secondary accent | `#2D7C78` muted teal, reserved for links or selected indicators |
| Page background | `#F6F7F3` warm cool-neutral |
| Surface | `#FFFFFF` |
| Soft surface | `#EEF1EC` |
| Main text | `#17243A` |
| Secondary text | `#536174` |
| Muted text | `#687486`, after contrast check for each size/use |
| Border | `#DCE2E3` |
| Success | `#217653` with pale surface `#EAF5EE` |
| Warning | `#8A5700` with pale surface `#FFF4D9` |
| Error | `#B33B42` with pale surface `#FCEBEC` |

Check every text/background pair to WCAG AA before implementation; these values are starting candidates, not a completed contrast audit. Do not use color alone to convey state.

**CSS-first token shape (illustrative, not an edit):**

```css
:root {
  --gg-ink: #17243a;
  --gg-accent: #b7d84b;
  --gg-page: #f6f7f3;
  --gg-surface: #ffffff;
  --gg-border: #dce2e3;
}

@theme inline {
  --color-ink: var(--gg-ink);
  --color-accent: var(--gg-accent);
  --color-page: var(--gg-page);
  --color-surface: var(--gg-surface);
  --color-border: var(--gg-border);
}
```

### Typography and layout tokens

- **Font strategy:** keep one sans-serif family for UI and headings. The current CSS names Inter but does not load a font file, so on systems without Inter it uses a system fallback. Prefer a licensed local WOFF2 asset if the project obtains one; otherwise retain a system stack (`Inter`, system UI, Segoe UI, sans-serif). Avoid a third-party runtime font request by default for privacy and predictable loading.
- **Type scale:** body 16/24 px; small supporting copy 14/20 px; H1 40–56 px desktop and 34–40 px mobile; H2 30–36 px; H3 22–26 px; label 14 px semibold. Use fluid `clamp()` only for major headings and keep long copy around 60–68 characters per line.
- **Spacing:** 4 px base with 4, 8, 12, 16, 24, 32, 48, 64, and 80 px steps. Use section spacing 56–80 px desktop, 40–56 px mobile.
- **Containers:** max-width 1200 px for the core marketplace, up to 1280 px for the homepage hero/catalogue grid; page gutters 20 px mobile, 28 px tablet, 36 px wide desktop; text columns 65ch max.
- **Grid:** 12 columns desktop, 8 tablet, 4 mobile. Service catalogue: 3 columns at wide desktop, 2 at medium, 1 at mobile. Filters use a desktop side rail or compact toolbar; move filters into a clearly labeled drawer/sheet on small screens.
- **Breakpoints:** keep Tailwind 4 defaults unless visual QA shows a real need: `sm` 40rem, `md` 48rem, `lg` 64rem, `xl` 80rem, `2xl` 96rem. Test actual content at 360/390 px, 768 px, 1024 px, and 1280+ px.
- **Radii:** 8 px controls, 12 px small surfaces, 16 px cards, 24 px large hero/media containers, pill only for compact tags/status.
- **Elevation:** mostly border-defined surfaces; one low soft shadow for floating controls/menus and a slightly stronger one on menus/dialogs. Avoid shadows on every card.

### Component states and interaction

- **Buttons:** primary deep navy with white label; high-intent lime variant with deep ink label; secondary outline; text/ghost; destructive outline/filled. All have 44 px minimum height, clear hover/pressed/disabled/loading states, and a 2 px visible focus ring with offset. Do not rely on hover for discoverability.
- **Inputs:** 48 px standard height, persistent visible label, optional hint, clear invalid border plus text message and `aria-describedby`; browser validation is a convenience, backend validation is authoritative. Password visibility is a button with a stable accessible name and no layout jump.
- **Service card:** meaningful image or consistent category art, category label, concise service name, supported device/brand summary only when data supports it, and clearly qualified indicative price only when curated price data exists. Entire card may link to detail; nested buttons should not make the click target ambiguous.
- **Category card:** one icon/image, short category title and device examples. Avoid filler metrics or unsupported item counts.
- **Navigation:** desktop brand + `Services` + `How it works` (only if implemented accurately) + `My gadgets`; signed-out `Log in` and a single primary account action. On mobile, retain a simple header with an accessible menu button and a labeled panel; close on navigation/Escape and keep focus predictable.
- **Toasts:** use only for transient confirmations; errors that require correction remain near the form or page. Announce with `role=status` or `role=alert` appropriate to urgency.
- **Loading skeletons:** match final geometry, apply `aria-busy`, and respect reduced-motion. A small initial page may use a clear status message instead of fabricated card skeletons.
- **Empty states:** explain why there are no results and offer a relevant reset or next action. Gadget empty state should link to “Add a gadget”; catalogue empty state should clear filters.
- **Error states:** explain what failed in plain language, offer retry when safe, preserve user-entered form data, and avoid raw server stack details.
- **Keyboard/focus:** logical DOM order, skip link, visible focus, no keyboard traps except a correctly managed modal/drawer, Escape handling for overlays, and sufficient touch target size.

## 7. Customer page and navigation plan

### Proposed routes

```text
/                         Home / service discovery
/services                 Catalogue and filters
/services/:slug           Service detail
/register                 Customer registration
/login                    Customer login
/account/profile          Customer profile
/account/gadgets           My Gadgets list
/account/gadgets/new       Add gadget
/account/gadgets/:id/edit  Edit gadget
```

Unknown service slugs and gadget IDs need explicit not-found handling. Account routes may redirect signed-out users to login with a validated return path. There are no booking routes in Phase 1.

### Homepage

1. **Responsive header:** logo/name, services route, account navigation, and a single clear action.
2. **Hero:** “Find the right repair service for your device” plus a concise reassurance line; primary CTA opens/focuses service search. Use a carefully licensed/created repair-device image with calm crop and adequate contrast rather than a gradient-only illustration.
3. **Service search:** one obvious text field with an accessible label and submit; suggestions only when backed by catalogue API behavior.
4. **Device categories:** use categories returned by the API; no fake counts.
5. **Selected services:** show curated catalogue results, not “popular” until popularity data is defined.
6. **How discovery works:** short steps limited to actual Phase 1 capabilities such as “Choose your device”, “Compare service details”, “Save your gadget”. Do not imply booking, technician assignment, diagnosis, or repair progress.
7. **Trust/content block:** explain what service descriptions and any indicative prices mean; only use operational claims backed by product data.
8. **Next step and footer:** browse services and access profile/gadgets; include help/contact/legal links only when real destinations exist.

The next customer action should be service search/browse. “Book a repair” is not a Phase 1 action.

### Catalogue and service detail

- Catalogue: search, category filter, and supported-brand/device filters only if the compatibility data model and API provide them. Repair-type filter only if repair type is a real catalogue field. Persist filters in URL query parameters so a refresh/share/back action restores the result state.
- Sort only by available fields: initially category ordering, service name, or relevance if the search implementation actually ranks relevance. Do not offer “popular”, “top rated”, “lowest price”, or “fastest” without underlying data and clear semantics. Expose result count when accurate.
- Paginate server-side from the start of a growing dataset. Reset page to zero when a filter changes. Preserve filter values if the API fails.
- Detail: service name, description, supported categories/brands from stored relationships, common symptoms only if maintained content exists, and carefully qualified indicative range only if an approved source exists. Use copy such as “Indicative only; final repair cost may depend on inspection” if a later product decision supports displaying an estimate. No confirmed quote language.
- Detail CTA can return to catalogue, encourage registration, or offer saving a gadget once that feature exists. Do not route to scheduling or promise a service mode unless the catalogue actually stores it.

### Authentication, profile, and My Gadgets screens

- **Register:** name/email/password/confirmation only after product decisions settle exact required fields. Show inline validation, password requirements before submit, generic server failures, duplicate-account handling, pending state, and success route. Never silently log a user into a different identity.
- **Login/logout:** email/password, password visibility toggle, generic authentication failure, pending state, keyboard-friendly submit, and explicit sign-out. Session validity is backend-owned.
- **Profile:** display and edit only collected profile fields; show save/cancel and field errors. Do not add address/location fields until a Phase 1 need is established.
- **My Gadgets:** list with type/brand/model/nickname; add/edit form with controlled enumerations for device type where supported, free-text model, appropriate optional nickname/year, and field-level errors. Delete requires a confirmation dialog that names the gadget and provides cancel as the safe default. Empty, loading, retry, and success states are needed.
- **Ownership:** every gadget API request is authorized against the authenticated customer in the backend. Client route guards only improve navigation and are never access control.

## 8. Reusable component plan

Start with a small, consistent set, then extract only repeated patterns:

- App router, `PublicLayout`, `AccountLayout`, responsive site header/footer, and page container.
- `Button`, `TextField`, `PasswordField`, `SelectField`, `FormError`, `InlineNotice`, `Badge`, `Spinner`, `Skeleton`, `EmptyState`, `ErrorState`, and modal/dialog shell.
- `ServiceSearch`, `CategoryCard`, `ServiceCard`, `ServiceFilterBar`/mobile filter panel, `SortControl`, `ResultSummary`, `Pagination`, and service detail facts.
- `AuthForm` building blocks, `ProfileForm`, `GadgetForm`, `GadgetList`, `GadgetCard`, and delete confirmation.

Use semantic HTML first; do not create a large design-system package or generic component framework. Use inline SVG for a very small stable icon set at first. If the icon count grows, consider one tree-shakeable icon package after checking its React 19 compatibility and bundle impact. No animation dependency is needed; CSS transitions and `prefers-reduced-motion` are enough. No Redux is justified; use local state, URL search parameters, and a small auth context or query/cache solution only when the real data flows warrant it.

## 9. Proposed customer data model

Do not create these tables in this stage. Use UUID primary keys, timezone-aware timestamps, explicit constraints, and Flyway migrations in the implementation stage.

### `customer`

- `id`, `email` (normalized and unique), `password_hash`, `display_name`, optional profile fields chosen by product, `created_at`, `updated_at`, and an account status/disabled field if operationally required.
- Store no plaintext password, session token, or unnecessary identity data. Do not store serial number/IMEI in Phase 1 My Gadgets absent a clearly justified secure workflow.
- Role modeling: Phase 1 can treat every registered account as a customer. Avoid technician/supplier/admin role tables until authorization requirements for those actors are in scope.

### `service_category`

- `id`, unique stable `slug`, `name`, short description, optional curated icon key, `display_order`, `active`.
- Keep icon keys semantic and render locally; do not persist external asset URLs without an asset policy.

### `repair_service`

- `id`, `category_id` FK, unique stable `slug`, `name`, short and full descriptions, `active`, `display_order`, created/updated timestamps.
- Optional indicative price fields only if product approves the source and interpretation: nullable min/max decimal amounts and ISO currency. Never call them quotations or final prices.
- Symptoms may live in clearly structured service content only if the team can maintain them. Avoid a separate symptom entity until filtering/search needs justify it.

### `service_compatibility` (only if filters need normalized compatibility)

- `service_id` FK, controlled device type, and brand value/key. Composite uniqueness on `(service_id, device_type, brand)`; indexes by device type and brand for supported filters.
- A separate brand catalog table is unnecessary until there are independent brand attributes, aliases, or management requirements. Do not expose filters whose source is not this data.

### `customer_gadget`

- `id`, `customer_id` FK, device type, brand, model, optional customer nickname and optional non-sensitive purchase year, created/updated timestamps.
- Keep the owner relationship mandatory. Default FK delete behavior should be restrictive until account-retention policy is decided. No global uniqueness on customer devices; a person may own two similar gadgets.

### Relationships, indexes, and migration safety

- One category has many services; each service belongs to one category.
- A service has zero or more compatibility rows; one customer owns many gadgets; each gadget has exactly one customer.
- Unique indexes: normalized customer email, category slug, service slug, and compatibility tuple. Query indexes: active/category/order service browse; compatibility device/brand filters; `(customer_id, created_at)` for gadget listing.
- Use `DECIMAL`/`NUMERIC` for money, never float; use `TIMESTAMPTZ` for instants; `VARCHAR` with validation/check constraints for finite types where appropriate; `TEXT` for longer copy. Keep currency explicit if money is introduced.
- First Phase 1 schema changes must account for the existing empty Flyway history table (no prior migration records). Add migrations forward-only, review SQL, and validate against an isolated test database before running on `gadgetgo`. Do not clean/baseline/reset or modify history to mask a conflict. Curated catalogue seed data needs product approval; no fake data.

## 10. Proposed REST API plan

Use `/api` prefix and JSON. Public catalogue operations are anonymous; customer operations require the authenticated session. Use stable slugs for public detail URLs while resolving to database IDs internally.

| Method and endpoint | Purpose / outline | Auth | Validation and authorization |
|---|---|---|---|
| `GET /api/service-categories` | Active categories: `{items:[{slug,name,description,iconKey}]}` | Public | Return only active, ordered categories. |
| `GET /api/services?q=&category=&deviceType=&brand=&repairType=&sort=&page=&size=` | Filtered catalogue: `{items:[ServiceSummary], page, size, totalItems, totalPages}` | Public | Validate enum/sort/page bounds; ignore or reject unsupported filters consistently; apply only filters backed by stored fields. |
| `GET /api/services/{slug}` | Detail: name, descriptions, category, supported device/brand data, optional explicitly indicative pricing | Public | Active service only; 404 for unknown/inactive slug. Do not expose entity internals. |
| `POST /api/auth/register` | `{email,password,displayName}` → customer summary; creates account/session only per agreed post-registration behavior | Anonymous | Normalize/validate email and password; hash before persistence; do not return hash; define duplicate email behavior. |
| `POST /api/auth/login` | `{email,password}` → current customer summary; session cookie set by Security | Anonymous | Generic invalid-credential response, rate/abuse controls considered, no credential logging. CSRF policy decided for login. |
| `POST /api/auth/logout` | Invalidates session and clears cookie | Authenticated | Require CSRF token; idempotent behavior documented. |
| `GET /api/auth/csrf` | Issue/return CSRF token for browser requests if using cookie-to-header flow | Anonymous/session | Token is not an auth token; CORS is exact-origin. |
| `GET /api/customers/me` | Current customer profile | Authenticated | Identity derives from server principal, never caller-supplied customer ID. |
| `PATCH /api/customers/me` | Update allowed profile fields | Authenticated | DTO allowlist, Bean Validation, ownership from principal. |
| `GET /api/customers/me/gadgets` | Customer's paged gadget list | Authenticated | Query scoped to authenticated customer. |
| `POST /api/customers/me/gadgets` | Create gadget, returns created summary/detail | Authenticated | Validate type, brand/model length, optional values; bind owner from principal. |
| `GET /api/customers/me/gadgets/{id}` | Read one owned gadget | Authenticated | Scope repository query by both gadget ID and current customer. Return 404 for non-owned IDs to avoid revealing existence. |
| `PUT /api/customers/me/gadgets/{id}` | Replace editable gadget fields | Authenticated | Same owner scope; do not allow caller to change owner or ID. |
| `DELETE /api/customers/me/gadgets/{id}` | Delete a gadget | Authenticated | Same owner scope; confirmation is client UX, server authorization remains required. |

### API conventions

- Use zero-based `page` and bounded `size` (for example 12 default, capped at 48); stable ordering must include a tie-breaker ID. Return a compact custom page envelope rather than leaking Spring `Page` internals.
- Use URL query parameters for sharable catalogue state. Search input maps to `q`; category/brand values use stable slugs/keys; `sort` is an allowlist of actual supported sorts.
- Use Spring `ProblemDetail` / `application/problem+json` (RFC 9457-compatible) for consistent errors. Include stable `type`, `title`, HTTP `status`, safe `detail`, and optional field-error extensions. Never return stack traces, SQL, or secrets.
- Expected status codes: 200 reads/updates, 201 create, 204 successful delete/logout where suitable, 400 validation/query error, 401 unauthenticated, 403 forbidden when disclosure is acceptable, 404 unknown/non-owned resource, 409 uniqueness conflict. Keep login failures generic.
- Request/response DTOs are the API contract; do not serialize JPA entities.

## 11. Authentication and security recommendation

### Options considered

| Approach | Benefits | Costs/risks |
|---|---|---|
| Server-side session cookie | Straightforward revocation/logout; HttpOnly session ID inaccessible to JS; well matched to a first-party React web client and one Spring monolith. | Requires CSRF protection; browser requests need credentials; in-memory sessions end on restart and require shared storage if scaled. |
| JWT bearer token held by React | Useful for independently deployed clients and stateless API verification. | Browser storage is exposed to XSS if persistent in JS-accessible storage; refresh, revocation, rotation, CSRF if cookie-held, and logout semantics add complexity not needed for this Phase 1 web scope. |

**Recommendation:** use Spring Security with a server-side session cookie for the first-party web application. Keep the session ID in an `HttpOnly` cookie, `Secure` in production, `SameSite=Lax`, Path `/`, no broad Domain. For local HTTP only, Secure may be disabled by a local profile, never production. Store sessions in-process for the first local milestone; document restart invalidation and revisit JDBC-backed session persistence before multi-instance deployment.

Enable CSRF protection for state-changing requests. A standard cookie-to-header CSRF token pattern can let React read a dedicated CSRF token cookie and return its value in a header; the session cookie remains HttpOnly. Never disable CSRF globally to make fetch work. Send `credentials: 'include'` from the frontend; configure exact allowed origin(s), allowed methods/headers, and credential support in Spring. Avoid wildcard origins with credentials. Align development hostnames (`localhost` versus `127.0.0.1`) and production site topology deliberately.

Use Spring Security's password encoder with a suitable adaptive hash (BCrypt via the existing Security crypto stack is a pragmatic initial choice; benchmark cost on deployment hardware and plan rehash/upgrade). Never log passwords or session IDs. Login errors should not reveal whether an email exists. Logout is a CSRF-protected POST that invalidates the server session and clears the cookie. Consider login/register abuse controls before public launch.

Authorize every protected endpoint on the backend. Resolve current customer from the Security principal; scope every gadget repository query by both gadget ID and owner ID. A React route guard only improves UX. Return 404 for a non-owned gadget where appropriate. Add Spring Security when implementing auth; do not add it during this audit.

## 12. Frontend architecture recommendation

Retain JavaScript and the existing Vite/Tailwind setup. Introduce a router only as the routes are implemented. Organize by feature with a small shared layer, for example:

```text
frontend/src/
  app/                 router, layouts, auth/session provider
  shared/ui/            buttons, fields, notices, dialogs, loading/empty states
  shared/lib/            API client, error parsing, small utilities
  features/catalog/     catalogue pages, service components, API functions
  features/auth/        register/login forms and auth API
  features/profile/     profile view/edit
  features/gadgets/     gadget list/forms/API
  styles/               tokens and global styles
```

Keep route pages thin, feature components near the feature, shared components truly shared. Split the current `App.jsx` health view into an explicit temporary/health route or replace it as part of approved implementation; preserve the API health endpoint regardless. Avoid a global server cache or Redux until actual cross-route state demands it. URL parameters should own catalogue filters; auth state should be small and sourced from `/api/customers/me`/auth status, not a second client-side identity store.

Use a single `apiClient` to apply base URL, JSON headers, cookie credentials, CSRF header, and safe `ProblemDetail` parsing. It must not log secrets, swallow cancellation, or conflate network and validation failures. Keep form validation declarative and accessible; native constraints plus a small reusable helper are sufficient initially. Add a form library only if forms become materially more complex. Keep inline SVG initially; add no icon/animation library until there is a concrete repeated need. No TypeScript conversion is proposed. No shadcn/ui adoption is recommended for this small CSS-first foundation; if reconsidered later, evaluate its current Tailwind 4 and React 19 setup against the exact Vite config before adopting any generated components.

## 13. Backend architecture recommendation

Use a modular monolith organized by business feature, with the requested controller → service → repository → database flow inside features:

```text
com.gadgetgo
  config/                 CORS, Security, OpenAPI, persistence configuration
  common/api/              ProblemDetail/error response handling
  catalog/                 controller, DTO, service, repository, entity/domain
  auth/                    controller, request/response DTO, service, account repository
  customer/                controller, DTO, service, repository, profile rules
  gadget/                  controller, DTO, service, repository, ownership rules
  api/health/              preserve existing health endpoint
```

Keep controllers focused on HTTP mapping and status codes. Validate request DTOs with Bean Validation, call service-layer use cases, and return response DTOs. Services enforce ownership/business rules and define transaction boundaries. Repositories use explicit queries for filtering and owner-scoped gadget operations. Entities model persistence, not public JSON. Map DTOs explicitly (manual mapping is adequate while the model is small); avoid generic base repositories and premature mapping frameworks.

Use `@Transactional` around account, profile, and gadget writes where consistency requires it; reads can be read-only transactions if useful. Centralize safe validation and domain exceptions into ProblemDetail responses. Keep Flyway SQL as the only schema evolution path; Hibernate validates/uses mappings without generating DDL (`ddl-auto=none` stays). Springdoc annotations document actual contracts, not speculative endpoints. The existing health controller can remain direct because it has no business rule.

## 14. Accessibility and responsive plan

- Semantic landmarks (`header`, `nav`, `main`, `footer`), one meaningful H1 per page, ordered headings, skip link, explicit labels, and descriptive link names.
- WCAG 2.2 AA target: keyboard operation, visible focus, contrast checks, 200% zoom/reflow, touch targets around 44×44 CSS px, no information conveyed by color alone, reduced-motion support, accessible status/error announcement.
- Search/filter controls must have labels, state conveyed to assistive technology, clear/reset affordances, and preserve focus when results refresh. Dialog/drawer focus must be trapped only while open and restored on close.
- Inputs associate hints/errors, use correct autocomplete (`email`, `current-password`, `new-password`), and do not rely on placeholders as labels.
- Responsive QA at 360/390, 768, 1024, and 1280+ widths. Check navigation, hero crop, form widths, filter drawer, cards, long service names, zoom, and no horizontal scroll. Do not merely shrink a desktop sidebar; design a usable mobile filter interaction.
- Test with keyboard only and at least one screen reader/browser combination before declaring accessibility complete. A build does not verify visual, responsive, or assistive quality.

## 15. Testing and acceptance criteria

### Frontend

- Production `npm run build` passes with the existing lockfile and no console/build errors.
- Each route loads directly and through navigation; browser back/forward works. Unknown routes show useful not-found state.
- Catalogue filter/search/sort updates URL and results consistently; page resets on filter change; back/refresh restores state.
- Every API view has loading, success, empty, network/server error, and retry behavior where retry is safe. Form submissions preserve input on validation/network failure.
- Registration/login validation and password visibility work by keyboard; error summaries/field errors are announced and associated.
- Profile/gadget forms provide pending/success/failure states; gadget delete requires a cancellable confirmation.
- Responsive visual inspection on mobile/tablet/desktop; check console/network and accessibility tree, not build alone.

### Backend

- Bean Validation tests cover required, malformed, boundary, and invalid enum fields.
- Catalogue service/controller tests cover category listing, query filters, allowed sort options, pagination bounds, inactive/unknown detail, and stable page ordering.
- Auth tests cover password hashing (never equals raw password), success/failure behavior, logout/session invalidation, CSRF behavior, and safe error response.
- Profile/gadget tests verify authentication, CRUD persistence, and customer ownership for read/update/delete, including attempts against another user's ID.
- Error mapping tests verify ProblemDetail status/shape and no internal stack/SQL leakage.
- Persistence/Flyway integration tests run against an isolated disposable PostgreSQL test database (for example Testcontainers if available) or a separately provisioned explicit test DB. Never point an automated test that migrates/resets schema at the user's existing `gadgetgo` database.
- Flyway tests validate clean migration order and upgrade path from the existing empty history state. Do not use `clean` or delete history in the shared DB.

### End-to-end and quality

- Start the database-enabled backend with the documented `.env` loading path; confirm Hikari and Flyway logs, then verify health endpoint JSON.
- Start React and verify browser network response for the actual API origin, CORS, cookie credentials, CSRF flow, and visible UI state.
- Verify service list/detail read real PostgreSQL-backed records; verify profile/gadget writes survive backend restart, using test records in an isolated test database.
- Check Git status and secret scanning before commits; no `.env`, passwords, generated `target`, `node_modules`, or `dist` files are tracked.
- Do visual inspection at the target breakpoints; record screenshots/issues and fix them. Passing tests/build alone does not establish premium visual quality.

## 16. Ordered implementation checkpoints

Each checkpoint should be reviewed and verified before moving on. Exact file names will follow the code structure already in the repository.

### Checkpoint 1 — Design tokens, routing shell, and premium homepage frame

- **Likely areas:** `frontend/src/styles.css` (or `styles/tokens.css`), `src/app/`, shared header/footer/button/field, homepage route and sections, Vite router dependency only if selected.
- **Deliverable:** approved tokens, responsive shell, branded but restrained homepage with honest content and service search entry point. No booking CTA.
- **Depends on:** this report's design approval and confirmed brand/content asset choices.
- **Tests:** frontend build, route smoke tests, component accessibility checks, keyboard and breakpoint review.
- **Acceptance:** visual hierarchy and next action are clear at 360/390/768/1280; contrast/focus pass; no fake trust claims or catalogue counts.

### Checkpoint 2 — Catalogue domain contract and additive Flyway schema

- **Likely areas:** API DTO contract, `catalog` domain/repository/service/controller, `src/main/resources/db/migration/V1__...sql`, isolated DB integration tests.
- **Deliverable:** category/service schema and only justified compatibility/pricing structures; approved, factual seed content if content is ready; public read APIs with pagination/filter allowlists.
- **Depends on:** approved category vocabulary, search/filter behavior, and decision on indicative price and supported compatibility data.
- **Tests:** repository/service/API tests against isolated PostgreSQL, migration validate/apply from empty history, ordering/pagination/filters.
- **Acceptance:** query responses reflect actual stored fields; no unsupported sort/filter or fake records; no destructive SQL; migration is reviewed and does not touch unrelated objects.

### Checkpoint 3 — Catalogue and service detail UI integration

- **Likely areas:** `features/catalog`, API client, routes for `/services` and `/services/:slug`, shared cards/filter/loading/error/empty components.
- **Deliverable:** live searchable catalogue and detail view integrated with real backend data and URL state.
- **Depends on:** Checkpoints 1–2 and stable API response contract.
- **Tests:** API loading/error/empty/filter/pagination tests; direct route navigation; mobile filters; keyboard; responsive screenshot review.
- **Acceptance:** filters match stored data and actual query params; refresh/back preserves state; detail never presents estimate as a confirmed quote.

### Checkpoint 4 — Customer identity, session security, and profile API/UI

- **Likely areas:** add Spring Security dependency/config, customer/auth model and migration, auth/profile DTO/service/repository/controller, exact-origin CORS/CSRF, frontend API session handling, auth/profile routes.
- **Deliverable:** registration, login, logout, current profile, edit profile, secure session-backed navigation.
- **Depends on:** chosen session/CSRF/CORS design, account fields, approved password rules and post-registration behavior.
- **Tests:** password hash, auth failures, session lifecycle, CSRF, CORS allowlist, validation, profile access controls, frontend form/accessibility states.
- **Acceptance:** no secret/token in browser storage or logs; backend gates protected routes; logout invalidates server session; CSRF protects writes; cookie flags are correct by environment.

### Checkpoint 5 — Customer gadget persistence and ownership

- **Likely areas:** additive Flyway gadget migration, gadget entity/DTO/repository/service/controller, customer-scoped frontend gadget routes/forms/list.
- **Deliverable:** authenticated customer gadget list/add/edit/delete.
- **Depends on:** Checkpoint 4 principal/session and final gadget fields.
- **Tests:** CRUD persistence, validation, ownership of every operation, non-owned IDs, delete flow, empty/error states, isolated PostgreSQL persistence.
- **Acceptance:** owner comes from authenticated server principal; cross-customer access is denied/hidden; no sensitive serial/IMEI field without a new need; delete is confirmable and cancellable.

### Checkpoint 6 — Integration, polish, and release readiness

- **Likely areas:** frontend/backend integration, CORS/security config, accessibility fixes, API errors, docs, test/CI commands.
- **Deliverable:** coherent customer flow from homepage to catalogue/detail, account/profile, and gadgets; stable restart behavior and documentation.
- **Depends on:** preceding checkpoints.
- **Tests:** full build/test suite, migration validation against isolated DB, browser end-to-end, restart persistence, mobile/keyboard/screen-reader review, dependency and secret checks.
- **Acceptance:** no Phase 1 excluded workflows; no regression to health endpoint; migration is additive and validated; all major states and breakpoints reviewed; known issues documented.

## 17. Risks, dependencies, and unresolved decisions

1. **Catalog content and compatibility source:** which device categories, brands, service names, and descriptions are approved? Do not seed fake or speculative coverage.
2. **Indicative prices:** are any service estimates supported by an authoritative source, currency, and display policy? If not, omit pricing for Phase 1.
3. **Search semantics:** define supported filters, sort options, page size, and result counts from the eventual schema; do not promise popularity/rating/availability.
4. **Brand assets and photography:** obtain licensed or created assets and decide whether local asset files or an asset host is used. No competitor logo/artwork.
5. **Typography asset:** current `Inter` CSS is not a loaded font; decide whether to self-host licensed font files or retain system fallback.
6. **Account requirements:** required registration fields, password policy, email verification, account recovery, duplicate email behavior, and retention/deletion policy remain unspecified.
7. **Deployment topology:** session storage, cookie Secure/SameSite policy, allowed origins, HTTPS termination, and whether a future mobile client is expected affect production security choices.
8. **Flyway migration starting point:** existing `flyway_schema_history` is present and empty, and the current `public` schema initially had no application objects. Review actual live state again before applying Phase 1 migrations; never rewrite history to accommodate a conflict.
9. **Testing isolation:** decide whether Docker/Testcontainers is available or provision a separate test DB. Never run migration tests that mutate/reset the user's `gadgetgo` database.
10. **Git workflow:** repository has no commits and all project files are currently untracked. Establish a baseline commit only through the user's chosen workflow; do not discard current files.

## 18. Explicit scope confirmation

This report is documentation-only. No Phase 1 customer registration/login, service catalogue, search/filter implementation, detail pages, profile, gadget CRUD, authentication, business API, business entity, or database migration was implemented. Existing application code, dependency manifests/lockfiles, database configuration, and database objects were not modified during this audit. Preserve `GET /api/health` and the Phase 0 setup.

## 19. Technical references checked

These official references support the planning recommendations and should be rechecked against the exact Spring Security version selected when authentication is implemented:

- [Tailwind CSS theme variables](https://tailwindcss.com/docs/theme) — CSS-first `@theme` tokens generate utilities and CSS variables in Tailwind 4.
- [Spring Security CSRF protection](https://docs.spring.io/spring-security/reference/6.5/servlet/exploits/csrf.html) — SPA-specific CSRF behavior and token handling.
- [Spring Security session management](https://docs.spring.io/spring-security/reference/6.5/servlet/authentication/session-management.html) — session lifecycle and fixation protection.
- [Spring MVC CORS configuration](https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html) — credentialed CORS requires explicit trusted origins and careful handling.
