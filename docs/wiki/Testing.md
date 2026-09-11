# Testing

The intended test pyramid consists of unit tests for domain rules, integration tests for
persistence, API/controller tests for authorization and response behaviour, and a limited
golden-path acceptance test. The current suite verifies work-entry rules, activity references,
evidence metadata/versioning, storage policies, local storage behaviour, and private-object
authorization rules. Full authentication/RBAC, attendance, and end-to-end golden-path coverage
remain planned.
