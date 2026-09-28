# System Overview

Workhub is a Spring Boot modular monolith. Implemented modules include identity and permissions,
organization management, first-party attendance, work tracking, evidence, submissions,
verification, notifications, escalations, dashboards, reporting, and audit events. The supported
database and object adapter configuration is local SQLite and local storage.

Clients communicate with the server through JSON REST endpoints. Relational storage keeps
metadata, relationships, workflow state, and historical records. The local adapter is the
verified development path; the S3 adapter is implemented but has not been tested against AWS.
RDS PostgreSQL and Aurora PostgreSQL remain proposed because the current schema and migrations
are SQLite-specific.

The separate React/TypeScript browser client covers student, reviewer, mentor, and administrator
workflows using these same endpoints. Its tests and production build pass, while live browser
acceptance and E2E testing remain open. See [Frontend Product Surface](Frontend-Product-Surface.md).
