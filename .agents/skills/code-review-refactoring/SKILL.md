---
name: code-review-refactoring
description: >-
  Use this skill when performing systematic automated code reviews, refactoring legacy components,
  eliminating code smells, and enforcing clean code standards.
---

# Code Review and Refactoring Skill

## Purpose
Establishes a rigorous, automated protocol for auditing pull requests, identifying architectural violations or code smells, and safely refactoring existing code without regression.

## When to Use
- Reviewing pull requests or newly generated code before merging.
- Refactoring complex methods, eliminating duplication, or upgrading patterns.
- Auditing compliance against the 10 invariant engineering rules.

## Required Inputs
- Git diff or target source files.
- Associated requirement ID and test suite.

## Step-by-Step Procedure
1. **Audit Invariant Rules Compliance**:
   - Check for `// TODO`, `FIXME`, or stub code (**Reject if present**).
   - Check for cross-service database access or foreign keys (**Reject if present**).
   - Check for anti-enumeration compliance (does unauthorized ID access return `404`?).
   - Check for dual-write violations (are Kafka events emitted via Outbox?).
2. **Audit Code Quality & Clean Code**:
   - Class size $\le 400$ lines, method size $\le 30$ lines.
   - Cognitive complexity $\le 10$ per method.
   - Proper use of Java 21 features (records for DTOs, pattern matching).
3. **Execute Regression Testing**:
   - Run existing unit and integration test suite before refactoring.
   - Apply single-responsibility refactoring steps in isolation.
   - Re-run test suite after each modification to confirm zero regression.
4. **Generate Code Review Findings**:
   - Document any blockers, major issues, or minor suggestions using standard severity levels.

## Applicable Project Standards
- [Rule 02: Java 21 & Spring Boot Standards](file:///d:/learning/task_manager/.agents/rules/02-java21-and-spring-boot.md)
- [Rule 11: Git & Code Review Standards](file:///d:/learning/task_manager/.agents/rules/11-git-and-code-review.md)
- [Automated Code Review Standards](file:///d:/learning/task_manager/documentation/10-antigravity-automation/06-automated-code-review-standards.md)

## Validation Checklist
- [ ] Zero placeholders or stubs exist in modified files.
- [ ] Line coverage does not drop below 80%.
- [ ] Checkstyle and SpotBugs report zero errors.
- [ ] No regression in existing functionality.

## Common Mistakes
- Refactoring without a green test baseline to catch regressions.
- Allowing non-blocking warnings to accumulate into technical debt.

## Completion Criteria
All review checklist criteria pass; code is cleaner, adheres to standards, and passes all tests.

