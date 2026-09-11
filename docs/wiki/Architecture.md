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

The currently implemented path is work-entry capture, configurable activity types, evidence
metadata/versioning, private-object storage policy, local storage, and an available S3 adapter.
Authentication, authorization, attendance workflows, submissions, verification, dashboards, and
deployment remain separate planned modules and are not implied by the current implementation.

See the PlantUML sources in `docs/diagrams/`:

- `class-diagram.puml`
- `erd.puml`
- `submission-activity.puml`
- `attendance-sequence.puml`
- `deployment.puml`
