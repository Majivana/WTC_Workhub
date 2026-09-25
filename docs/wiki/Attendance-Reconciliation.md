# Attendance Reconciliation

The MVP uses first-party attendance:

- authenticated user
- selfie at clock-in and clock-out
- server timestamps
- approved WeThinkCode_ campus geofence
- private selfie references
- auditable corrections

Attendance presence does not prove work performed. Sessions are reconciled with work entries,
evidence, and verification. No external attendance import or facial recognition is claimed.

The development API uses `POST /api/attendance/clock-in` and
`POST /api/attendance/{sessionId}/clock-out` with a user, assigned campus, work period,
selfie metadata, and reported coordinates. The server supplies timestamps, validates the
configured geofence using distance in metres, stores only a private object reference, and
returns a reconciliation summary on clock-out. Manual corrections require an active
supervisor or administrator, a reason, and retain the previous/new timestamps in
`attendance_correction` plus an audit event.

The API accepts legacy `userId` fields for compatibility, but authenticated bearer requests use
the Spring Security principal and authorization checks. Remove these request fields before
stabilizing the API contract. Selfies are supporting attendance evidence only, not
facial-recognition input. Camera/location/network failures require an explicit operational
fallback and retention/deletion policy before production use. AWS selfie upload is not complete:
the service does not yet pass the pre-signed upload URL to the client, and no AWS object round trip
has been tested.
