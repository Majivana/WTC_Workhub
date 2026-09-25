# Architecture Documentation

This directory contains the architecture baseline for WTC Workhub.

## Initial decisions

- The application is a Spring Boot modular monolith.
- Work periods provide configurable weekly targets.
- Attendance is first-party: clock-in and clock-out require selfie capture and campus geofence validation.
- Evidence and attendance selfie files are private objects; the relational database stores metadata and references.
- Verification history and audit events are append-oriented.
- SQLite is the only implemented and locally tested database. RDS PostgreSQL is a proposed
  staging option and Aurora PostgreSQL is a proposed later production option; neither is
  deployed or currently compatible with the SQLite-specific schema and migration code.

## Diagrams

PlantUML source files are stored in `../diagrams/`. Render them with PlantUML or a compatible
PlantUML extension before placing exported images in project documentation.

The release evidence directory includes a rendered
[application architecture diagram](../release/evidence/application-architecture.svg) that marks
the local SQLite/local-storage path as verified and AWS components as planned.
