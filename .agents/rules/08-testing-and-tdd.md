# Rule 08: Testing & TDD Red-Green-Refactor Standards

## Test-Driven Development (TDD) Workflow
1. **Red Stage**:
   - Write unit or integration tests that fail before writing production code.
   - Run Maven test to confirm failure: `mvn test -Dtest=MyServiceTest`.
2. **Green Stage**:
   - Write the minimal production code necessary to make the test pass.
   - Run tests to confirm green execution.
3. **Refactor Stage**:
   - Clean up code, remove duplication, and optimize without altering external behavior.
   - Ensure all tests remain green.

## Unit & Integration Testing Standards
1. **Frameworks**:
   - JUnit 5 (`org.junit.jupiter.api.*`) and AssertJ (`org.assertj.core.api.Assertions.*`).
   - Mockito (`org.mockito.*`) for isolating unit test dependencies.
   - Never use JUnit 4 (`org.junit.Test` is prohibited).
2. **Coverage Thresholds**:
   - Minimum **80% line coverage** and **75% branch coverage** across all service and domain packages.
3. **Integration Testing with Testcontainers**:
   - Integration tests must use real PostgreSQL and Apache Kafka containers managed via `org.testcontainers`.
   - Never use in-memory H2 databases for JPA testing because H2 does not replicate PostgreSQL UUID functions, JSONB columns, or `SKIP LOCKED` concurrency semantics.
4. **Test Naming Convention**:
   - `should<ExpectedBehavior>_when<StateUnderTest>()` (e.g., `shouldReturn404_whenTaskBelongsToAnotherUser()`, `shouldPersistOutboxEvent_whenTaskIsCreated()`).

