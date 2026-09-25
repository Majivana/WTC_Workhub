# API Documentation

The application exposes JSON REST resources. The implemented endpoints use ordinary Java DTO
classes for JSON binding; database models are not exposed directly as transport objects.

### Authentication

`POST /api/auth/login` is public and accepts `{"username":"...", "password":"..."}`. A
successful response contains an opaque bearer token and expiry:

```http
Authorization: Bearer <token>
```

The health endpoint is public; all other API endpoints require this header. Passwords are
BCrypt-hashed and bearer tokens are stored only as SHA-256 hashes. Existing endpoint-specific
identity parameters remain temporary compatibility fields and are not a substitute for the
authenticated principal.

- `GET /api/me` returns the authenticated account and its effective permissions.
- `GET /api/work-periods` lists configured periods available to authenticated users.

### Administration

Administrators manage organization and assignment data under `/api/admin`:

- `GET/POST /api/admin/institutions` and `PUT /api/admin/institutions/{id}`
- `GET/POST /api/admin/campuses` and `PUT /api/admin/campuses/{id}`
- `GET/POST /api/admin/users`, `PUT /api/admin/users/{id}`, and
  `POST /api/admin/users/{id}/deactivate`

User creation and updates validate institution/campus ownership, work-role references, active
mentor and supervisor references, and supervisor roles. Deactivation is a soft operation:
historical work entries, attendance, evidence, submissions, and verification records retain
their user foreign keys. Inactive users are excluded from the default user list and cannot log
in; pass `includeInactive=true` to list them.

Business endpoints enforce ownership and assignment using the authenticated bearer principal:
students can access only their own records, supervisors and mentors can review assigned users,
and admin/report operations require the corresponding permission. Legacy identity fields remain
accepted only for compatibility with older role-only test clients and are ignored for bearer
requests.

Authorization is permission-based rather than work-role-based. The system roles
`STUDENT`, `SUPERVISOR`, `MENTOR`, `ADMIN`, and `SUPER_ADMIN` receive permissions through
the persisted `role_permission` mapping. Management endpoints require centralized permission
authorities; a user's work role remains an assignment attribute and does not grant system
access.

### Weekly progress

```http
GET /api/users/{userId}/work-periods/{workPeriodId}/progress
Accept: application/json
```

### Activity types

Activity types are configurable resources. `GET /api/activity-types` returns active types;
pass `includeInactive=true` to include disabled types. Supervisors or administrators can
create and update types with `POST` and `PUT /{id}`. `DELETE /{id}` deactivates the type
instead of removing it, preserving historical work-entry references. Duplicate names are
rejected.

Work entries must reference an existing active activity type.

### Evidence metadata

`POST /api/work-entries/{workEntryId}/evidence` records an evidence version and its private
object metadata (`mediaType`, `sizeBytes`, `checksum`, `purpose`, and optional `changeNotes`). The
authenticated principal supplies the uploader identity. The storage port generates a random
`evidence/<uuid>` key and a private reference. Send raw file bytes to
`PUT /api/work-entries/{workEntryId}/evidence/{objectId}/content` with the file media type as
`Content-Type`; the service checks byte length, media type, and SHA-256 checksum before storage.
`GET` on that content path returns the latest version's bytes only to a user authorized for the
work entry. PDF, JPEG, and PNG files up to 10 MB are accepted. Each successful metadata upload
creates the next evidence version, retains prior versions, and writes an audit event. `GET` on the
metadata path returns the latest relational metadata.

Private object downloads currently use:

```http
GET /api/private-objects/{objectId}/download
```

The service permits the object owner, supervisors, and administrators, and returns a
short-lived local URL or S3 presigned URL. Object keys are never public URLs. The authenticated
principal determines the actor; an `actorId` query parameter supplied by an older client is ignored.

Successful responses contain:

