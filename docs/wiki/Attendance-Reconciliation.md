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
stabilizing the API contract. The browser requests camera and location permission, captures a JPEG,
and sends image bytes (base64), checksum, size, media type, coordinates, and assigned campus through
the authenticated API. The service validates the location and image metadata and sends bytes to the
configured storage adapter; the browser reports success only after the API returns successfully.
The local browser-to-API workflow and AWS S3 storage have not been manually end-to-end tested in
this review, and no AWS object round trip has been tested. Selfies are supporting attendance
evidence only, not facial-recognition input. Camera/location/network failures require an explicit
operational fallback and retention/deletion policy before production use.

Local seed data configures two active geofences for the seeded Cape Town campus, each with a 150 m
radius: `(-33.9249, 18.4241)` and the additional test point `(-34.036152, 18.675398)`. Attendance
accepts a report inside any active geofence assigned to the user's campus; other coordinates
remain rejected. These coordinates are local demo configuration, not a campus registry or a
production geofence approval.
