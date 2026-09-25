# AWS Design and Verification Status

## Status

This document is a target design, not a report of a deployed AWS environment. No AWS account,
VPC, ECS service, IAM role, S3 bucket, database, or CloudWatch log group was inspected or changed
for this work: the AWS CLI is not installed in the development environment, so account identity
and live resource state could not be verified. The only container verification for this project
is the local Docker image build and health-check smoke test described in the root README.

The application currently defaults to the `local` profile and SQLite. Its S3 adapter can issue
pre-signed URLs, but this does not prove that an AWS bucket, IAM task role, network path, or
end-to-end upload/download has been configured. The codebase does not currently provide a
verified PostgreSQL deployment path. Database migration and PostgreSQL compatibility are
prerequisites to the RDS option below; do not connect this SQLite-oriented build to RDS and call
it production-ready.

### Database deployment status

| Target | Status as of 2026-09-25 | Evidence |
|---|---|---|
| SQLite local/integration | Implemented and locally tested | `DatabaseIntegrationProfileTests` and `CorePersistenceIntegrationTests` pass |
| RDS PostgreSQL staging | Planned; not provisioned or application-tested | Disposable PostgreSQL 16 schema smoke test fails at SQLite `PRAGMA` in `schema.sql`; no PostgreSQL driver/profile |
| Aurora PostgreSQL production | Planned; not provisioned or application-tested | No Aurora resource inspected; application has the same PostgreSQL compatibility gap |

The local schema check used the official `postgres:16` container image, waited for `pg_isready`,
then piped `src/main/resources/schema.sql` to `psql -v ON_ERROR_STOP=1 -f -`. PostgreSQL exited
with status 3 at line 2: `syntax error at or near "PRAGMA"`. The disposable container was removed
after the check. This is a schema-level smoke test only; it did not test RDS/Aurora services or
application JDBC behavior.

No RDS endpoint, credentials, subnet group, parameter group, or application PostgreSQL
configuration is available. The VPC diagram's PostgreSQL link is a proposed target flow only.
See the [database strategy](../database/README.md) and ADRs 015–017 for decisions and gates.

### Database cost considerations

No cost estimate has been produced because the AWS account, region, staging hours, database size,
availability target, I/O profile, and backup retention are not selected. SQLite adds no managed
database service charge, though workstation storage and backups remain operational concerns.

For a future RDS PostgreSQL estimate, include instance-hours, allocated storage, selected storage
performance/IOPS, backup and snapshot storage beyond any included allowance, cross-AZ or
cross-region transfer, monitoring, and any NAT/interface endpoint networking needed by the
application. Stopping or deleting a staging instance changes compute charges but does not by itself
eliminate retained storage or backup charges; verify the current service behavior and prices before
planning a schedule.

For Aurora PostgreSQL, estimate database instance or Serverless capacity, cluster storage, the
selected Standard or I/O-Optimized configuration, replicas, backups, and data transfer. Aurora
Standard charges for I/O; I/O-Optimized shifts the price mix and has no per-request read/write I/O
charge. Serverless capacity and replica choices still need workload-based sizing. Do not select
Aurora solely on a presumed lower cost or assume it is already deployed.

