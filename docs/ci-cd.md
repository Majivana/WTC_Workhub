# CI/CD workflow

The GitHub Actions workflow at `.github/workflows/ci.yml` runs these ordered stages for pull
requests to `main` and pushes to `main`:

1. **Compile** production and test sources with Java 21 and Maven.
2. **Test** runs the unit and integration suites and retains Surefire reports for 14 days.
3. **Package** builds the deployable Spring Boot JAR and retains it for 14 days.
4. **Container smoke** builds the multi-stage image, waits for Docker health, and requests
   `/actuator/health`.
5. **Publish image** runs only for a push to `main` (or an opted-in staging dispatch from
   `main`), and publishes `ghcr.io/<owner>/<repository>:sha-<commit>` to GHCR. Pull requests
   receive no package-write permission. The tag identifies the source commit; deployments use
   this immutable commit tag rather than `latest`.
6. **Deploy staging** can be requested with **Actions → CI and delivery → Run workflow** and the
   `deploy_staging` input. It requires the protected GitHub `staging` environment and AWS OIDC.
   It updates the configured ECS service and waits for service stability.

All jobs have timeouts. Workflow and job permissions default to read-only; package write access
is scoped to the publish job and OIDC token access to the staging deploy job. Test reports and
the JAR are retained as short-lived workflow artifacts. Do not put AWS access keys, database
passwords, or other credentials in repository files or image layers.

## Staging setup and current gate

Staging is deliberately **not enabled**. No AWS environment has been provisioned, and the
application has no verified PostgreSQL runtime path. Keep the repository/environment variable
`STAGING_DEPLOYMENT_READY` unset or set to `false`. A manual dispatch will fail at the preflight
step until all of these gates are complete:

- Implement and verify PostgreSQL schema/query compatibility and migrations; SQLite is currently
  the only supported database.
- Provision and validate the VPC, private ECS service, database, private evidence bucket, logging,
  health checks, and security boundaries described in [AWS design](aws/README.md).
- Create a GitHub `staging` environment with required reviewers and deployment branch restriction
  to `main`.
- Configure GitHub OIDC in AWS with a trust policy restricted to this repository and the
  `staging` environment. Set these environment variables: `STAGING_DEPLOYMENT_READY=true`,
  `AWS_DEPLOY_ROLE_ARN`, `AWS_REGION`, `ECS_CLUSTER`, `ECS_SERVICE`, `ECS_TASK_DEFINITION`, and
  `ECS_CONTAINER_NAME`. `ECS_TASK_DEFINITION` must name a task definition JSON file committed in
  the repository.
- Scope the assumed role to register the required task definition revision and update/describe the
  one staging ECS service and cluster. It must not administer IAM, networking, S3 policy, or
  unrelated services.
- Ensure ECS can pull the GHCR image. For a private package, configure the task definition's
  `repositoryCredentials` to reference a narrowly scoped, rotated Secrets Manager token; alternatively
  publish to an account-owned ECR repository and use the ECS execution role. Never put the token in
  the task definition as plaintext.
- Store runtime secrets in Secrets Manager, set HTTPS and encrypted storage, choose approved data
  retention, and verify the exact service health path before enabling deployment.

The staging GitHub environment should require an independent reviewer. GitHub environment
protection and AWS OIDC trust must be configured in their respective control planes; the workflow
file alone does not create those controls. See the root [README](../README.md) and
[release checklist](release/README.md) for the wider status and evidence.
