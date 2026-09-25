# ADR-017: Aurora PostgreSQL for production

- **Status:** Proposed; not deployed
- **Date:** 2026-09-25
- **Decision owners:** Project owner
- **Related issues:** None recorded

## Context

Aurora PostgreSQL has been discussed as a possible production database. There is no measured
production load, validated PostgreSQL application path, or deployed AWS environment. The current
project is SQLite-only.

## Decision

Treat Aurora PostgreSQL as a future option, not the committed production database. Reconsider it
after PostgreSQL support is implemented, RDS staging compatibility is demonstrated, workload and
availability requirements are measured, and a regional cost estimate is reviewed. No Aurora
cluster has been created or tested.

## Alternatives considered

### RDS for PostgreSQL

Preferred first managed PostgreSQL candidate because the MVP does not yet demonstrate a need for
Aurora-specific scaling or storage behavior. This is itself planned and unsupported by the current
application.

### SQLite

Retained for local development only. Its current implementation is SQLite-specific and it is not
the proposed shared production store for an AWS multi-instance application.

## Consequences

### Positive consequences

- Avoids choosing a more complex managed cluster before availability and scaling needs are known.
- Allows production selection to use measured workload, recovery, and cost data.

### Negative consequences and trade-offs

- A later move from RDS may require operational migration and cost review.
- Aurora cost includes database compute and storage; Aurora Standard also charges for I/O, while
  I/O-Optimized changes the pricing mix. Replicas, Serverless capacity, backups, and data transfer
  affect the estimate. Use the AWS Pricing Calculator with a chosen Region and workload rather
  than carrying over generic rates.

## Security and privacy impact

If selected, use private subnets, TLS, encryption at rest and for backups, least-privilege secret
and database roles, monitored administrative access, and approved retention. Do not treat managed
service status as a substitute for application authorization or audit controls.

## Operational impact

There are no Aurora resources or operational results to report. Pricing varies with region,
instance or Serverless capacity, storage, I/O configuration, replicas, backups, and transfer.
Provision only after budget approval and a workload-based estimate. Compare availability and
recovery requirements with the additional cluster and replica cost.

## Validation

No Aurora compatibility or service test has been performed. The current schema smoke test failed
against PostgreSQL 16 before reaching application queries. Reconsider this ADR only after the
PostgreSQL implementation and staging validation gates in ADR-016 are complete.
