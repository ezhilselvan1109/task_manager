# Workflow 08: Bug Investigation and Fix

## 1. Overview & Purpose
Establishes a systematic, test-driven protocol for reproducing, root-causing, patching, and verifying software defects without regression.

- **Workflow ID**: `WF-008`
- **Trigger**: Bug report, failed test, or production incident.

## 2. Preconditions
- Clear description of expected vs actual behavior.
- Access to application logs, correlation IDs, or stack traces.

## 3. Required Inputs
- Defect description and steps to reproduce.
- Target service or module.

## 4. Execution Steps
1. **Analyze Incident & Logs**:
   - Trace request lifecycle using correlation ID (`X-Correlation-ID`) across Gateway, service logs, and Kafka.
2. **Reproduce via Failing Test (Red Stage)**:
   - Write a unit or integration test reproducing the exact failure conditions.
   - Run test to confirm it fails with the expected error.
3. **Root-Cause Analysis**:
   - Identify the exact line or logic error causing the issue.
4. **Implement Minimal Fix (Green Stage)**:
   - Apply the fix in the affected service.
   - Re-run the reproduction test and confirm it passes.
5. **Run Full Regression Suite**:
   - Run entire test suite (`mvn test`) to ensure no collateral regression.
6. **Commit & Document**:
   - Commit using Conventional Commits: `fix(<service>): <defect summary>`.

## 5. Skills to Use
- [`unit-testing-junit-mockito`](file:///d:/learning/task_manager/.agents/skills/unit-testing-junit-mockito/SKILL.md)
- [`code-review-refactoring`](file:///d:/learning/task_manager/.agents/skills/code-review-refactoring/SKILL.md)

## 6. Rules to Follow
- [Rule 08: Testing & TDD Standards](file:///d:/learning/task_manager/.agents/rules/08-testing-and-tdd.md)
- [Rule 11: Git & Code Review Standards](file:///d:/learning/task_manager/.agents/rules/11-git-and-code-review.md)

## 7. Tests and Validation
- Reproduction test must transition from Red $\rightarrow$ Green.
- Full module test suite must remain 100% green.

## 8. Failure Handling
- If the bug cannot be reproduced with a test, gather additional production inputs or request traces.

## 9. Review Checklist
- [ ] Failing test was created prior to applying the fix.
- [ ] No regression introduced in existing tests.
- [ ] Fix respects architectural rules (e.g. anti-enumeration, outbox).

## 10. Expected Outputs
- Defect reproduction test case.
- Production bug fix code.
- Clean regression test report.

