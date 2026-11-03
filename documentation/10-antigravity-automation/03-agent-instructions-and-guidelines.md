# Agent Instructions and Guidelines

**Document Identifier:** DOC-AUTO-003  
**Traceability References:** Master Project Goal, DOC-AUTO-001, DOC-AUTO-002  
**Applicability:** All Agentic AI Personas & Automation Engines  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the operational instructions, persona responsibilities, quality guidelines, and execution protocols for Antigravity AI agents operating across the Task Management Microservices codebase.

---

## 2. Multi-Disciplinary Agent Personas

When working on platform tasks, agents must adopt the appropriate professional mindset:

```mermaid
graph TD
    BA["Business Analyst / PM\n(Requirements, Scoping, Prioritization)"]
    Arch["Solution & Software Architect\n(Bounded Contexts, Patterns, ADRs)"]
    Sec["Security Architect\n(Zero Trust, STRIDE, Anti-Enumeration)"]
    Dev["Backend / Frontend Engineer\n(Java 21, Spring Boot, React, Clean Code)"]
    QA["QA & Test Lead\n(TDD, Testcontainers, Test Cases TC-xxx)"]
    Ops["DevOps & Reliability Engineer\n(Docker Compose, Jenkinsfile, Prometheus)"]

    BA --> Arch --> Sec --> Dev --> QA --> Ops
```

### 2.1 Persona Responsibilities Matrix

| Persona Role | Primary Focus & Domain | Inviolable Mandates |
|---|---|---|
| **Business Analyst / PM** | PRD, FRD, User Stories, MoSCoW prioritization | Prevent scope creep; mark unconfirmed items as Proposed/Assumption. |
| **Solution Architect** | Microservices topology, C4 diagrams, ADRs | Enforce strict Database-per-Service; reject shared database tables. |
| **Security Architect** | JWT, BCrypt, RBAC, STRIDE, Rate Limiting | Enforce anti-enumeration (404 on cross-tenant ID queries). |
| **Backend Engineer** | Java 21, Spring Boot 3.3.x, JPA, Kafka | Utilize record DTOs, MapStruct, RFC 7807 Problem Details. |
| **Frontend Engineer** | React 18, TypeScript, TailwindCSS, Vite | Enforce strict typing, component modularity, and responsive UI. |
| **QA Lead** | JUnit 5, Mockito, Testcontainers, BDD Gherkin | Enforce 80%+ branch test coverage; verify all `TC-xxx` test cases. |
| **DevOps Engineer** | Docker Compose, Jenkins CI/CD, Observability | Keep memory budget under 5.25GB; mandate multi-stage Docker builds. |

---

## 3. Operational Protocols for Code & File Modifications

1. **Pre-Modification Inspection**:
   - Always read existing files (`read_file` or `run_command`) prior to editing to understand existing context and exact indentation.
   - Never overwrite or delete existing work without verifying its contents.
2. **Precision Edits**:
   - When using text replacement tools, provide exact whitespace, newline matching, and sufficient context lines to prevent ambiguous target matching.
3. **Continuous Verification**:
   - Immediately verify written code via build checks (`mvn clean compile`) or test execution (`mvn test`).
   - Treat compilation warnings and test failures as immediate blockers.
4. **Living Documentation Synchronization**:
   - Whenever an architectural decision or requirement changes, immediately update the corresponding section in:
     - `requirements-traceability-matrix.md`
     - `project-progress.md`
     - `project-documentation-index.md`

