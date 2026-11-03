# Rule 11: Git & Code Review Standards

## GitFlow Branching Strategy
1. **Branch Names**:
   - `main`: Production-ready release baseline.
   - `develop`: Integration branch for tested features.
   - `feature/<ticket-or-id>-<description>` (e.g., `feature/FR-007-task-creation`).
   - `bugfix/<ticket-or-id>-<description>` (e.g., `bugfix/SEC-002-idor-patch`).
   - `release/v<version>` (e.g., `release/v1.0.0`).

## Conventional Commits
1. **Format**:
   - `<type>(<scope>): <subject>`
   - Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`.
   - Examples:
     - `feat(task-service): implement task creation endpoint with outbox event`
     - `test(user-service): add unit tests for BCrypt password hashing`
     - `fix(gateway): resolve CORS header duplication on preflight requests`

## Code Review & PR Checklist
1. **Review Gates**:
   - Does the change conform to the approved SDLC specification?
   - Are there any stubs, placeholders, or `TODO` comments? (Must be zero).
   - Are unit and integration tests included and passing?
   - Is line coverage $\ge 80\%$ and branch coverage $\ge 75\%$?
   - Are secrets or sensitive credentials absent from code, configs, and logs?
   - Is database access strictly isolated to the owning service?
   - Is anti-enumeration (`HTTP 404`) preserved?

