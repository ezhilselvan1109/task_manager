# ADR-001: Microservices Architecture and Database-per-Service Pattern

| Metadata Item | Details |
|---|---|
| **ADR ID** | ADR-001 |
| **Status** | Approved |
| **Date** | 2026-11-03 |
| **Deciders** | Solution Architect, Lead BA |
| **Related Documents** | `DOC-ARCH-001`, `DOC-ARCH-007` |

---

## 1. Context & Problem Statement
The Task Management System requires high availability, autonomous service scaling, independent deployment lifecycles, and strict multi-tenant data isolation. A traditional monolithic architecture with a single shared database creates tight domain coupling, cascading deployment failures, and risks cross-tenant data leakage.

## 2. Decision
Adopt a **Microservices Architecture** with a strict **Database-per-Service** pattern:
1. Decompose the system into autonomous services: `api-gateway`, `user-service`, `task-service`, `notification-service`, and `service-discovery`.
2. Each microservice manages its own dedicated database (`user_db`, `task_db`, `notification_db`) using isolated PostgreSQL database user credentials.
3. Cross-service database queries, shared tables, and cross-database foreign keys are strictly prohibited.
4. All inter-service dependencies flow via REST APIs or asynchronous Kafka events.

## 3. Consequences
- **Positive**:
  - Independent schema evolution: each service can run Flyway migrations without coordination or locking other services.
  - Fault isolation: database lock contention or slowdowns in notification storage cannot degrade core task creation.
  - Strict data isolation: cross-tenant data leaks via SQL joins are physically impossible.
- **Negative / Trade-offs**:
  - Distributed transaction complexity: requires eventual consistency and the Transactional Outbox pattern rather than two-phase commits.
  - Operational overhead: managing three logical databases instead of one (mitigated by running them in a unified PostgreSQL container for local development).

