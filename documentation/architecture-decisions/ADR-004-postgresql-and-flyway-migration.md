# ADR-004: PostgreSQL 16 Relational Engine and Flyway Migration

| Metadata Item | Details |
|---|---|
| **ADR ID** | ADR-004 |
| **Status** | Approved |
| **Date** | 2026-11-03 |
| **Deciders** | Database Architect, Software Architect |
| **Related Documents** | `DOC-ARCH-001`, `DOC-BA-008` |

---

## 1. Context & Problem Statement
The Task Management System requires ACID-compliant relational persistence, native UUID support, JSON document handling for outbox event payloads, and automated, repeatable schema evolution across environments without manual DBA intervention.

## 2. Decision
Standardize on **PostgreSQL 16** as the sole relational database engine, managed deterministically via **Flyway Database Migration**:
1. All three microservices (`user-service`, `task-service`, `notification-service`) target PostgreSQL 16.
2. Each microservice maintains its own Flyway migration scripts under `src/main/resources/db/migration` using semantic version naming (`V1__initial_schema.sql`, `V2__...sql`).
3. Hibernate `ddl-auto` is strictly set to `validate` (never `update` or `create-drop`), ensuring that Flyway is the single source of truth for DDL changes.

## 3. Consequences
- **Positive**:
  - Deterministic schemas: migrations run automatically on service startup, preventing code-schema drift.
  - Native performance: PostgreSQL native UUID types, B-Tree indexes, and `JSONB` support provide optimal query speed and storage efficiency.
  - Zero licensing costs: 100% open-source engine with standard Alpine container distribution.
- **Negative / Trade-offs**:
  - Schema changes must be written as immutable forward-only SQL migration scripts; requires strict pre-commit review.

