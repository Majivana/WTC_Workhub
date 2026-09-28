# Browser frontend product surface

The browser app in `frontend/` is a separate React/TypeScript client for the Spring Boot API. The
API and its tests define the contract. Frontend role checks improve navigation and feedback; every
data operation remains protected by backend authentication and authorization.

## User workflows and API backing

| User task | Browser surface | Existing API contract | Current boundary |
|---|---|---|---|
| Sign in and restore identity | `/login`, protected `/app/*` routes | `POST /api/auth/login`, `GET /api/me` | Token in tab-scoped `sessionStorage`; no backend logout/revocation route |
| Check progress and next actions | `/app` | Work periods, student dashboard, supervisor queue | Dashboard JDBC summary fields are normalized in the frontend API adapter |
| Record attendance | `/app/attendance` for students | Clock-in/out request with server-side geofence, selfie metadata and image bytes | Camera/location flow exists; no manual browser acceptance or real S3 test recorded |
| Record work and submit | `/app/work` | Work-entry create/update, submission create/transition | Current API has no written work-description field |
| Attach evidence | Work-entry evidence workspace | Evidence metadata plus authenticated content upload/download endpoints | PDF/JPEG/PNG up to 10 MB; previous versions retained by the API |
| Review a submission | `/app/review` | Assigned queue, work entry, evidence, verification history and decision APIs | Review decision is linked to the exact evidence version; no reviewer E2E run recorded |
| Follow updates | `/app/notifications` | User-scoped list and mark-read endpoint | Related record references are displayed as identifiers; the API does not expose a universal deep-link contract |
| Raise an operational issue | `/app/escalations` | Open escalation list and create endpoint | Current API has no resolve/reassign operation |
| Manage accounts and reference data | `/app/administration` | Users, institutions, campuses and activity types | Work-role catalogue/assignment is not available in this client |
| Export work summary | `/app/reports` | Permission-protected CSV with user, period and work-status filters | CSV only; the API does not currently provide chart data |

## Authorization model

Navigation and protected routes use effective permissions from `GET /api/me` plus the role
fallbacks that match current API controller rules. Administration sections are separated by
`USER_MANAGE`, `ORGANIZATION_READ`, and `ORGANIZATION_MANAGE`. The API is still authoritative and
may return `401`, `403`, `404`, or `409` for any operation.

## Validation recorded on 2026-09-28

- `npm test`: 2 test files, 3 tests passed. Coverage is the central API client and dashboard JDBC
  row normalization.
- `npm run build`: passed with route-level code splitting.
- No live backend browser run, manual responsive/accessibility review, or end-to-end suite has been
  performed in this review. AWS, camera/location production behavior, and S3 uploads remain
  unverified.

See [frontend setup and limitations](../../frontend/README.md), [implementation status](Implementation-Status.md),
[API documentation](API-Documentation.md), and [security](Security.md).