Before creating either service, price the exact region and configuration in the
[AWS Pricing Calculator](https://calculator.aws/#/) and set a budget alert. Primary references:
[Amazon RDS pricing](https://aws.amazon.com/rds/pricing/) and
[Amazon Aurora pricing](https://aws.amazon.com/rds/aurora/pricing/). Prices and eligible free
allowances can change; no rates are asserted here.

There is also an attendance integration gap: `AttendanceService` requests a pre-signed selfie
upload and stores the object reference, but discards the returned upload URL. The attendance API
does not currently give that URL to the client, so the selfie upload round-trip is not implemented
end to end. Do not claim that clock-in/out selfies have been written to S3 until that contract and
an AWS upload test are implemented.

### Verification performed locally (2026-09-25)

- Built and ran the local Docker image; Docker reported the configured process UID/GID as
  `1000:1000` (non-root) and container health as `healthy`.
- `GET /actuator/health` returned `{"status":"UP","groups":["liveness","readiness"]}`.
- Scanned the container log output and explicit application logging calls for selfie, latitude,
  longitude, geofence, credential, secret, and pre-signed URL terms; no matching log lines/calls
  were found in this check. This is a limited static/runtime log scan, not a comprehensive logging
  or privacy assessment. The application does persist a derived geofence distance in its
  attendance database result; that value must not be exported to routine operational logs.
- AWS identity and resources were not checked because the AWS CLI is absent. No AWS components
  were deployed or tested.

## Target MVP topology

See [`../diagrams/deployment.puml`](../diagrams/deployment.puml) for the network, data, attendance,
and observability paths. The target uses one AWS region and multiple Availability Zones:

- Route public HTTPS through an internet-facing Application Load Balancer (ALB) in public
  subnets, with an ACM certificate and a redirect from HTTP to HTTPS.
- Place ECS Fargate tasks in private application subnets, with public IP assignment disabled.
  The ALB is the only ingress source to the task security group, on the application port.
- Place the MVP relational database in private database subnets with deletion protection,
  automated backups, encryption, and no public endpoint. RDS PostgreSQL is the proposed target,
  conditional on implementing and validating PostgreSQL support first. Use one database per
  environment; do not connect staging and production databases to the same service.
- Store evidence and attendance selfies in a dedicated private S3 bucket. Use separate key
  prefixes (`evidence/` and `attendance-selfies/`) and short-lived pre-signed requests. For the
  target flow, the API validates capture metadata/location and returns a pre-signed S3 upload URL;
  the browser uploads selfie bytes directly to S3 over TLS. The API records the object
  key/reference and attendance metadata in the database. Authorized downloads follow an
  application authorization check before a short-lived pre-signed GET URL is issued. Evidence
  upload URL issuance and private-object download URL issuance exist in code; the selfie upload
  response path is still missing.
- Route task access to S3 through an S3 gateway VPC endpoint. Use interface endpoints for ECR
  image pulls, CloudWatch Logs, and Secrets Manager where practical. If endpoints are not
  provisioned, explicitly budget and restrict NAT egress; do not give tasks public IPs as a
  substitute.
- Send application stdout/stderr to a dedicated CloudWatch Logs group with finite retention;
  use ECS container health checks and ALB target health checks for service health. Add alarms for
  unhealthy targets, repeated task restarts, elevated 5xx responses, and database availability.
- Use EventBridge Scheduler for one configurable weekly invocation of the reminder Lambda. Its
  schedule timezone and weekday/time must be approved by operations; a proposed default is
  Monday 09:00 `Africa/Johannesburg`. Configure Scheduler delivery retries, a maximum event age,
  and a Scheduler dead-letter queue; also configure Lambda asynchronous processing retries and a
  function dead-letter queue for handler failures after invocation. The Lambda rethrows failures,
  and notification uniqueness makes retries idempotent. No schedule or queue is configured here.

### Security groups

| Group | Inbound | Egress |
|---|---|---|
| ALB | TCP 443 from approved client CIDRs (or the public internet if the service is intentionally public); TCP 80 only for HTTPS redirect | App port to the ECS task security group |
| ECS tasks | App port only from the ALB security group | Database port to the database security group; HTTPS 443 to required interface endpoints and the S3 managed prefix list |
| Database | PostgreSQL TCP 5432 only from the ECS task security group | No general internet egress required |
| Interface endpoints | HTTPS 443 only from the ECS task security group | Service-managed |

Use separate security groups and subnets per environment. Do not add SSH/RDP ingress. Restrict
operator access through audited AWS management paths such as Systems Manager, with just-in-time
authorization and MFA. Apply endpoint policies so only the selected bucket, image repositories,
log group, and secret ARNs are reachable. Network controls complement IAM and do not replace it.

## IAM responsibilities

- **Task execution role:** assumed by ECS/Fargate for pulling the approved ECR image, writing the
  container log stream, and retrieving explicitly referenced runtime secrets. It does not receive
  application S3 object permissions.
- **Application task role:** assumed by the Workhub process through the ECS task credential
  endpoint. Grant only `s3:PutObject` on `arn:aws:s3:::<bucket>/evidence/*` and
  `arn:aws:s3:::<bucket>/attendance-selfies/*`, plus `s3:GetObject` on those same prefixes for
  pre-signing authorized downloads. Add `s3:AbortMultipartUpload`/multipart list permissions only
  if multipart transfer is implemented. Avoid `s3:*`, bucket-wide object access, public ACLs,
  and `s3:ListBucket` unless a demonstrated code path requires it. If bucket default encryption
  uses a customer-managed KMS key, scope `kms:GenerateDataKey` and `kms:Decrypt` to that key and
  constrain use to S3 and the bucket encryption context.
- **Task role trust policy:** trust only `ecs-tasks.amazonaws.com`; scope `aws:SourceAccount` and
  `aws:SourceArn` to this account and the intended ECS cluster where supported. Do not place
  long-lived AWS access keys in environment variables or the image.
- **Reminder Lambda execution role:** trust `lambda.amazonaws.com`; grant only CloudWatch log
  stream writes, required VPC network-interface actions for a VPC-attached Lambda, read access to
  the single database secret, and database network reachability through its security group. It
  needs no S3 object permissions. The EventBridge Scheduler invoke role separately receives only
  `lambda:InvokeFunction` on this function. The Lambda needs a PostgreSQL-compatible database
  connection before AWS use; today it starts the SQLite local profile and is not deployable
  against the proposed RDS target.
- **Deployment/operator roles:** separate CI deployment, infrastructure administration, and
  read-only operational access. Require MFA and audit changes. Runtime task roles must not mutate
  IAM, networking, ECS service configuration, bucket policy, retention, or CloudWatch alarms.

The existing adapter creates pre-signed PUT/GET URLs using the AWS SDK's default credential
provider chain. In ECS, that chain should obtain temporary credentials from the application task
role. The application must authorize a user before returning a download URL. Pre-signed URLs are
bearer credentials: never log them, keep their lifetime short, and do not place them in analytics,
browser referrers, or persistent client storage. Restrict upload content type/size/checksum at
both the API and S3 boundary where supported; the current adapter signs content type and length
but its end-to-end upload contract has not been verified against S3.

## S3 privacy, encryption, and retention

- Use a dedicated bucket per environment, S3 Block Public Access at account and bucket levels,
  Object Ownership `Bucket owner enforced`, and a bucket policy that denies non-TLS requests.
- Enable default SSE-KMS encryption with a dedicated customer-managed key if its operational
  ownership and recovery are staffed; otherwise use S3-managed encryption while recording that
  key separation is not provided. Scope key administrators separately from data readers and
  writers. Enable bucket versioning only with a defined lifecycle for old versions and delete
  markers.
- The task role is the only routine application principal with object access. Humans should use
  audited, time-limited role assumption for approved support cases. Do not grant public-read
  access. Keep evidence and selfie prefixes distinct for policy, audit, and lifecycle targeting.
- Set lifecycle expiration only after the data owner approves a retention schedule for attendance
  selfies, evidence, versions, and incomplete uploads. The retention period and legal hold
  requirements are unresolved; do not invent a period or enable irreversible expiration before
  approval. Use lifecycle cleanup for incomplete multipart uploads after multipart is enabled.
- Encrypt database storage, snapshots, backups, task ephemeral storage where applicable, and
  transport links. Keep credentials in Secrets Manager or a supported identity provider, not in
  source, container layers, task definitions as plaintext, or logs.

## Attendance, privacy, and logs

The intended path is HTTPS from the client to the API; the client supplies selfie bytes and a
location assertion for clock-in/out; the server checks the campus geofence and server time; selfie
objects go to the private attendance prefix; and the database stores the attendance session,
capture result, object reference, and audit event. The current code stores a derived distance in
`location_result`; precise coordinates should not be copied into CloudWatch logs or routine
application audit messages. Define whether storing that derived distance is necessary and apply a
documented retention policy to location-related database fields.

Logs must contain operational facts only: request/correlation ID, route template, status, latency,
exception class, and non-sensitive actor/entity identifiers only where justified. Never log
request/response bodies, selfie bytes, coordinates, geofence distance, authentication headers,
cookies, passwords, secrets, S3 URLs, or pre-signed URL query strings. Restrict log-group access,
set finite retention, encrypt log groups, and audit access. Application logs do not replace the
append-oriented business audit records. Configure load balancer access logs with the same privacy
review; disable or redact query strings and sensitive headers before enabling them.

Use HTTPS/TLS from clients to the ALB and TLS for service-to-service calls. Configure health
probes so liveness does not require external services and readiness reflects required database
availability. Do not expose actuator details publicly; permit only health information needed by
the ALB/container check. Rotate secrets and KMS keys under documented procedures, test backup
restore, review IAM/access logs, and define an incident process for credential or data exposure.

## Validation plan before deployment

1. Select the AWS account, region, environment, budget ceiling, data owner, and retention schedule.
2. Implement PostgreSQL driver/schema/query compatibility and production identity enforcement;
   verify migrations and backups with a disposable non-production database.
3. Provision VPC/subnets/routes/endpoints, security groups, ALB/ACM, ECS cluster/service, roles,
   bucket/KMS key, database, log groups, alarms, and secrets as reviewed infrastructure as code.
4. Review generated IAM policies and bucket policy; confirm public access blocks, encryption,
   TLS-deny, version/lifecycle settings, and absence of long-lived credentials.
5. Deploy a non-production task. Verify the task is non-root, receives only its task role, cannot
   access unrelated buckets, and has no public IP. Test authorized and unauthorized upload and
   download, expiry of pre-signed URLs, object privacy, health checks, database connectivity,
   secret rotation, logs, alarms, and backup restore using synthetic data only.
6. Record AWS account/region, resource identifiers, test date, test results, and redacted evidence
   in the deployment journal. Never include real selfies, coordinates, credentials, or signed
   URLs in evidence.

The reminder handler has local automated coverage and can be packaged as
`target/weekly-reminder.zip` using `mvn -Pweekly-reminder-lambda package`. The package and handler
have not been invoked by the AWS runtime. EventBridge Scheduler deployment is deferred: AWS
account access and an approved cost boundary are not available in this workspace. The Lambda
still needs a supported persistent database and configured secret/profile before deployment.
The current schema also has no student-to-work-period enrollment relation; implementation
evaluates all active student accounts against every active work period. Confirm that deployment
uses one shared active cohort or add explicit enrollment before enabling concurrent periods.

When the database and runtime prerequisites are met, configure the Lambda handler as
`co.za.millenniumsolutions.aws.WeeklyReminderLambdaHandler`. Review Scheduler delivery retries
separately from Lambda's asynchronous processing retry and dead-letter settings; they cover
different failure points.

Until those checks have been completed, describe VPC/ECS/RDS/S3/IAM/CloudWatch as **designed**,
not deployed, tested, or operational.

## AWS reference documentation

- [ECS IAM role responsibilities and best practices](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/security-iam-roles.html)
- [ECS task IAM role](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-iam-roles.html)
- [ECS VPC endpoints](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/vpc-endpoints.html)
- [S3 gateway endpoints](https://docs.aws.amazon.com/vpc/latest/privatelink/gateway-endpoints.html)
- [S3 Block Public Access](https://docs.aws.amazon.com/AmazonS3/latest/userguide/access-control-block-public-access.html)
- [S3 bucket policy examples, including HTTPS-only access](https://docs.aws.amazon.com/AmazonS3/latest/userguide/example-bucket-policies.html)
- [Sharing objects with pre-signed URLs](https://docs.aws.amazon.com/AmazonS3/latest/userguide/ShareObjectPreSignedURL.html)
- [EventBridge Scheduler schedule types and time zones](https://docs.aws.amazon.com/scheduler/latest/UserGuide/schedule-types.html)
- [EventBridge Scheduler dead-letter queues](https://docs.aws.amazon.com/scheduler/latest/UserGuide/configuring-schedule-dlq.html)
- [Lambda asynchronous retry and failure handling](https://docs.aws.amazon.com/lambda/latest/dg/invocation-async-error-handling.html)
- [Java Lambda handler contract](https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html)
