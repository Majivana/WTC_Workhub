# API Documentation

The application exposes JSON REST resources. The current implemented endpoint is:

### Weekly progress

```http
GET /api/users/{userId}/work-periods/{workPeriodId}/progress
Accept: application/json
```

### Activity types

Activity types are configurable records. `GET /api/activity-types` returns active types;
pass `includeInactive=true` to include disabled types. Supervisors or administrators can
create and update types with `POST` and `PUT /{id}`. `DELETE /{id}` deactivates the type
instead of removing it, preserving historical work-entry references. Duplicate names are
rejected.

Work entries must reference an existing active activity type.

### Evidence metadata

`POST /api/work-entries/{workEntryId}/evidence` records an evidence version and its private
object metadata (`objectKey`, `mediaType`, `sizeBytes`, `checksum`, `purpose`, and
`createdBy`). File bytes are not accepted or stored by this API. Only PDF, JPEG, and PNG
metadata is accepted, with a positive size up to 10 MB and a 64-character SHA-256 checksum.
Media types, checksums, object keys, and purposes are normalized before storage. Each
successful upload creates the next evidence version, and `GET` on the same path returns the
latest relational metadata.

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

Planned resource groups are:

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

Each endpoint must document authentication, authorization, validation errors, response shape,
and audit behaviour before it is marked complete.
