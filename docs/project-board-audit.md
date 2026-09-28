# Project board alignment record

## Scope and access status

The repository contains milestone links and a prior issue-status note in
[`docs/wiki/Implementation-Status.md`](wiki/Implementation-Status.md), but it does not contain an
issue-board export. On 2026-09-28, the GitHub CLI (`gh`) was unavailable and the web view of the
project board could not be opened from this environment. The live issue or Projects board was not
read or edited in this pass. The references below are the last status recorded in the versioned
documentation, not a claim about current GitHub state.

## Last recorded issue notes

- [Issue #15](https://github.com/Majivana/WTC_Workhub/issues/15) and [issue #16](https://github.com/Majivana/WTC_Workhub/issues/16)
  were recorded as closed and aligned with activity/evidence/storage implementation.
- [Issue #17](https://github.com/Majivana/WTC_Workhub/issues/17) was recorded as implemented and
  tested, but awaiting board closure.
- The Milestone 2 parent issue was recorded with an older unchecked checklist for #15 and #16.
- The five milestone epics are linked from [project objectives](wiki/Project-Objectives.md).

## Frontend scope mapping for board review

The frontend work present in this repository maps to the existing application/API scope as follows:

| Implemented browser area | Backend/domain area | Board follow-up to verify |
|---|---|---|
| Authentication, role-aware navigation | Authentication and RBAC | Confirm the existing milestone ticket includes the browser client, or create/link a frontend issue |
| Student dashboard and work period | Dashboard and progress | Confirm API-backed and not fixture-driven acceptance is recorded |
| Attendance capture | First-party attendance | Keep real camera/location and S3 validation open until exercised |
| Work, evidence, and submissions | Work entries, evidence versioning, submission lifecycle | Include file upload and status-transition validation |
| Review and verification | Supervisor queue and immutable decisions | Include assigned-user authorization and reviewed evidence-version linkage |
| Notifications, escalations, reports | Operational follow-up | Confirm current scope reflects supported APIs and CSV-only reports |
| Administration | Users, institutions, campuses, activity types | Note work-role catalogue/assignment and organization editing gaps |

The frontend build and unit tests now run in GitHub Actions through the `frontend-verify` job. The
browser E2E/manual acceptance work remains open. See [frontend product surface](wiki/Frontend-Product-Surface.md)
for contracts and limitations.

## Requested board actions

After a repository maintainer reviews the live board, update the #17 state and the Milestone 2
checklist if the historical note is still accurate. Ensure a frontend issue is linked to the proper
milestone and list manual browser/E2E acceptance as an open acceptance task. No GitHub issue,
milestone, or Project field was modified by this repository-only change.
