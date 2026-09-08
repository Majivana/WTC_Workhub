# WTC Workhub

## Repository verification

Verification code: `WTC-5ECA3RJ3`

## Local development

The application requires **Java 21** and **Maven 3.9+**.

```bash
mvn test
mvn package
mvn spring-boot:run
```

The local profile starts on port `8080`. The health endpoint is available at
`http://localhost:8080/actuator/health`.

## Planning

### Current-system analysis

This analysis is based only on the supplied attendance register, Evidence of Work emails,
feedback forms, review form, and self-evaluation form. It does not claim access to the
underlying KwantuGo, Evidence Drive, feedback portal, GitLab, or payroll systems.

#### [CURRENT SYSTEM EVIDENCE]

- Attendance is recorded in KwantuGo and is described as biometric attendance.
- The supplied attendance report contains sites/teams, people, dates, clock-in times,
  clock-out times, and recorded daily durations.
- The July–August 2026 payment cycle covers **21 July–20 August 2026**.
- Days without a sign-out are excluded from the current hours calculation.
- Days with a sign-out after 18:00 are also excluded from the current hours calculation.
- Attendance confirms presence, but does not by itself confirm the work performed.
- Evidence of Work must describe the date, activities, people assisted, support provided,
  topics/projects/exercises, technical or academic assistance, supporting documentation,
  and signatures.
- Evidence was submitted to an Evidence Drive using campus folders and a
  `Name_Surname_Role` naming convention.
- The supplied emails specify first-round and final submission deadlines and warn that
  insufficient evidence can affect payroll processing.
- One follow-up email identifies attendance days with no corresponding Evidence of Work.
- Peer Tutor feedback asks for daily work descriptions, student support, workshops,
  planning, code reviews, GitLab links, evidence, challenges, and escalation concerns.
- The review form records a date, student assisted, workshop/content description, and
  signatures.
- The self-evaluation form records reflection on learning, guidance, workload, and
  collaboration.

#### [INFERENCE]

The evidence indicates a fragmented process in which attendance, work evidence, feedback,
repositories, email communication, and payroll review are maintained separately. This
requires manual reconciliation and makes it difficult to determine whether recorded hours
are supported by sufficiently detailed evidence.

Likely operational risks include missing evidence, incomplete signatures, excluded
attendance records, late corrections, duplicate data entry, and unclear review ownership.
These are inferences from the supplied documents and are not claims about undocumented
system behaviour.

#### [PROPOSED REDESIGN]

WTC Workhub will provide one traceable workflow connecting first-party attendance sessions,
work entries, activities, evidence, submissions, supervisor verification, notifications,
dashboards, and reports.

For the MVP, students and supervisors will sign in to Workhub and use the device camera to
capture a selfie when they start and end an attendance session. The system will record the
server time for each event and calculate the session duration from the clock-in and clock-out
timestamps. A location check must confirm that the device is within an approved WeThinkCode_
campus boundary before a session can start or end.

This is a proposed attendance-control mechanism, not facial-recognition or biometric identity
verification. The selfie and location are supporting attendance evidence and must be protected
as sensitive personal data. The design must define how failed location checks, camera denial,
offline devices, duplicate clock-outs, abandoned sessions, and manual corrections are handled.
There is no external attendance-system integration or imported/mock attendance dataset in the
MVP design.

The system will still preserve the distinction between attendance and evidence: being present
on campus does not by itself prove what work was completed. Attendance sessions therefore remain
reconcilable with work entries, activities, evidence, and supervisor verification.

### Proposed first-party attendance workflow

```text
User signs in
  -> camera permission and selfie capture
  -> campus geofence validation
  -> server records clock-in time, location result, and selfie reference
  -> user performs work
  -> camera permission and selfie capture
  -> campus geofence validation
  -> server records clock-out time
  -> session duration is calculated
  -> session is reconciled with work entries and evidence
```

Attendance implementation requirements:

