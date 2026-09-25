# ADR-023: Configurable work periods and progress

- **Status:** Accepted for the MVP
- **Date:** 2026-09-25
- **Decision owners:** Project owner
- **Related issues:** None recorded

## Context

Weekly targets and date ranges may vary by cohort or engagement and should not be embedded as
application constants.

## Decision

Represent work periods as relational data with start/end dates and a weekly target. Calculate
expected and actual progress from the selected period and recorded work; keep seeded example
periods as local demo data only.

## Alternatives considered

### One global hard-coded target

Rejected because the required target is a business setting that can differ by period.

### A fixed calendar month report

Deferred because the business target is weekly and period boundaries are configurable.

## Consequences

### Positive consequences

- Target settings can change without recompiling the application.
- Progress can be recalculated for defined periods.

### Negative consequences and trade-offs

- Expected-progress timing and enrolment association require explicit business rules; the schema
  currently has no student-to-work-period enrollment relation.
- SQLite date/time mapping remains dialect-specific until a PostgreSQL port is implemented.

## Security and privacy impact

Progress and work records are user-associated data and must be returned only after ownership or
assignment authorization.

## Operational impact

Local period setup may use explicit opt-in seed data. No cloud data source is deployed.

## Validation

Progress service and API integration tests cover target calculation and response status using
SQLite.
