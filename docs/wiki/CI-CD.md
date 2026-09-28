# CI/CD

GitHub Actions compiles and tests the Java backend, runs frontend tests and a Vite production
build, packages the Spring Boot application, and builds/smoke-tests the Docker image. Pushes to
`main` publish the immutable commit-tagged image to GHCR. Staging deployment remains manually
requested, OIDC-authenticated, environment-gated, and disabled until the AWS and database gates
are satisfied. See the [pipeline guide](../ci-cd.md).
