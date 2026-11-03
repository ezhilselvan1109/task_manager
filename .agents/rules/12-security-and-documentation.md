# Rule 12: Security, Secrets Management & Documentation Synchronization

## Secrets Management
1. **Zero Hardcoded Secrets**:
   - Never commit passwords, private keys, JWT secret keys, or database credentials into Git.
   - Use environment variables with safe development defaults in `application.yml` (e.g., `${JWT_SECRET:dGFza19tYW5hZ2VyX3NlY3JldF9rZXlfMjU2X2JpdF9leGFtcGxlXzEyMzQ1Njc4OTA=}`).
   - Production secrets must be supplied via Docker Compose `.env` or external secret stores.

## Documentation Synchronization Mandate
1. **Living Documentation**:
   - Any modification to database schemas, API contracts, Kafka topics, or environment configurations must immediately be accompanied by an update to the corresponding documentation in `documentation/`.
   - Update `documentation/requirements-traceability-matrix.md` whenever new requirements, APIs, or test cases are introduced.
   - Record architectural trade-offs or modifications as new Architecture Decision Records in `documentation/architecture-decisions/ADR-xxx.md`.
2. **Review Verification**:
   - Maintain the `phase-review-report.md` pattern across all ongoing implementation and maintenance activities.

