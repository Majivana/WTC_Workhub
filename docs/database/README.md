# Database Documentation

The relational model supports institutions, campuses, configurable work periods, work entries,
first-party attendance sessions, evidence versions, submissions, verification history, and audit
events.

The initial entity relationship specification is available in
`../diagrams/erd.puml`. The current persistence implementation is SQLite-specific. No PostgreSQL
driver, profile, migration set, or supported runtime configuration exists. PostgreSQL schema
compatibility was checked against PostgreSQL 16 on 2026-09-25 and failed at the first SQLite
`PRAGMA` statement in `schema.sql`; this project is not currently portable to RDS PostgreSQL or
Aurora PostgreSQL. See [ADR-015](../adr/ADR-015-sqlite-local.md),
[ADR-016](../adr/ADR-016-rds-postgresql.md), and
[ADR-017](../adr/ADR-017-aurora-postgresql.md).

## Environment status

| Environment | Database | Status |
|---|---|---|
| Local development and integration tests | SQLite JDBC | Implemented and verified by the integration tests listed below |
| AWS staging | RDS for PostgreSQL | Planned only; no AWS instance, PostgreSQL driver/profile, or tested application connection |
| AWS production | Aurora PostgreSQL | Planned only; no cluster, PostgreSQL driver/profile, or tested application connection |

Do not configure a PostgreSQL JDBC URL and assume the application can run against it. RDS and
Aurora require a deliberate schema/query/migration conversion and application-level compatibility
tests before they can be considered supported.

Verify the existing SQLite setup from the repository root with:

```bash
mvn -DargLine=-javaagent:/path/to/byte-buddy-agent.jar \
  -Dtest=DatabaseIntegrationProfileTests,CorePersistenceIntegrationTests test
```

Use the Byte Buddy agent version resolved for the project. In the 2026-09-25 workspace it was
`/home/kwanda/.m2/repository/net/bytebuddy/byte-buddy-agent/1.15.11/byte-buddy-agent-1.15.11.jar`.
The project database itself remains SQLite in both test classes.

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
