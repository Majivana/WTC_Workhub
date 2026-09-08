# AWS Architecture Documentation

The deployment target is a containerized Spring Boot application running on ECS.

The initial deployment specification is available in `../diagrams/deployment.puml`. It shows
VPC networking, ECS, private S3 storage, Lambda reminders, CloudWatch, IAM, RDS staging, and
Aurora production as environment-specific components.

Only AWS components that are deployed and tested may be described as operational in the final
README and demonstration.
