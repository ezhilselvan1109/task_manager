# Workflow 11: Refactoring Workflow

## 1. Overview & Purpose
Defines the safe, non-breaking procedure for refactoring complex code, reducing technical debt, and improving readability without changing external behavior.

- **Workflow ID**: `WF-011`
- **Trigger**: High cyclomatic complexity, code duplication, or modernization opportunities.

## 2. Preconditions
- Class or component has an existing, passing unit test suite with high coverage.
- Git working directory is clean.

## 3. Required Inputs
- Target source file and target refactoring goal (e.g. convert DTOs to Java records, simplify switch statement).

## 4. Execution Steps
1. **Establish Green Baseline**:
   - Run tests: `mvn test -Dtest=TargetClassTest`.
   - Confirm all existing tests pass before touching code.
2. **Apply Single Incremental Refactoring**:
   - Perform one atomic structural improvement (e.g. extract method, replace class with record, apply MapStruct mapper).
3. **Verify Regression-Free (Green)**:
   - Re-run test suite immediately: `mvn test -Dtest=TargetClassTest`.
   - If tests fail, diagnose and fix or immediately rollback.
4. **Repeat for Subsequent Improvements**:
   - Apply refactorings incrementally, verifying tests after each micro-step.
5. **Final Clean-Up**:
   - Run linter and formatting checks: `mvn checkstyle:check`.

## 5. Skills to Use
- [`code-review-refactoring`](file:///d:/learning/task_manager/.agents/skills/code-review-refactoring/SKILL.md)
- [`java-21-development`](file:///d:/learning/task_manager/.agents/skills/java-21-development/SKILL.md)
- [`unit-testing-junit-mockito`](file:///d:/learning/task_manager/.agents/skills/unit-testing-junit-mockito/SKILL.md)

## 6. Rules to Follow
- [Rule 02: Java 21 & Spring Boot Standards](file:///d:/learning/task_manager/.agents/rules/02-java21-and-spring-boot.md)
- [Rule 08: Testing & TDD Standards](file:///d:/learning/task_manager/.agents/rules/08-testing-and-tdd.md)

## 7. Tests and Validation
- 100% pass rate on existing unit and integration tests.
- Zero change in public API contracts or external behaviors.

## 8. Failure Handling
- If any test breaks, immediately discard uncommitted changes using `git checkout -- <file>` and re-evaluate approach.

## 9. Review Checklist
- [ ] No behavioral change occurred.
- [ ] All tests remain green.
- [ ] Code is cleaner and adheres to Java 21 best practices.

## 10. Expected Outputs
- Refactored source code.
- Clean test run verifying zero regression.

