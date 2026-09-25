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
- React/TypeScript browser client with authenticated student, reviewer, and administration
  workflows under `frontend/`. Frontend installation, build, and browser workflows are not yet
  validated in this environment.
- Ownership and assignment authorization across work entries, progress, dashboards,
  submissions, verification, evidence, attendance, notifications, private objects, reports,
  escalations, and activity management.
- Explicit object-oriented Java classes instead of records, with enums retained where useful.

## Planned and still open

- Remove the remaining development-era `userId`/`actorId` request/query fields and rely only on
  the authenticated principal.
- Finish replacing compatibility behavior for legacy role-only test contexts; production bearer
  requests already use authenticated principal and centralized authorization checks.
- Frontend unit/build checks and live browser acceptance across student and reviewer workflows.
- AWS runtime validation. Docker runs locally, but ECS, RDS, Aurora, S3, Lambda, and EventBridge
  have not been deployed or verified in AWS.
- Evidence and selfie retention/deletion policy.
- Management UI/views and broader authenticated-principal migration for legacy identity fields.

See [Security](Security.md), [Testing](Testing.md), and the root README release checklist for
known limits and the current validation record.

## Issue-board alignment

GitHub issues #15 and #16 are closed and match the implemented activity/evidence/storage work.
Issue #17's evidence-versioning acceptance criteria are implemented and tested, pending board
closure. The Milestone 2 parent issue still contains an older
unchecked child checklist for #15 and #16; that checklist should be corrected in GitHub. The
available repository integration is read-only, so this repository cannot claim that board edit
has been performed.
