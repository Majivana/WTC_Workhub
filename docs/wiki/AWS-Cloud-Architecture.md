# AWS Cloud Architecture

- ECS Fargate hosts the Spring Boot application.
- S3 stores private evidence and attendance selfie objects.
- Evidence and selfie objects use separate prefixes and presigned upload/download URLs.
- VPC provides network isolation.
- IAM supplies least-privilege task and function roles.
- CloudWatch receives application and Lambda logs.
- Lambda and EventBridge support weekly reminders.
- RDS PostgreSQL is the staging target.
- Aurora PostgreSQL-compatible is the production target.

Only deployed and tested services may be described as operational in the final assessment.
