# Database Documentation

The relational model supports institutions, campuses, configurable work periods, work entries,
first-party attendance sessions, evidence versions, submissions, verification history, and audit
events.

The initial entity relationship specification is available in
`../diagrams/erd.puml`. The model avoids SQLite-specific business logic so it can be validated
against PostgreSQL for RDS and Aurora.

## Persistence rules

`src/main/resources/schema.sql` is idempotent: every table and index uses `IF NOT EXISTS`, and
Spring SQL initialization runs it on every startup. Foreign keys are enabled for SQLite.

`campus_geofence` stores active latitude/longitude/radius configurations, so geofences are data
rather than hard-coded application constants. A partial unique index prevents more than one
`ACTIVE` attendance session per user. Work entries, attendance captures, evidence versions,
verification history, and audit events have indexes for their principal lookup paths.

`private_object_reference` is deliberately separate from relational metadata. It stores only a
private object key and metadata such as media type, size, and checksum. `attendance_capture` and
`evidence_version` reference it by foreign key; neither table stores image/file bytes, public URLs,
or sensitive content. Logs and audit records contain entity references and action details only.

Important constraints include positive geofence radius and durations, valid work-period date
ordering, unique institution/work-role/activity names or codes, unique evidence versions per
evidence item, and foreign keys across all core relationships.
