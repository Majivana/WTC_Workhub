# Architecture

The MVP uses a modular monolith because it is appropriate for a solo project with a short
delivery window. Modules communicate through application services and domain rules rather than
directly sharing controller logic.

The application uses a client-server design. Clients communicate with Spring Boot controllers
over JSON/HTTP. Controllers translate requests and responses, services enforce business rules,
repositories persist relational data, and storage adapters isolate local development from AWS S3.
The Java implementation uses ordinary object-oriented classes with private fields, constructors,
accessors, and enums where a fixed set of values is appropriate. This keeps the code explicit and
explainable while the project is being learned and assessed.

Implemented modules include authentication and permission-based authorization, work-period
progress, work-entry capture, configurable activity types, evidence metadata/versioning,
submissions, verification history, first-party attendance, dashboards, notifications,
escalations, and CSV reporting. The active local persistence path is SQLite and local object
storage; an S3 adapter exists but has not been tested against AWS. The browser frontend and AWS
deployment remain planned. RDS PostgreSQL and Aurora PostgreSQL are not compatible with the
current SQLite-specific schema and migration code.

See the PlantUML sources in `docs/diagrams/`:

- `class-diagram.puml`
- `erd.puml`
- `submission-activity.puml`
- `attendance-sequence.puml`
- `deployment.puml`
