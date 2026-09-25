# Release readiness and acceptance

This page records the local release review for 2026-09-25. It does not assert that an AWS
environment, external demo, or public release exists. Release tags are intentionally created only
after every required publication gate is verified.

## Evidence

- [Application architecture (SVG)](evidence/application-architecture.svg), with
  [PNG rendering](evidence/application-architecture.png).
- [Local health endpoint screenshot](evidence/local-health.png).
- [Seven-minute API demonstration runbook](demo-script.md).
- Database strategy, including the PostgreSQL 16 compatibility result:
  [database guide](../database/README.md) and [ADR index](../adr/README.md).
- Cloud infrastructure evidence and deployment status: [AWS design](../aws/README.md).

The checked-in images document local state and intended flows. They are not AWS console evidence.

## Final acceptance checklist

- [x] README gives local SQLite setup, Docker commands, environment variables, feature status, and
  explicit database/AWS limitations.
- [x] System, database, attendance, and target deployment diagrams are maintained in
  `docs/diagrams/`; application architecture is rendered in this release evidence.
- [x] ADRs cover the major implemented persistence, security, workflow, and deployment decisions;
  proposed cloud decisions are labelled planned.
- [x] Current work and validation are recorded in the development journal.
- [x] Security notes identify implemented controls, compatibility fields, and unresolved retention
  and production identity decisions.
- [x] SQLite integration suites pass; full Maven and container validation are recorded below after
  the final release run.
- [ ] A 5–10 minute screen recording has been captured and reviewed.
- [ ] The reviewed demo has been uploaded to YouTube and its URL added to the README.
- [ ] Final release tag created after all required gates pass.

### Demo publication

No video capture or YouTube publishing tool/account is available in this workspace. The timed
runbook is ready, but no recording has been made and no YouTube URL exists. Do not insert a
placeholder link or create a release tag until the recording is captured, reviewed for synthetic
data and privacy, uploaded, and the real URL is added here and to the README.

## Validation record

Completed on 2026-09-25:

- `mvn -q -DargLine=-javaagent:/home/kwanda/.m2/repository/net/bytebuddy/byte-buddy-agent/1.15.11/byte-buddy-agent-1.15.11.jar verify` — passed; 19 suites, 45 tests, 0 failures, 0 errors, 0 skipped.
- `docker build --tag wtc-workhub:release-audit .` — passed; multi-stage image built.
- Local container health check — passed; Docker reported `healthy`, configured user `10001:10001`,
  and `GET /actuator/health` returned `{"status":"UP","groups":["liveness","readiness"]}`.
  See [`local-health.png`](evidence/local-health.png).
- `git diff --check` — passed.
- Markdown link audit — passed, zero missing local links. All five PlantUML files have matching
  `@startuml`/`@enduml` markers.
- SQLite integration is exercised by the full Maven run. PostgreSQL 16 schema smoke test — expected
  compatibility failure at `PRAGMA foreign_keys = ON`; see [database guide](../database/README.md).
- Test repeatability: every `@SpringBootTest` now activates the integration profile and applies
  `src/test/resources/test-database-reset.sql` before each method. This prevents the test suite from
  using `workhub-local.db` and clears foreign-key dependents before fixture setup. The four suites
  from the reported CI failure and the complete 45-test build pass after this change.
- AWS — no account resources were provisioned or tested; all AWS infrastructure remains planned.

The first container launch used a host data directory not writable by forced UID 10001 and exited.
The acceptance run was repeated against the image's own `/data` directory, owned by the non-root
runtime user, and passed. The documented `make docker-run` path maps the caller UID/GID for its
host-mounted data directory.
