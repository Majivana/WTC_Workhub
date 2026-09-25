# Security

Security priorities are server-side RBAC, secure authentication, private S3 objects, upload
validation, secret management, HTTPS, VPC isolation, least-privilege IAM, audit logging,
location-data minimization, and protected selfie access. The current code implements storage
privacy and upload validation. Spring Security protects all business endpoints, with only
health and login public.

`POST /api/auth/login` accepts a username and password and returns an eight-hour opaque bearer
token. Passwords are stored as BCrypt hashes; raw tokens are never stored, only SHA-256 token
hashes. Sessions are invalidated on application restart and inactive users cannot authenticate.

Evidence uses the `evidence/` object-key namespace. Attendance selfies use the separate
`attendance-selfies/` namespace and accept only JPEG or PNG files up to 2 MB. Evidence
accepts PDF, JPEG, and PNG files up to 10 MB. Both namespaces use random private keys,
never expose objects through public URLs, and use short-lived presigned URLs when S3 is
configured.

The local storage adapter generates non-public `local-storage://` references and never
requires real personal images. Selfie retention, deletion, legal hold, and exact access
retention periods remain unresolved policy decisions until confirmed by the project owner.

The existing business APIs still expose development-era `userId`/`actorId` fields for backward
compatibility with the current workflow tests. In authenticated bearer-token requests, sensitive
actor and verifier values are replaced with the authenticated principal and ownership/assignment
guards are enforced. Legacy role-only test contexts retain compatibility until those request
schemas are removed.

Administrative management routes are role-protected with `ROLE_ADMIN`. User deactivation is
implemented as a soft state change so historical records remain queryable and referentially
intact.

Authorization uses a centralized permission model separate from work roles. Supported system
roles are `STUDENT`, `SUPERVISOR`, `MENTOR`, `ADMIN`, and `SUPER_ADMIN`; permissions are stored
in `permission` and `role_permission` tables and exposed to the security context as
`PERM_*` authorities. Method checks call the shared authorization service rather than
duplicating role logic in controllers.

## Production-readiness limitations

- The default local profile uses SQLite and the local storage adapter; production deployment
  requires an external database, private object storage, HTTPS, and least-privilege IAM.
- Selfie retention, deletion, legal hold, and exact access-retention periods remain policy
  decisions and must be approved before production rollout.
- OWASP Dependency-Check is not part of the default CI workflow. A local scan attempted during
  the quality pass could not update the NVD database because no NVD API key was configured.
  CI should run a dependency scanner with a configured advisory-data source before release.
- Development-only role-based test contexts remain for backward-compatible integration tests;
  production bearer-token requests use the authenticated principal and centralized ownership
  checks.
