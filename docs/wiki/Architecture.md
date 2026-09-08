# Architecture

The MVP uses a modular monolith because it is appropriate for a solo project with a short
delivery window. Modules communicate through application services and domain rules rather than
directly sharing controller logic.

See the PlantUML sources in `docs/diagrams/`:

- `class-diagram.puml`
- `erd.puml`
- `submission-activity.puml`
- `attendance-sequence.puml`
- `deployment.puml`
