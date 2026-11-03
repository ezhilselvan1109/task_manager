# Automated Code Review Standards

**Document Identifier:** DOC-AUTO-006  
**Traceability References:** Master Project Goal, DOC-AUTO-001, DOC-AUTO-002  
**Target Services:** All Platform Source Code  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the automated and peer code review standards for the Task Management System. Pull requests are evaluated through automated static analysis, security scanners, architectural conformance checks, and human peer reviews before merge approval.

---

## 2. Automated Static Analysis Quality Gates

| Tool | Focus Area | Quality Gate Rule | Pipeline Action on Failure |
|---|---|---|---|
| **Checkstyle** | Code formatting & style | Google Java Style guide (4-space indent, import ordering). | Fails Maven build (`checkstyle:check`). |
| **SpotBugs** | Static bug patterns | Zero High-priority or Medium-priority bugs. | Fails Maven build (`spotbugs:check`). |
| **SonarQube / SonarLint** | Code smells, complexity | Cognitive complexity $\le 15$ per method; zero duplicated code blocks. | PR Quality Gate blocks merge. |
| **ESLint / Prettier** | React / TypeScript | `@typescript-eslint/strict`; zero unhandled `any` types. | Fails frontend build (`npm run lint`). |
| **Gitleaks / TruffleHog** | Secret detection | Zero credentials, private keys, or high-entropy tokens. | Immediate CI rejection. |

---

## 3. Mandatory Architectural Conformance Checks

During every code review, reviewers (automated and human) must verify:

### 3.1 Security & Multi-Tenant Isolation
- [ ] **Tenant Scoping**: Does every task query enforce `user_id = :authenticatedUserId`?
- [ ] **Anti-Enumeration**: Does missing resource ownership throw `ResourceNotFoundException` resulting in `HTTP 404` (never `403`)?
- [ ] **No SQL Injection**: Are all queries parameterized using Spring Data method names or JPA Criteria API?
- [ ] **Password Scrubbing**: Are passwords excluded from log statements, toString methods, and response DTOs?

### 3.2 Transactional & Outbox Conformance
- [ ] **Outbox Dual-Write Rule**: Are Kafka events written to the local PostgreSQL `outbox_events` table inside the `@Transactional` boundary rather than published directly to Kafka?
- [ ] **Minimal Transaction Scope**: Is `@Transactional` confined strictly to repository operations, keeping external network calls outside transactions?
- [ ] **Locking Strategy**: Do concurrent modifications use optimistic locking (`@Version`) with retry handlers?

### 3.3 Clean Code & Idiomatic Java 21
- [ ] **Record DTOs**: Are request and response DTOs implemented as immutable Java 21 `record` types?
- [ ] **Exception Handling**: Do controllers delegate all exception formatting to `@RestControllerAdvice` emitting RFC 7807 `ProblemDetail`?
- [ ] **Dependency Injection**: Is constructor-based injection (via `@RequiredArgsConstructor` or explicit constructor) used exclusively over field injection (`@Autowired`)?

---

## 4. Pull Request Review Checklist

```markdown
### PR Quality Verification
- [ ] Builds cleanly: `mvn clean verify` / `npm run build` passes with zero errors.
- [ ] Unit & slice tests pass with >= 80% branch coverage.
- [ ] Integration tests pass against Testcontainers (PostgreSQL + Kafka).
- [ ] OpenAPI documentation is updated to match any controller modifications.
- [ ] Flyway migration scripts are idempotent, numbered sequentially, and validated against checksums.
- [ ] Requirement Traceability Matrix (`requirements-traceability-matrix.md`) is synchronized.
```

