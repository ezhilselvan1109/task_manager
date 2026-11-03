# Workflow 10: Code Review Workflow

## 1. Overview & Purpose
Defines the peer review and quality gate procedure applied to every pull request or proposed changeset.

- **Workflow ID**: `WF-010`
- **Trigger**: Pull request submission or pre-merge evaluation.

## 2. Preconditions
- Feature branch pushed with passing CI pipeline.
- Clean Git diff against target branch (`develop` or `main`).

## 3. Required Inputs
- Pull request URL or git diff.
- Linked requirement or user story ID.

## 4. Execution Steps
1. **Rule Compliance Audit**:
   - Verify compliance against Rules 01 through 12.
   - Scan for forbidden placeholders (`TODO`, `FIXME`, stubs).
2. **Architecture & Security Check**:
   - Verify database-per-service isolation (no cross-service joins).
   - Verify anti-enumeration returns `404 Not Found`.
   - Verify Kafka events use Transactional Outbox.
   - Verify zero plaintext secrets.
3. **Test & Quality Inspection**:
   - Verify unit tests are present and comprehensive ($\ge 80\%$ coverage).
   - Verify Checkstyle/SpotBugs report zero violations.
4. **Formulate Review Feedback**:
   - Categorize comments into: Blockers (Must fix), Major (Should fix), Minor (Suggestions).
5. **Approve or Request Changes**:
   - Approve only when zero blockers and zero major violations remain.

## 5. Skills to Use
- [`code-review-refactoring`](file:///d:/learning/task_manager/.agents/skills/code-review-refactoring/SKILL.md)
- [`technical-documentation-maintenance`](file:///d:/learning/task_manager/.agents/skills/technical-documentation-maintenance/SKILL.md)

## 6. Rules to Follow
- [Rule 01: Architecture & Stack](file:///d:/learning/task_manager/.agents/rules/01-architecture-and-stack.md)
- [Rule 02: Java 21 & Spring Boot](file:///d:/learning/task_manager/.agents/rules/02-java21-and-spring-boot.md)
- [Rule 11: Git & Code Review](file:///d:/learning/task_manager/.agents/rules/11-git-and-code-review.md)

## 7. Tests and Validation
- CI automated checks green.
- PR template checklist completed.

## 8. Failure Handling
- If violations are discovered, block merge and detail required remediation.

## 9. Review Checklist
- [ ] Conforms to approved architecture.
- [ ] No stubs or placeholders.
- [ ] Tests accompany the change.
- [ ] Documentation updated if contracts changed.

## 10. Expected Outputs
- Completed Code Review Assessment report.
- Approval or actionable change requests.

