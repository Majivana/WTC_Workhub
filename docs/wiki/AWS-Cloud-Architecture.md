# AWS Cloud Architecture

The target design uses an HTTPS Application Load Balancer in public subnets, ECS Fargate tasks
without public IPs in private application subnets, and a private database in database subnets.
RDS PostgreSQL is the proposed MVP database only after PostgreSQL support is implemented; the
current app uses SQLite. S3 stores private evidence and attendance selfies under separate
prefixes. CloudWatch receives sanitized application logs and health alarms. IAM separates the
ECS task execution role from the application task role, whose S3 access is limited to the two
object prefixes.

See the [AWS design and validation plan](../aws/README.md) and
[deployment diagram](../diagrams/deployment.puml) for security groups, HTTPS, encryption,
retention, operational access, data flows, and the deployment verification checklist.

**Verification status:** This is planned architecture. AWS CLI/account access was unavailable
during review; no VPC, ECS service, IAM role, S3 bucket, database, or CloudWatch resource was
deployed or validated. The local Docker image and health check were verified separately. The S3
adapter can issue pre-signed URLs, but AWS object access has not been integration-tested, and the
attendance service currently discards the selfie upload URL. Do not describe AWS as operational.
