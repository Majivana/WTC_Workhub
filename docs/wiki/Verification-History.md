# Verification History

Every supervisor decision creates an immutable verification record containing the submission,
verifier, action, timestamp, comment, and reviewed evidence version.

Current status may be displayed for convenience, but history remains authoritative.

`POST /api/submissions/{submissionId}/verifications` records a decision against the exact
evidence version reviewed and transitions an `UNDER_REVIEW` submission to `APPROVED`,
`CHANGES_REQUESTED`, or `REJECTED`. Only active supervisors and administrators may verify,
workers cannot verify their own submissions, and a second decision for an already-resolved
submission is rejected.

`GET /api/submissions/{submissionId}/verifications` returns all decisions in chronological
order. Verification and audit repositories expose insert/query operations only; there are no
update or delete operations for history records.
