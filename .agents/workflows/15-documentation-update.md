# Workflow 15: Documentation Update and Synchronization

## 1. Overview & Purpose
Defines the mandatory procedure for synchronizing documentation, updating the Requirements Traceability Matrix, and recording architectural decisions whenever changes occur.

- **Workflow ID**: `WF-015`
- **Trigger**: Schema changes, API alterations, architecture decisions, or implementation milestones.

## 2. Preconditions
- The underlying code, schema, or process modification has been implemented and tested.

## 3. Required Inputs
- Modified artifact paths.
- Linked requirement IDs (`BR`, `FR`, `NFR`, `ADR`).

## 4. Execution Steps
1. **Identify Impacted Documentation**:
   - Determine which phase documentation files are affected (e.g. database schema change impacts Phase 6 and Phase 12).
2. **Update Specific Design Documents**:
   - Edit target files in `documentation/` to accurately reflect the implementation.
   - Increment document version number and update revision history table.
3. **Synchronize Requirements Traceability Matrix**:
   - Update `documentation/requirements-traceability-matrix.md` with any new APIs, test cases, or component linkages.
4. **Author Architecture Decision Record (if applicable)**:
   - If an architectural shift occurred, author `documentation/architecture-decisions/ADR-XXX-<title>.md`.
5. **Update Master Index and Progress Tracker**:
   - Update `documentation/project-documentation-index.md`.
   - Update `documentation/project-progress.md`.

## 5. Skills to Use
- [`technical-documentation-maintenance`](file:///d:/learning/task_manager/.agents/skills/technical-documentation-maintenance/SKILL.md)

## 6. Rules to Follow
- [Rule 01: Architecture & Stack](file:///d:/learning/task_manager/.agents/rules/01-architecture-and-stack.md)
- [Rule 12: Security & Documentation Standards](file:///d:/learning/task_manager/.agents/rules/12-security-and-documentation.md)

## 7. Tests and Validation
- Verify all links in updated documentation resolve cleanly.
- Verify zero placeholder stubs remain.

## 8. Failure Handling
- If documentation diverges from code, code review gates will reject the change until documentation is aligned.

## 9. Review Checklist
- [ ] Document ID taxonomy preserved.
- [ ] Requirements Traceability Matrix is 100% synchronized.
- [ ] Index and Progress files updated.

## 10. Expected Outputs
- Synchronized technical documentation.
- Updated Master RTM and Progress tracker.

