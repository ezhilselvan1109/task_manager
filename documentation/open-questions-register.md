# Open Questions Register

| Field | Value |
|---|---|
| **Document ID** | REG-OQ-001 |
| **Project** | Task Management System – Microservices Architecture |
| **Current Status** | Active / Maintained Across Phases |
| **Last Updated** | 2026-11-03 |

---

## 1. Overview
The Open Questions Register tracks ambiguities, business policy decisions, and technical architectural options that require formal resolution or validation during the Software Development Life Cycle (SDLC). No assumption is silently promoted to a confirmed requirement without explicit tracking.

---

## 2. Question Classification
- **Critical (P1)**: Blocker for core architecture or compliance; must be resolved before Phase 5/6.
- **Important (P2)**: Affects API contract or UI workflow; must be resolved before Phase 7.
- **Moderate (P3)**: Tuning, operational thresholds, or future enhancements; can proceed with standard defaults.

---

## 3. Register of Open Questions

| ID | Title & Description | Why It Matters | Related Requirements | Affected Documents | Suggested Options | Status | Decision / Target Phase |
|---|---|---|---|---|---|---|---|
| **OQ-001** | **Overdue Task Definition & Trigger**<br>Should a task automatically transition to an `OVERDUE` status, or remains in its current status (e.g. `TODO`, `IN_PROGRESS`) with a calculated runtime flag `isOverdue = (dueDate < now && status != COMPLETED && status != CANCELLED)`? | Storing an `OVERDUE` status requires a scheduled batch job or cron worker mutating database states across all users every minute/hour, causing lock contention. A dynamic flag avoids heavy background batch writes. | BR-004, FR-012, BRULE-007 | BRD, SRS, Table Design, API Spec | **Option A (Recommended)**: Dynamically evaluate `isOverdue` on read queries and dashboard aggregation without altering state enum.<br>**Option B**: Introduce dedicated status `OVERDUE` driven by Spring Scheduled cron job. | Under Review (Option A adopted for MVP) | Phase 2 / Phase 6 |
| **OQ-002** | **User Account Deletion Policy & Task Lifecycle**<br>When a user account is deleted or deactivated, should all associated tasks and notifications be hard deleted (cascade), soft deleted (`deleted_at`), or anonymized? | Impacts GDPR/privacy compliance, referential integrity across decoupled service databases, and event-driven cleanup over Kafka. | BR-001, FR-005, SEC-006 | SRS, Database Design, Kafka Event Design | **Option A (Recommended for MVP)**: Soft deletion in User Service (`status = DEACTIVATED`); publish `USER_DEACTIVATED` event; Task and Notification services mark resources archived/inaccessible.<br>**Option B**: Hard cascade delete via distributed Kafka event saga.<br>**Option C**: Account deletion deferred to Post-MVP. | Under Review (Option C for MVP baseline, Option A planned for v1.1) | Phase 2 / Phase 8 |
| **OQ-003** | **Notification Channel Scope for MVP**<br>Should external notification dispatch (Email via SMTP/SendGrid, WebPush) be in MVP scope or strictly confined to in-app notification queries? | Adding external channels requires email template engines, external SMTP credentials, spam mitigation, and network egress policies. | BR-005, FR-018, NFR-008 | Scope Document, Notification Service LLD | **Option A (Confirmed MVP)**: In-app polling/REST notification retrieval only.<br>**Option B**: Add asynchronous SMTP email notifications in MVP.<br>**Option C**: Add WebSocket/SSE for real-time in-app push. | Confirmed (Option A for MVP; SSE/Email deferred to Post-MVP) | Phase 1 / Phase 2 |
| **OQ-004** | **JWT Revocation & Session Invalidation Strategy**<br>How should user logout or immediate token revocation be handled in a stateless JWT architecture with Spring Cloud Gateway? | Stateless JWTs remain valid until expiration timestamp (`exp`). Immediate revocation requires either a distributed token blacklist (Redis/DB) or short-lived access tokens (e.g., 15 mins) with refresh tokens. | BR-002, FR-003, SEC-002 | Security Architecture, Gateway LLD | **Option A (Recommended for MVP)**: Short-lived Access Token (15 mins) with Refresh Token rotation persisted in User Service DB; client clears token on logout.<br>**Option B**: Gateway Redis-backed blacklist check on every incoming request.<br>**Option C**: Long-lived stateless JWT without server-side revocation. | Resolved (Option A adopted to maintain zero Redis dependency in MVP) | Phase 4 / Phase 9 |
| **OQ-005** | **Task Deletion: Hard Delete vs Soft Delete**<br>Should task deletion (`DELETE /api/v1/tasks/{id}`) physically remove rows or update a `deleted_at` timestamp? | Affects data recovery, event publishing semantics (`TASK_DELETED`), and aggregate statistics. | FR-010, BRULE-005 | Table Design, Event Design | **Option A (Recommended)**: Soft delete (`is_deleted = true`, `deleted_at = TIMESTAMP`) with database filter.<br>**Option B**: Hard physical delete from `tasks` table. | Resolved (Option A adopted for data recovery and auditability) | Phase 2 / Phase 6 |
| **OQ-006** | **Transactional Outbox Implementation Mechanism**<br>Should the Transactional Outbox pattern in Task Service be polled via a Spring `@Scheduled` background worker or via Debezium CDC? | Debezium requires Kafka Connect and Postgres logical replication configuration; Spring Scheduled Outbox poller requires zero additional infrastructure containers. | NFR-005, EVT-006 | High-Level Architecture, Kafka Design | **Option A (Recommended for MVP)**: Database outbox table polled by Spring `@Scheduled` worker with `SELECT ... FOR UPDATE SKIP LOCKED`.<br>**Option B**: Full Debezium CDC container. | Resolved (Option A selected for infrastructure simplicity) | Phase 4 / Phase 8 |

---

## 4. Resolution Protocol
When an open question is resolved:
1. Update `Status` to `Resolved`.
2. Document the formal decision in `Decision / Target Phase`.
3. Create or link an Architecture Decision Record (ADR) if it impacts architecture or schema.
4. Propagate the outcome to the Requirements Traceability Matrix and respective phase documents.

