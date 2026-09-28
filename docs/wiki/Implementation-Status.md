# Implementation Status

This page is the source of truth for the current code slice. The original project goal remains
unchanged: connect attendance, work entries, activities, evidence, verification, reporting, and
AWS operations into one explainable platform.

## Implemented and tested

- Spring Boot modular-monolith/client-server structure.
- JSON REST endpoints for work entries, progress, activities, evidence metadata, and private
  object download URLs.
- Configurable activity types with duplicate-name validation and safe deactivation.
- Work-entry references to active activities and time validation.
- Relational evidence metadata and append-only evidence versions.
- Evidence version change notes and upload audit events.
- Submission state machine, API, and notification hook.
- Immutable verification decisions linked to reviewed evidence versions and audit events.
- First-party attendance clock-in/out, geofence checks, private selfie references, corrections,
  and reconciliation summary.
- Student dashboard, supervisor queue, in-app notification persistence, escalations, and
  filtered CSV work summary export.
- Evidence/selfie storage policies for media type, size, checksum, and private namespaces.
- Local storage adapter for safe development without real personal images.
- S3 storage adapter and presigned URL boundary; AWS deployment is not yet verified.
- BCrypt password storage, opaque bearer-token login, and anonymous-route protection.
- Admin management APIs for users, institutions, campuses, and validated assignments;
  user deactivation preserves historical foreign-key records.
- Centralized RBAC permissions and role-to-permission mapping for student, supervisor, mentor,
  admin, and super-admin system roles.
- React/TypeScript browser client under `frontend/` with student attendance/work/evidence/submission
  flows, reviewer queue and verification, notifications, escalations, administration, and CSV
  reporting. API/client tests pass and the production build passes; manual browser acceptance and
  end-to-end tests remain open.
- Frontend route chunks are lazy-loaded, protected navigation follows effective API permissions,
  and the dashboard adapter translates JDBC snake_case rows into the browser model.
- Ownership and assignment authorization across work entries, progress, dashboards,
  submissions, verification, evidence, attendance, notifications, private objects, reports,
  escalations, and activity management.
- Explicit object-oriented Java classes instead of records, with enums retained where useful.

## Planned and still open

- Remove the remaining development-era `userId`/`actorId` request/query fields and rely only on
  the authenticated principal.
- Finish replacing compatibility behavior for legacy role-only test contexts; production bearer
  requests already use authenticated principal and centralized authorization checks.
- Manual browser acceptance and end-to-end coverage across student and reviewer workflows,
  including camera/location denial and 401/403/409 interaction states.
- AWS runtime validation. Docker runs locally, but ECS, RDS, Aurora, S3, Lambda, and EventBridge
  have not been deployed or verified in AWS.
- Evidence and selfie retention/deletion policy.
- Management UI/views and broader authenticated-principal migration for legacy identity fields.

See [Security](Security.md), [Testing](Testing.md), and the root README release checklist for
known limits and the current validation record.

## Issue-board alignment

See [project board audit](../project-board-audit.md) for the last recorded issue state, frontend
acceptance mapping, and board follow-ups. The live GitHub board was not accessible during the
2026-09-28 review, so its current status is not asserted.
