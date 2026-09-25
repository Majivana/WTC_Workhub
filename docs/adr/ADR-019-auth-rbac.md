# ADR-019: Authentication and centralized authorization

- **Status:** Accepted for the current API, with compatibility cleanup open
- **Date:** 2026-09-25
- **Decision owners:** Project owner
- **Related issues:** None recorded

## Context

Business resources require authenticated access and ownership/assignment checks. Role checks
duplicated across controllers are difficult to audit consistently.

## Decision

Use BCrypt password hashes, opaque bearer sessions stored as hashes, and a centralized
role-to-permission model for student, supervisor, mentor, admin, and super-admin roles. Apply
ownership and assignment authorization to business operations. Bearer requests use the
authenticated principal. Legacy identity request fields and role-only test compatibility remain
temporary and must not define a production identity boundary.

## Alternatives considered

### Trust caller-provided user and role values

Rejected for production use because a caller could assert another identity or role.

### External identity provider

Deferred until its issuer, claim, and account lifecycle requirements are selected.

## Consequences

### Positive consequences

- Permission checks share one model and authenticated requests derive actor identity from the
  principal.
- Session tokens are not stored in plaintext.

### Negative consequences and trade-offs

- Sessions are invalidated on application restart in the current implementation.
- Compatibility inputs remain a cleanup item; the API contract is not yet fully principal-only.

## Security and privacy impact

Never log credentials, bearer tokens, or authorization headers. Require TLS in a deployed
environment, enforce least privilege, audit administrative operations, and remove compatibility
identity inputs before production use.

## Operational impact

Local accounts support development. Production identity provider, secret delivery, session
revocation, and incident operations remain deployment decisions.

## Validation

Authentication and authorization integration tests exercise the local implementation. No external
identity provider or AWS runtime was tested.
