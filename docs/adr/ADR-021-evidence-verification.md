# ADR-021: Versioned evidence and immutable verification history

- **Status:** Accepted for the MVP
- **Date:** 2026-09-25
- **Decision owners:** Project owner
- **Related issues:** None recorded

## Context

Review decisions need to identify the exact submitted material and preserve a trace when evidence
changes or is reviewed again.

## Decision

Store evidence metadata as numbered versions with change notes and private object references.
Verification decisions refer to the evidence version reviewed and are append-only. Submission
state changes and relevant actions write audit events rather than silently rewriting history.

## Alternatives considered

### Overwrite the existing evidence metadata

Rejected because it removes the ability to establish which material was reviewed.

### Keep verification only as the current submission status

Rejected because it loses prior review decisions and correction context.

## Consequences

### Positive consequences

- Reviewers can trace a decision to the precise evidence version.
- Prior review and audit history remains queryable.

### Negative consequences and trade-offs

- Historical metadata and object versions require storage, access, and retention policies.
- Retention and legal-hold periods are unresolved.

## Security and privacy impact

Private object access must be authorized before a short-lived link is issued. Audit entries must
avoid secrets, selfie bytes, precise coordinates, and signed URLs.

## Operational impact

The relational history is implemented locally. S3 lifecycle policy must not delete historical
objects until the owner-approved retention and legal-hold rules are implemented.

## Validation

Evidence versioning and verification integration tests cover version sequencing, decision links,
and append-only history. AWS object lifecycle and restore behavior were not tested.
