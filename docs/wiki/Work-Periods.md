# Work Periods

A WorkPeriod contains a name, dates, weekly target, submission rules, status, and timestamps.
The current 18-hour target is configuration data, not a hard-coded constant.

Weekly progress includes target, logged, verified, pending, remaining, percentage, and status.

`ProgressService` loads the target and inclusive start/end boundaries from the persisted
`WorkPeriod`. It sums only entries whose work date falls within those boundaries. Logged and
verified hours are reported separately; pending hours are the unverified logged hours, remaining
hours never become negative, and percentage is intentionally allowed to exceed 100% so
over-target work is visible. Status values are `BELOW_TARGET`, `ON_TARGET`, and `OVER_TARGET`.

The local 18-hour example is seed data only. Progress calculations do not contain an 18-hour
default or other fixed target.

The current JSON API exposes this calculation at
`GET /api/users/{userId}/work-periods/{workPeriodId}/progress`. API responses use a dedicated
DTO and the `ProgressStatus` enum; database records are not exposed directly to clients.

Work entries submitted through the API must fall within the WorkPeriod dates. Their effective
duration is calculated from start/end times minus break minutes, and entries cannot overlap for
the same user and date.
