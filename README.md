# WTC Workhub

## Planning

### Delivery objective

Deliver a demonstrable MVP by **20 September 2026**. The delivery is organized around the
student-work submission and supervisor-verification golden path:

```text
Student
  -> assigned WorkPeriod
  -> work entry and activity
  -> evidence upload
  -> submission
  -> supervisor review
  -> immutable verification
  -> notification and dashboard progress
```

### Milestones

| Milestone | Due date | Outcome |
|---|---:|---|
| [Milestone 1 — Project Setup](https://github.com/Majivana/WTC_Workhub/milestone/1) | 10 Sep 2026 | Scope, architecture, repository foundation, local setup, and CI baseline |
| [Milestone 2 — Core Features](https://github.com/Majivana/WTC_Workhub/milestone/2) | 14 Sep 2026 | Work periods, work entries, evidence, submissions, verification, reconciliation, dashboards, and reports |
| [Milestone 3 — Authentication & Users](https://github.com/Majivana/WTC_Workhub/milestone/3) | 16 Sep 2026 | Authentication, users, system roles, work roles, permissions, and authorization |
| [Milestone 4 — Testing & Quality](https://github.com/Majivana/WTC_Workhub/milestone/4) | 19 Sep 2026 | Unit, integration, API, acceptance, security, and production-readiness validation |
| [Milestone 5 — MVP Release](https://github.com/Majivana/WTC_Workhub/milestone/5) | 20 Sep 2026 | Containerization, AWS validation, documentation, demonstration, and final release checks |

### MVP prioritization

#### Must have

- Configurable WorkPeriods and weekly progress calculations
- Work-entry validation and activity tracking
- Evidence metadata and evidence versioning
- Submission state machine
- Immutable verification history and audit events
- Student and supervisor workflows
- Authentication and RBAC
- Attendance import and reconciliation boundary
- Unit, integration, and golden-path tests
- Docker, Makefile, CI, and complete README

#### Should have

- Private S3 adapter and presigned downloads
- Supervisor escalations
- CSV reporting
- In-app notifications
- ECS, VPC, IAM, CloudWatch, and RDS validation

#### Could have

- Weekly reminder Lambda
- Announcements
- Email notifications
- Aurora deployment, if staging is stable and cost is controlled

#### Out of scope for the MVP

- Live biometric integration
- Payroll-system integration
- SMS or WhatsApp
- Native mobile application
- Multi-region deployment
- Advanced analytics or AI review

### Daily delivery plan

| Date | Focus |
|---|---|
| 8 Sep | Freeze scope, document supplied evidence, finalize architecture and ERD |
| 9 Sep | Bootstrap Spring Boot, database profile, seed data, and test foundation |
| 10 Sep | Complete Project Setup milestone, local workflow, documentation skeleton, and CI baseline |
| 11 Sep | WorkPeriod, weekly progress, work-entry model, and validation |
| 12 Sep | Activities, evidence metadata, and storage abstraction |
| 13 Sep | Evidence versioning and S3 integration boundary |
| 14 Sep | Submission state machine, verification history, audit trail, and Core Features milestone |
| 15 Sep | Dashboards, supervisor queue, notifications, and escalations |
| 16 Sep | Authentication, users, RBAC, authorization, and milestone validation |
| 17 Sep | Docker, Makefile, integration tests, and CI hardening |
| 18 Sep | AWS VPC, IAM, ECS, S3, CloudWatch, RDS, and Lambda validation |
| 19 Sep | Security review, acceptance testing, README, diagrams, ADRs, and demo preparation |
| 20 Sep | Final validation, bug fixes, release evidence, demo recording, and submission |

Testing and documentation are completed alongside each feature rather than postponed until the
final days.

### GitHub delivery controls

- [Milestone 1 epic and child issues](https://github.com/Majivana/WTC_Workhub/issues/1)
- [Milestone 2 epic and child issues](https://github.com/Majivana/WTC_Workhub/issues/2)
- [Milestone 3 epic and child issues](https://github.com/Majivana/WTC_Workhub/issues/3)
- [Milestone 4 epic and child issues](https://github.com/Majivana/WTC_Workhub/issues/4)
- [Milestone 5 epic and child issues](https://github.com/Majivana/WTC_Workhub/issues/5)
- Project board: create or organize this from the repository’s **Projects** tab once a token
  with GitHub Projects permissions is available.

Recommended board columns:

```text
Backlog -> Ready -> In Progress -> Code Review -> Testing -> Done
                                      \-> Blocked
```

Issues should be moved only when their actual implementation state changes. Commits must remain
small, meaningful, and linked to the relevant issue.