# ADR-014: Explicit object-oriented classes instead of records

## Status

Accepted for the current learning and assessment scope.

## Context

The project is a solo Spring Boot client-server application. The implemented code used Java
records for domain models, JSON request/response DTOs, storage value objects, and progress
results. The project owner needs to understand the object model and explain it to an assessor
before introducing less familiar language features.

## Decision

Use ordinary Java classes for implemented and scaffolded source models and DTOs. Classes use
private fields, explicit constructors, accessors, and setters only where JSON binding requires
them. Existing record-style accessors are retained during the transition so service and
repository code remains readable and changes stay incremental.

Use enums for fixed categories such as storage object type and progress status. Keep JSON as the
client-server representation and keep controllers, services, repositories, and storage adapters
as separate boundaries.

## Consequences

- The object state and construction rules are visible to a beginner.
- Jackson can bind JSON through normal bean properties.
- Existing behaviour and endpoint shapes can be preserved while the implementation is learned.
- Classes require more boilerplate than records.
- Immutable historical concepts still use final fields and are created through constructors; they
  are not edited in place.

## Alternatives considered

- Java records: concise, but deferred until the project owner understands their generated
  methods and immutability semantics.
- JPA entities: rejected for the current scope because the application already uses explicit JDBC
  repositories and does not need an ORM migration.
