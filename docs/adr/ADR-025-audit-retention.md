# ADR-025: Append-oriented audit and retention controls

- **Status:** Accepted for MVP history; retention policy pending owner approval
- **Date:** 2026-09-25
- **Decision owners:** Project owner
- **Related issues:** None recorded

## Context

Evidence review, submission changes, attendance corrections, and administrative actions need a
traceable history. Sensitive data also requires bounded access and a defined retention schedule.

## Decision

Persist audit events alongside business operations and keep verification/correction history
append-oriented. Operational logs report system outcomes without copying selfie data, exact
coordinates, secrets, or signed URLs. Do not set irreversible evidence or selfie expiration until
the data owner approves retention and legal-hold requirements.

## Alternatives considered

### Overwrite historical state without an event

Rejected because reviewers cannot identify who changed a material decision or attendance record.

### Choose retention periods without the data owner

Rejected because no approved policy or legal-hold instruction is available.

## Consequences

### Positive consequences

- Material workflow actions retain an audit trail.
- Log minimization reduces unnecessary exposure of sensitive attendance and evidence data.

### Negative consequences and trade-offs

- Append-only records increase storage over time.
- Retention, deletion, legal hold, and access-review schedules remain deployment prerequisites.

## Security and privacy impact

Restrict audit and log access, set finite log retention, review administrative access, and keep
personal data out of routine operational logs. Protect audit integrity and include approved
deletion procedures for data that reaches its retention limit.

## Operational impact

CloudWatch log retention, encryption, access, and alarms are part of the proposed AWS design only.
They have not been configured in an AWS account.

## Validation

Integration tests exercise append-oriented workflow history. Logging review found no selfie or
precise-coordinate logging in the reviewed paths; this is not a full privacy audit.
