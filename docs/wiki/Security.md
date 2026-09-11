# Security

Security priorities are server-side RBAC, secure authentication, private S3 objects, upload
validation, secret management, HTTPS, VPC isolation, least-privilege IAM, audit logging,
location-data minimization, and protected selfie access.

Evidence uses the `evidence/` object-key namespace. Attendance selfies use the separate
`attendance-selfies/` namespace and accept only JPEG or PNG files up to 2 MB. Evidence
accepts PDF, JPEG, and PNG files up to 10 MB. Both namespaces use random private keys,
never expose objects through public URLs, and use short-lived presigned URLs when S3 is
configured.

The local storage adapter generates non-public `local-storage://` references and never
requires real personal images. Selfie retention, deletion, legal hold, and exact access
retention periods remain unresolved policy decisions until confirmed by the project owner.
