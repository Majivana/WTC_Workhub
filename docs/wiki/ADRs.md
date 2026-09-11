# Architecture Decision Records

The ADR index and template are in `docs/adr/`.

Planned decisions cover modular monolith architecture, SQLite/RDS/Aurora, S3, first-party
attendance, Lambda, RBAC, WorkPeriods, evidence versioning, immutable verification, audit
logging, and privacy/retention.

Implemented decisions are recorded in:

- `docs/adr/ADR-014-explicit-oop-classes.md` — use explicit Java classes instead of records while
  preserving JSON REST and the modular-monolith boundaries.
