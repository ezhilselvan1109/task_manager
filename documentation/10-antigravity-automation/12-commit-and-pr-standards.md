# Commit and Pull Request Standards

**Document Identifier:** DOC-AUTO-012  
**Traceability References:** Master Project Baseline, DOC-AUTO-001, DOC-AUTO-011  
**Specification:** Conventional Commits 1.0.0  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document establishes the commit message syntax, semantic release tagging standards, and pull request (PR) template enforced across the Task Management System codebase. Standardized commit metadata enables automated changelog generation, clear auditability, and traceability to requirements.

---

## 2. Conventional Commits Specification

All commit messages must adhere to the Conventional Commits v1.0.0 format:

```
<type>(<scope>): <short description in imperative mood>

[optional body: detailed explanation of motivation and changes]

[optional footer(s): issue references, breaking changes, requirement IDs]
```

### 2.1 Commit Types Inventory

| Type | Intent & Usage | Example |
|---|---|---|
| `feat` | New user-facing or API feature | `feat(task): implement task creation with outbox event` |
| `fix` | Bug fix in production code | `fix(auth): return 401 instead of 500 on expired jwt` |
| `docs` | Documentation-only changes | `docs(security): complete STRIDE threat model document` |
| `test` | Adding or updating tests without altering production code | `test(user): add unit tests for registration validation` |
| `refactor` | Code restructuring with zero behavior change | `refactor(task): convert task dtos to java 21 records` |
| `perf` | Performance enhancement | `perf(task): add composite index for user_id and due_date` |
| `ci` | Modifications to Jenkinsfile or build automation | `ci(jenkins): configure jacoco code coverage quality gate` |
| `chore` | Dependency upgrades, build configs, tool setup | `chore(deps): upgrade spring boot to 3.3.4` |

### 2.2 Standard Scopes
- `gateway`, `user`, `task`, `notification`, `discovery`, `frontend`, `infra`, `security`, `docs`.

### 2.3 Commit Message Example
```git
feat(task): implement transactional outbox event emission

Persist TASK_CREATED event in outbox_events table within the same
local ACID transaction as the task entity insertion.

Closes #24
Traceability: US-004, FR-005, EVT-001, BRULE-009
```

---

## 3. Pull Request (PR) Template

Every PR opened on GitHub must utilize the following standardized template:

```markdown
## Summary
A concise description of the purpose of this PR and what was changed.

## Traceability & Requirements
- **User Story:** US-xxx
- **Functional Requirements:** FR-xxx, FR-yyy
- **Test Cases:** TC-xxx, TC-yyy

## Architectural Impact
- [ ] Database Schema Modified? (If yes, confirm Flyway script included)
- [ ] API Contract Modified? (If yes, confirm OpenAPI YAML updated)
- [ ] Domain Events Added / Modified? (If yes, confirm schema versioned)

## Testing Strategy & Verification
- Unit test coverage percentage: __%
- Testcontainers integration test results: PASS / FAIL
- Manual verification steps executed:

## Pre-Merge Quality Checklist
- [ ] Adheres to Definition of Done (DoD).
- [ ] Zero Checkstyle / SpotBugs violations.
- [ ] No plaintext secrets or hardcoded passwords.
- [ ] `requirements-traceability-matrix.md` updated.
```

