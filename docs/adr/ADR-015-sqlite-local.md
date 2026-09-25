# ADR-015: SQLite for local development

- **Status:** Accepted for local development and integration tests
- **Date:** 2026-09-25
- **Decision owners:** Project owner
- **Related issues:** None recorded

## Context

The application needs a low-cost database for development and automated integration tests. The
current implementation uses SQLite-specific schema statements and migration/query behavior.

## Decision

Keep SQLite as the only supported database for local development and automated integration tests.
The default `local` profile uses `./workhub-local.db`; the `integration` profile uses an isolated
file under `target/`. Treat this as a development choice, not a production high-availability
strategy.

## Alternatives considered

### PostgreSQL for local development

Not selected now because the project has no PostgreSQL driver/profile and its schema and
migrations have not been ported. The PostgreSQL 16 schema smoke test failed immediately on a
SQLite `PRAGMA` statement.

### In-memory database

Not selected because file-backed SQLite exercises the same relational engine and persistence
behavior used by the local application.

## Consequences

### Positive consequences

- Developers can run the application without a database server or AWS account.
- The local profile and SQLite integration tests are currently verified.

### Negative consequences and trade-offs

- SQLite-specific SQL and migration behavior make PostgreSQL compatibility unproven and currently
  false for the schema initialization path.
- SQLite is not being proposed as a multi-instance AWS production database.

## Security and privacy impact

The local database file may contain sensitive attendance and evidence metadata. Keep it out of
version control and protect local backups and workstation access. Production encryption, access,
and retention controls must be supplied by the selected managed database and deployment.

## Operational impact

Local startup has no database service cost. Operators remain responsible for local file lifecycle
and backups. This decision does not authorize use of SQLite in ECS or Lambda deployments.

## Validation

On 2026-09-25, `DatabaseIntegrationProfileTests` and `CorePersistenceIntegrationTests` passed
against SQLite using Maven Surefire with the locally installed Byte Buddy agent. PostgreSQL 16
compatibility was separately checked and rejected by the current schema's first `PRAGMA`.