```json
{
  "targetHours": 10,
  "loggedHours": 3,
  "verifiedHours": 2,
  "pendingHours": 1,
  "remainingHours": 7,
  "percentage": 30,
  "status": "BELOW_TARGET"
}
```

`status` is the `ProgressStatus` enum with values `BELOW_TARGET`, `ON_TARGET`, and
`OVER_TARGET`. Unknown work periods return `404 Not Found`. The endpoint serializes DTOs rather
than exposing database records directly.

### Work-entry capture

```http
POST /api/users/{userId}/work-periods/{workPeriodId}/work-entries
Content-Type: application/json
```

Request:

```json
{
  "activityTypeId": "activity-student-support",
  "workDate": "2026-09-10",
  "startTime": "09:00",
  "endTime": "17:00",
  "breakMinutes": 60
}
```

The server calculates `durationMinutes` as elapsed time minus the break and creates a `DRAFT`
entry. A draft can be edited with:

```http
PUT /api/work-entries/{id}
```

Requests are rejected with `400 Bad Request` when required fields are missing, the end is not
after the start, the break is negative or consumes the entire range, the date is outside the
WorkPeriod, or the interval overlaps an existing entry for the user. Editing a non-draft returns
`409 Conflict`; unknown records return `404 Not Found`.
Use `GET /api/users/{userId}/work-periods/{workPeriodId}/work-entries` to list entries for an
authorized user and period, and `GET /api/work-entries/{id}` to retrieve one entry the caller may
access.

Implemented resource groups include:

- `/auth`
- `/users`
- `/campuses`
- `/work-periods`
- `/attendance-sessions`
- `/work-entries`
- `/activity-types` (also available as `/activities`)
- `/evidence`
- `/submissions`
- `/verifications`
- `/notifications`
- `/escalations`
- `/reports`

Some request contracts retain legacy `userId`, `actorId`, or `verifierId` fields for compatibility;
in authenticated bearer-token requests, actor identity and authorization use the principal.
Remove remaining legacy fields before declaring the external API contract stable. The browser
client in `frontend/` consumes the existing REST API; API availability does not imply an AWS
deployment.

### Submissions

`POST /api/work-entries/{workEntryId}/submissions` creates or returns the draft submission
for a work entry. Transition it with `POST /api/submissions/{id}/transitions` and a JSON body
such as `{"status":"SUBMITTED","actorId":"u-student"}`.

The valid lifecycle is:

```text
DRAFT -> SUBMITTED -> UNDER_REVIEW
                       |-> APPROVED
                       |-> REJECTED
                       |-> CHANGES_REQUESTED -> RESUBMITTED -> UNDER_REVIEW
```

Invalid transitions return `409 Conflict`. Approved and rejected submissions are terminal;
changes must be represented by a separate workflow rather than silently mutating the record.

Verification history is available at
`GET /api/submissions/{submissionId}/verifications`. Decisions require `verifierId`,
`evidenceVersionId`, `action`, and a non-empty `comment`; each decision is persisted as a
history record and linked to the exact evidence version reviewed.

### Dashboards, notifications, escalations, and reports

- `GET /api/users/{userId}/work-periods/{periodId}/dashboard` returns progress, attendance
  sessions, work-entry/submission actions, and that user's notifications.
- `GET /api/supervisor/queue?supervisorId={id}` returns pending reviews, attendance exceptions,
  and open escalations. It exposes selfie/location verification state only as session status
  and reconciliation metadata, never image data.
- `GET /api/users/{userId}/notifications` lists in-app notifications and
  `POST /api/users/{userId}/notifications/{id}/read` marks one read.
- `GET /api/admin/reports/work-summary.csv` supports optional `userId`, `workPeriodId`, and
  `status` filters and exports escaped CSV.
- `POST /api/escalations` creates an `OPEN` escalation with `LOW`, `MEDIUM`, or `HIGH`
  severity; `GET /api/escalations` lists unresolved escalations.

Each endpoint must document authentication, authorization, validation errors, response shape,
and audit behaviour before it is marked complete.
