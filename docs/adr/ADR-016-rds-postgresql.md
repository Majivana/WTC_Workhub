# ADR-016: RDS PostgreSQL for staging

- **Status:** Proposed; blocked on PostgreSQL implementation and validation
- **Date:** 2026-09-25
- **Decision owners:** Project owner
- **Related issues:** None recorded

## Context

Staging needs a persistent database representative of a potential AWS deployment. RDS for
PostgreSQL is a candidate, but the current application has only a SQLite JDBC dependency and
SQLite-specific schema initialization, migration, and repository behavior.

## Decision

Consider RDS for PostgreSQL as the staging target only after a PostgreSQL driver/profile,
PostgreSQL-compatible migrations and queries, and an application-level compatibility suite are
implemented. No RDS instance or working PostgreSQL runtime configuration currently exists.
The project was tested against a disposable local PostgreSQL 16 container on 2026-09-25; applying
`schema.sql` fails at its opening SQLite `PRAGMA foreign_keys = ON` statement. This is a failed
compatibility check, not evidence of a supported PostgreSQL deployment.

Future staging configuration should use private database subnets, TLS-verified connections,
storage and backup encryption, automated backups with an owner-approved retention period, a
Secrets Manager secret, and security-group ingress only from the application task role's network
boundary. These are requirements for a future deployment, not configured resources.

## Alternatives considered

### Continue with SQLite in staging

Rejected for an AWS multi-instance service because it does not provide the managed shared database
target intended for ECS and Lambda.

### Aurora PostgreSQL in staging

Deferred. The application compatibility gap applies to Aurora PostgreSQL as well, and Aurora's
cluster features and pricing add complexity without a demonstrated staging need.

## Consequences

### Positive consequences

- A PostgreSQL staging environment can exercise the same engine family selected for a future
  production database.
- Managed backups, monitoring, encryption, and private VPC placement can be configured centrally.

### Negative consequences and trade-offs

- Application database portability work and compatibility testing are prerequisites; current
  SQLite success does not establish PostgreSQL support.
- Database instance hours, storage, optional provisioned IOPS, backups beyond included allowances,
  and cross-AZ data movement can add recurring charges. Obtain a region- and configuration-specific
  estimate from the AWS Pricing Calculator before provisioning.

## Security and privacy impact

Keep the database private, require TLS, encrypt storage and snapshots, restrict secrets to the
runtime role, and limit ingress to the application security group. Database backups and logs must
follow approved retention and access controls; do not place selfies, exact location histories, or
credentials in diagnostic logs.

## Operational impact

No staging database is deployed or tested. Budget for instance uptime even when lightly used,
storage growth, backup retention, monitoring, and any networking charges. A staging schedule or
smaller instance may reduce compute cost, but must preserve required test availability and data.

## Validation

The PostgreSQL 16 DDL smoke test failed at the first `PRAGMA` in `src/main/resources/schema.sql`.
Do not mark RDS validated until migrations, repository queries, constraints, transaction behavior,
health checks, and backup/restore have passed against a disposable RDS PostgreSQL database.
