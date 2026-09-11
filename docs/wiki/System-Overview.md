# System Overview

Workhub is a Spring Boot modular monolith with planned modules for identity, organization,
attendance, work tracking, evidence, submissions, verification, notifications, escalations,
reporting, and audit. The current implemented slice is work periods, work entries, configurable
activities, evidence metadata/versioning, and storage policy/adapters.

Clients communicate with the server through JSON REST endpoints. Relational storage keeps
metadata, relationships, workflow state, and historical records. The local adapter is the
verified development path; the S3 adapter is implemented as a deployment boundary but has not
been deployed or integration-tested in AWS.
