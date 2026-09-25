# ADR-020: First-party attendance evidence

- **Status:** Accepted for the MVP implementation; policy approvals remain open
- **Date:** 2026-09-25
- **Decision owners:** Project owner
- **Related issues:** None recorded

## Context

Attendance must connect to evidence of work while respecting that attendance alone does not prove
work performed. An external attendance integration and facial recognition are outside the MVP.

## Decision

Record first-party clock-in and clock-out sessions using server time, an active campus geofence,
and a private selfie object reference. Enforce at most one active session per user and retain
corrections as explicit auditable actions. This is supporting attendance evidence, not biometric
identity verification or facial recognition.

## Alternatives considered

### Import an external attendance system

Deferred because no integration contract or authorized source system is available.

### Treat geofence or selfie as proof of identity

Rejected; neither proves a person's identity, and facial recognition is explicitly out of scope.

## Consequences

### Positive consequences

- Attendance, work entries, and evidence can be reconciled in one application.
- Server timestamps and correction events provide a traceable history.

### Negative consequences and trade-offs

- Camera, location permission, accuracy, connectivity, and manual correction require operational
  fallback rules.
- Current development identity compatibility inputs must be removed before production use.

## Security and privacy impact

Selfies and location assertions are sensitive personal data. Keep objects private, minimize
location retention, restrict access, and never log selfie bytes, coordinates, or derived distance.
Retention, deletion, and legal-hold periods require owner approval before deployment.

## Operational impact

Local storage supports tests and demos. The AWS selfie upload contract is incomplete and no real S3
round trip has been tested.

## Validation

Attendance integration tests cover local clock-in/out, validation, correction, and reconciliation
rules. AWS object storage and production identity behavior remain unverified.
