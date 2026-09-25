# ADR-022: Weekly progress reminder

- **Status:** Accepted for local use; AWS schedule proposed only
- **Date:** 2026-09-25
- **Decision owners:** Project owner
- **Related issues:** None recorded

## Context

Students behind expected work-period progress should receive reminders without sending a message
to every student or creating duplicates on retries.

## Decision

Evaluate actual logged/verified progress against each active work period's expected progress and
create an in-app notification only for students behind target. Use a unique period/user/periodic
key for idempotency. Package a Java Lambda handler, but do not configure EventBridge until a
persistent supported database, cohort/enrolment rule, AWS account, and schedule are approved.

## Alternatives considered

### Notify every active student

Rejected because it does not target the students who are behind and creates unnecessary noise.

### Configure an AWS schedule before persistence is supported

Deferred because the handler currently uses the SQLite local profile, while AWS staging is planned
for PostgreSQL and no PostgreSQL runtime exists.

## Consequences

### Positive consequences

- Notifications are targeted and retries remain idempotent.
- Business logic can be called locally and through the Lambda handler.

### Negative consequences and trade-offs

- Current evaluation considers active students against every active period because no explicit
  student-to-period enrolment relation exists.
- EventBridge, Lambda runtime, alarms, and failure destinations remain undeployed.

## Security and privacy impact

Log counts and execution outcomes only; never log private notification details, credentials, or
personal location/evidence data. The Lambda needs no S3 access.

## Operational impact

The package and unit tests are local evidence only. AWS retries, dead-letter handling, secret
delivery, database connections, cost, and schedule timezone require deployment validation.

## Validation

Unit tests cover due-student selection, idempotency, and handler behavior. The package has not been
invoked by AWS Lambda and no EventBridge schedule exists.
