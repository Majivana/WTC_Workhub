# System Overview

Workhub is a Spring Boot modular monolith with modules for identity, organization, attendance,
work tracking, evidence, submissions, verification, notifications, escalations, reporting,
and audit.

Files are stored privately in S3 in deployed environments. Relational storage keeps metadata,
relationships, workflow state, and historical records.
