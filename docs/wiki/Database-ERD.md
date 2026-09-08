# Database and ERD

The model separates institution from campus, system role from work role, and mutable current
state from historical records.

Important historical concepts are `EvidenceVersion`, `Verification`, and `AuditLog`.
Campus geofence configuration and first-party attendance sessions are part of the proposed MVP.

See `docs/diagrams/erd.puml`.
