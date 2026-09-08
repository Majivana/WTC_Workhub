# Database Documentation

The relational model supports institutions, campuses, configurable work periods, work entries,
first-party attendance sessions, evidence versions, submissions, verification history, and audit
events.

The initial entity relationship specification is available in
`../diagrams/erd.puml`. The model avoids SQLite-specific business logic so it can be validated
against PostgreSQL for RDS and Aurora.
