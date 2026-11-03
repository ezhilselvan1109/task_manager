# Workflow 09: Unit and Integration Testing Execution

## 1. Overview & Purpose
Defines the execution, coverage measurement, and validation procedure for unit and integration test suites across all microservices.

- **Workflow ID**: `WF-009`
- **Trigger**: Pre-commit validation, pull request verification, or CI test stage execution.

## 2. Preconditions
- Source code and test classes compiled.
- Docker daemon running if executing Testcontainers integration tests.

## 3. Required Inputs
- Target module or repository root.
- Test scope (unit only, or full integration).

## 4. Execution Steps
1. **Execute Unit Tests**:
   - Run: `mvn clean test` across all modules.
   - Assert all unit tests pass with zero failures and zero errors.
2. **Execute Integration Tests**:
   - Run: `mvn verify -Pintegration-tests` (or `mvn test -Dtest=*IT`).
   - Confirm Testcontainers spins up PostgreSQL and Kafka, executes migrations, and completes tests cleanly.
3. **Generate Coverage Report**:
   - Run JaCoCo report: `mvn jacoco:report`.
   - Inspect coverage in `target/site/jacoco/index.html`.
4. **Assert Coverage Gates**:
   - Confirm line coverage $\ge 80\%$ and branch coverage $\ge 75\%$.

## 5. Skills to Use
- [`unit-testing-junit-mockito`](file:///d:/learning/task_manager/.agents/skills/unit-testing-junit-mockito/SKILL.md)
- [`integration-testing-testcontainers`](file:///d:/learning/task_manager/.agents/skills/integration-testing-testcontainers/SKILL.md)

## 6. Rules to Follow
- [Rule 08: Testing & TDD Standards](file:///d:/learning/task_manager/.agents/rules/08-testing-and-tdd.md)

## 7. Tests and Validation
- Surefire test reports in `target/surefire-reports/`.
- Failsafe test reports in `target/failsafe-reports/`.

## 8. Failure Handling
- If a test fails, do not proceed with commit or deployment; initiate Workflow 08 (`bug-investigation-and-fix`).

## 9. Review Checklist
- [ ] 0 failed tests, 0 skipped critical tests.
- [ ] Line coverage meets or exceeds 80%.
- [ ] Test execution completes within acceptable time limit.

## 10. Expected Outputs
- Completed test execution logs.
- JaCoCo code coverage summary.

