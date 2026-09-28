# ADR-024: Private object storage boundary

- **Status:** Accepted for the storage contract; AWS deployment proposed only
- **Date:** 2026-09-25
- **Decision owners:** Project owner
- **Related issues:** None recorded

## Context

Evidence files and attendance selfies should not be stored as public URLs or embedded as bytes in
relational rows. Local development must not require genuine personal images.

## Decision

Keep object bytes behind a storage adapter and persist private object references and metadata in
the relational model. The local adapter uses non-public local references. The S3 adapter uses
separate `evidence/` and `attendance-selfies/` namespaces and short-lived pre-signed requests.
Require an application authorization check before issuing a download URL. The S3 adapter is not
evidence that a bucket, role, policy, or end-to-end upload flow exists.

## Alternatives considered

### Store file bytes in the relational database

Rejected because file lifecycle and transport should remain separate from relational metadata.

### Make bucket objects public

Rejected because attendance and evidence objects contain sensitive personal information.

## Consequences

### Positive consequences

- Local tests use synthetic metadata and local storage without real images.
- S3 access can be constrained by role and private key prefixes.

### Negative consequences and trade-offs

- Browser uploads pass through the application API, increasing API bandwidth and memory pressure
  compared with a direct-to-S3 upload flow.
- S3 upload/download behavior, IAM, encryption, and retention have not been tested in AWS.

### Implementation update (2026-09-28)

The browser now sends evidence file bytes to authenticated content endpoints and attendance JPEG
bytes in the attendance request. The service writes content through the configured storage adapter.
The S3 adapter's presigning support remains available, but the browser does not currently upload
directly to S3. This update records implementation status and does not claim AWS validation.

## Security and privacy impact

Use private buckets, block public access, require TLS, scope IAM to object prefixes, encrypt data,
shorten signed-link lifetime, and never log signed URLs. Owner-approved retention and legal-hold
rules are still required.

## Operational impact

Local storage is the verified adapter for development. AWS object operations and lifecycle remain
planned and carry storage, request, key, monitoring, and transfer cost considerations.

## Validation

Local storage/security integration tests validate namespace, type, size, checksum, and authorization
behavior. No S3 integration test has been performed.
