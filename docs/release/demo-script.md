# Seven-minute local demonstration runbook

**Duration:** about 7 minutes. **Audience:** assessment/demo reviewers. **Mode:** local seeded
REST API; no AWS account or real personal data is used. This is a recording script, not a claim
that a video has already been recorded.

## Before recording

1. Use a clean checkout with Java 21, Maven 3.9+, Docker, and `curl`.
2. Build and start with synthetic seed data only: `WTC_SEED_DATA=true make run`.
3. Confirm `GET http://localhost:8080/actuator/health` reports `UP`.
4. Authenticate before recording. Keep the token in an environment variable and ensure shell
   tracing is off. For example, with `jq` installed:

   ```bash
   read -rsp 'Demo password: ' DEMO_PASSWORD; echo
   LOGIN_JSON=$(jq -n --arg username student.demo --arg password "$DEMO_PASSWORD" '{username:$username,password:$password}' \
     | curl -fsS -H 'Content-Type: application/json' --data-binary @- http://localhost:8080/api/auth/login)
   WTC_DEMO_TOKEN=$(jq -r .token <<<"$LOGIN_JSON")
   unset DEMO_PASSWORD LOGIN_JSON
   ```

   Do not run with `set -x`, print the variable, or include login response output in the recording.
5. Keep terminal history and the recording free of passwords, tokens, real selfies, and exact
   location data. Use only the documented fake `student.demo` and `supervisor.demo` users.
6. For a short video, use a terminal font size that keeps command output readable and crop out
   unrelated desktop content.

## Recording outline

| Time | Segment | Show |
|---|---|---|
| 0:00–0:40 | Scope and status | README, explain local SQLite is implemented while AWS and PostgreSQL remain planned |
| 0:40–1:20 | Health and authentication | Health response; login as the synthetic student; keep the token in a shell variable and never print it |
| 1:20–2:40 | Work tracking | List activity types, create a work entry for the demo period, read weekly progress |
| 2:40–3:40 | Evidence privacy/versioning | Post evidence metadata only (no file bytes), show the created version/private object reference |
| 3:40–4:40 | Attendance | Show attendance API contract and the integration test evidence; do not submit arbitrary real coordinates or a selfie during the public recording |
| 4:40–5:50 | Review trail | Explain submission states and immutable verification history; show the relevant API documentation or tests |
| 5:50–6:30 | Architecture | Show the architecture SVG and point out local SQLite/local storage versus planned AWS services |
| 6:30–7:00 | Limitations | Show database compatibility result, security/retention decisions, and final acceptance checklist |

## Safe API outline

Use a throwaway local database and only the seeded demo account. Do not copy a bearer token into
the recording or save it in the video. Keep the token in a shell variable and suppress login
response output when recording. The API examples and response shapes are maintained in
[`docs/wiki/API-Documentation.md`](../wiki/API-Documentation.md). Confirm request date and period
validity before posting, because seeded period dates are fixed demonstration data.

Suggested commands to narrate:

```bash
curl -s http://localhost:8080/actuator/health
curl -s -H "Authorization: Bearer $WTC_DEMO_TOKEN" http://localhost:8080/api/activity-types
curl -s -H "Authorization: Bearer $WTC_DEMO_TOKEN" \
  http://localhost:8080/api/users/user-student-demo/work-periods/period-july-august-2026/progress
curl -s -X POST -H "Authorization: Bearer $WTC_DEMO_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"activityTypeId":"activity-student-support","workDate":"2026-08-10","startTime":"09:00","endTime":"11:00","breakMinutes":15}' \
  http://localhost:8080/api/users/user-student-demo/work-periods/period-july-august-2026/work-entries
```

The seeded demo password is `demo-student-password`; do not type it visibly during a public
recording. Login and protected calls should be shown from a prepared shell session with output
redaction. Never show database contents containing tokens or password hashes.

## After recording

- Review the complete video, including screen edges and terminal scrollback, for credentials,
  signed URLs, real personal data, precise location, or an unintended desktop notification.
- Confirm duration is between 5 and 10 minutes and the narration distinguishes implemented,
  locally tested, and planned cloud behavior.
- Upload to the approved YouTube account and visibility setting, then add the real URL to the root
  README and `docs/release/README.md`.
