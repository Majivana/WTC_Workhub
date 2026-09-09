# API Documentation

The application exposes JSON REST resources. The current implemented endpoint is:

### Weekly progress

```http
GET /api/users/{userId}/work-periods/{workPeriodId}/progress
Accept: application/json
```

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

Planned resource groups are:

- `/auth`
- `/users`
- `/campuses`
- `/work-periods`
- `/attendance-sessions`
- `/work-entries`
- `/evidence`
- `/submissions`
- `/verifications`
- `/notifications`
- `/escalations`
- `/reports`

Each endpoint must document authentication, authorization, validation errors, response shape,
and audit behaviour before it is marked complete.
