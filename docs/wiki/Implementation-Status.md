# Implementation Status

This page is the source of truth for the current code slice. The original project goal remains
unchanged: connect attendance, work entries, activities, evidence, verification, reporting, and
AWS operations into one explainable platform.

## Implemented and tested

- Spring Boot modular-monolith/client-server structure.
- JSON REST endpoints for work entries, progress, activities, evidence metadata, and private
  object download URLs.
- Configurable activity types with duplicate-name validation and safe deactivation.
- Work-entry references to active activities and time validation.
- Relational evidence metadata and append-only evidence versions.
- Evidence/selfie storage policies for media type, size, checksum, and private namespaces.
- Local storage adapter for safe development without real personal images.
- S3 storage adapter and presigned URL boundary; AWS deployment is not yet verified.
- Explicit object-oriented Java classes instead of records, with enums retained where useful.

## Planned and still open

- Authentication and Spring Security integration.
- Real RBAC enforcement using the authenticated principal.
- Attendance capture, selfie workflow, geofence validation, and reconciliation.
- Submission, supervisor verification, audit history, dashboards, notifications, reporting,
  frontend, container deployment, and AWS runtime validation.
- Evidence and selfie retention/deletion policy.

## Issue-board alignment

GitHub issues #15 and #16 are closed and match the implemented activity/evidence/storage work.
Issue #17's evidence-versioning behaviour is also present in the current implementation and
should be reviewed before it is closed. The Milestone 2 parent issue still contains an older
unchecked child checklist for #15 and #16; that checklist should be corrected in GitHub. The
available repository integration is read-only, so this repository cannot claim that board edit
has been performed.
