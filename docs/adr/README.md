# Architecture Decision Records

Create one ADR per significant architectural decision. Copy `ADR-template.md`, give the copy a
number and descriptive name, and complete every section.

ADRs document accepted MVP decisions and proposed infrastructure decisions. They are not
deployment evidence. The main application/security decisions are:

- [ADR-018: Modular monolith and JSON API](ADR-018-modular-monolith.md)
- [ADR-019: Authentication and centralized authorization](ADR-019-auth-rbac.md)
- [ADR-020: First-party attendance evidence](ADR-020-attendance.md)
- [ADR-021: Versioned evidence and verification history](ADR-021-evidence-verification.md)
- [ADR-022: Weekly progress reminder](ADR-022-weekly-reminder.md)
- [ADR-023: Configurable work periods and progress](ADR-023-work-periods.md)
- [ADR-024: Private object storage boundary](ADR-024-private-storage.md)
- [ADR-025: Append-oriented audit and retention](ADR-025-audit-retention.md)

Database decisions and current validation status are recorded separately:

- [ADR-015: SQLite for local development](ADR-015-sqlite-local.md)
- [ADR-016: RDS PostgreSQL for staging](ADR-016-rds-postgresql.md)
- [ADR-017: Aurora PostgreSQL for production](ADR-017-aurora-postgresql.md)
