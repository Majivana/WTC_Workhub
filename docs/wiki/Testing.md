# Testing

The intended test pyramid consists of unit tests for domain rules, integration tests for
persistence, API/controller tests for authorization and response behaviour, and a limited
golden-path acceptance test. The integration suite covers work-entry rules, activity references,
evidence metadata/versioning, storage policies, local storage, private-object authorization,
authentication, authorization, attendance, submissions, verification, and an API golden path.
See the root README for the release run and its actual test result. AWS permissions, S3
upload/download round trips, and a PostgreSQL runtime are not covered by local tests. The
PostgreSQL 16 schema smoke test fails at the SQLite `PRAGMA` in `schema.sql`; see
[database status](../database/README.md).
