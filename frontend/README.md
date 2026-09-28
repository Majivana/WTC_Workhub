# WTC Workhub browser client

React and TypeScript client for the existing Spring Boot API. It uses the backend's bearer-token
login and permission model; the server remains authoritative for every protected operation.

## Local setup

Requirements: Node.js 22.12+ and npm 10+; Java 21 and Maven 3.9+ for the API.

Terminal 1, from the repository root:

```bash
mvn spring-boot:run
```

Terminal 2:

```bash
cd frontend
npm ci
npm run dev
```

Open <http://localhost:5173>. Vite forwards `/api` and `/actuator` to `http://localhost:8080`.
For a different local API host, set `WORKHUB_API_TARGET` before `npm run dev`, for example:

```bash
WORKHUB_API_TARGET=http://localhost:8081 npm run dev
```

`VITE_API_BASE_URL` is optional and defaults to the same-origin `/api` path. Vite variables are
public browser configuration; never place passwords, tokens, or cloud credentials in a `VITE_*`
variable. Login tokens are kept in tab-scoped `sessionStorage` and are not written to logs. Logout
clears this browser tab's token; the backend currently has no logout/revocation endpoint, so the
server-side bearer session remains usable until it expires or the application restarts.

For an explicitly seeded local demo, start the backend with `WTC_SEED_DATA=true`. The demo accounts
are `student.demo` / `demo-student-password` and `supervisor.demo` / `demo-supervisor-password`;
these fixed credentials are for isolated local demonstration only. Do not enable demo seed data in
a shared or production environment.

## Workflows

- Students: period progress and notifications, attendance with camera and geolocation, work entry
  creation/editing, evidence upload/download, and submission/resubmission.
- Supervisors and mentors: assigned review queue, private evidence retrieval, review history,
  decisions, and escalations.
- Administrators: account creation/deactivation, institution and campus lists, activity types, and
  CSV work summaries.

Routes and navigation adapt to the authenticated user's role and permissions. The API enforces
access again on every request. A `401` clears the local session; `403`, validation, conflict, missing
record, server, and network errors are shown as distinct feedback states.

## Development and checks

```bash
npm ci
npm run dev
npm test
npm run build
npm run preview
```

Vite uses React, TypeScript, Tailwind CSS 4, React Router, TanStack Query, React Hook Form and Zod.
The API client is centralized in `src/api/client.ts`; API DTOs live in `src/types/domain.ts` and
feature screens are under `src/features/`. Feature routes are lazy-loaded. The dashboard adapter
converts backend JDBC snake_case summary rows to the camelCase view model.

## Current limitations

- `package-lock.json` is present in the working tree. Keep it committed alongside `package.json`
  and use `npm ci` for reproducible installs.
- The browser frontend runs separately from the Spring Boot application. The current Docker image
  packages only the backend; a production deployment needs a same-origin reverse proxy or a static
  frontend host configured to forward `/api` to the backend over HTTPS.
- Work entry descriptions are absent because the current API model does not accept them.
- User account work-role assignment is not available in this screen because the API does not
  expose the work-role catalogue. Institution, campus, supervisor and mentor assignments are
  supported.
- Attendance selfies and evidence files are sent to the backend and stored in its configured
  private provider. Production use requires private encrypted storage, TLS, operational retention
  controls and access policies described in the root security documentation.
- Automated tests currently cover the API client's token/error handling and dashboard row
  normalization (3 tests). There is no end-to-end browser suite yet, and screens have not been
  manually accepted against a running backend in this review.
- Camera capture, geolocation, and attendance requests use the backend API contract, but the
  complete user workflow has not been exercised in a real browser with camera/location permissions.
