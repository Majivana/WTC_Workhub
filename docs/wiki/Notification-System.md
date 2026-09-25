# Notification System

The MVP uses in-app notifications for submissions received, approvals, requested changes,
missing evidence, attendance exceptions, escalations, and weekly reminders.

Weekly progress reminders run at completed seven-day boundaries from a work period's start. The
expected cumulative hours are `weekly target × completed weeks`; the service creates an in-app
reminder only when period-to-date logged hours are below that expectation. The notification's
period/week key and a unique database index make EventBridge/Lambda retries idempotent. A new
reminder may be created for a later week while the student remains behind.

Submission transitions publish through `SubmissionNotificationHook`, which persists an in-app
notification and logs a summary event. The weekly Lambda handler logs aggregate counts and
rethrows failures so configured Lambda retries and a dead-letter queue can surface failures.
Neither EventBridge nor Lambda has been deployed. The schema lacks student-to-work-period
enrollment, so the current reminder evaluates all active students against every active period;
confirm the single-cohort assumption or add enrollment before enabling overlapping periods.
