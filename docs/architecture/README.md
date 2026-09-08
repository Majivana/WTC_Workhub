# Architecture Documentation

This directory contains the architecture baseline for WTC Workhub.

## Initial decisions

- The application is a Spring Boot modular monolith.
- Work periods provide configurable weekly targets.
- Attendance is first-party: clock-in and clock-out require selfie capture and campus geofence validation.
- Evidence and attendance selfie files are private objects; the relational database stores metadata and references.
- Verification history and audit events are append-oriented.
- SQLite is used for local development; PostgreSQL-compatible RDS and Aurora are deployment targets.

## Diagrams

PlantUML source files are stored in `../diagrams/`. Render them with PlantUML or a compatible
PlantUML extension before placing exported images in project documentation.