- Store server-generated timestamps rather than trusting the device clock.
- Store campus coordinates and an approved geofence radius as configuration.
- Store selfie metadata and private object references, not public image URLs.
- Encrypt and restrict selfie access.
- Record failed attempts and relevant audit events without exposing images in logs.
- Prevent a second active session for the same user.
- Require a valid active session before claiming time-based work where applicable.
- Make corrections explicit and auditable rather than silently changing timestamps.
- Provide a documented fallback for camera, location, or network failure.

### Source-derived requirements

The following requirements are directly supported by the supplied documents:

1. Record attendance dates, clock-in times, clock-out times, and recorded durations.
2. Identify incomplete attendance records, including missing sign-outs.
3. Identify attendance records excluded by current business rules, including late sign-outs.
4. Record the date on which work was completed.
5. Record detailed activities and the specific support provided.
6. Record the people or groups assisted.
7. Record topics, projects, exercises, troubleshooting, debugging, code reviews, and
   technical or academic guidance where applicable.
8. Attach supporting documents, screenshots, notes, presentations, attendance records,
   repository links, and other relevant evidence.
9. Capture worker and assisted-person confirmations or signatures where required by the
   applicable evidence process.
10. Support submission deadlines and late-submission visibility.
11. Compare claimed work hours with first-party attendance sessions.
12. Identify attendance sessions that have no corresponding work evidence.
13. Support review, correction, and resubmission of incomplete evidence.
14. Capture daily feedback, challenges, blockers, concerns, achievements, and support needs.
15. Capture feedback about engagement, participation, collaboration, confidence,
   understanding, problem-solving, improvement, strengths, and challenges.
16. Retain enough context to support payroll-oriented verification without claiming that
   Workhub is a payroll system.

### Unresolved questions

These questions require confirmation before the corresponding behaviour is treated as an
authoritative business rule:

- What exact institutions, campuses, site codes, and team structures must be seeded?
- Are Johannesburg and Cape Town the only active campuses for the MVP?
- Which partner institutions and campus/location names are official?
- What is the authoritative source for a student's supervisor, mentor, and work role?
- Is the 18-hour weekly target universal, or does it vary by work period, role, campus,
  or contract?
- How are partial weeks at the beginning and end of a work period calculated?
- What exact rules define a workday, break, overtime, late sign-out, and payable hours?
- Should a work entry be required for every attendance day, or can one entry cover multiple
  people or activities?
- Which evidence types and file sizes are permitted?
- Are signatures required as uploaded files, typed confirmations, or an external process?
- Who may request changes, reject evidence, approve evidence, and reopen an approval?
- What is the formal correction deadline after a change request?
- What are the approved WeThinkCode_ campus coordinates and geofence radii?
- What location accuracy threshold is acceptable before clock-in or clock-out is rejected?
- Are selfie images retained, and if so, for how long and who may view them?
- Is a selfie used only as attendance evidence, or is any identity comparison required?
- What happens when camera permission, location permission, GPS accuracy, or network access
  is unavailable?
- Are manual attendance corrections permitted, who may approve them, and how are they audited?
- Can a supervisor clock in/out using the same campus restrictions, or do supervisors have
  different attendance rules?
- Which notification channels are required for the MVP: in-app, email, or both?
- What retention period and access rules apply to evidence, attendance, and audit records?
- Which reports are required by operations, supervisors, or payroll reviewers?
- What exact data may be used in a public demonstration or repository?
- Is an RDS staging database and an Aurora production database expected to be deployed,
  or is documented architecture sufficient for assessment?
- What AWS budget and account restrictions apply to ECS, RDS, Aurora, and S3?

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
- First-party campus attendance with selfie and location evidence
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

- External biometric or attendance-system integration
- Facial-recognition identity verification
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

### Documentation

- [Architecture specification](docs/architecture/README.md)
- [Database specification](docs/database/README.md)
- [AWS specification](docs/aws/README.md)
- [PlantUML diagrams](docs/diagrams/)
- [Architecture Decision Records](docs/adr/README.md)
- [Development journal](docs/journal/README.md)
- [Publish-ready Wiki content](docs/wiki/README.md)