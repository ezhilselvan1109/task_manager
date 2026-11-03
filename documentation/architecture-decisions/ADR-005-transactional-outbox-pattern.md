# ADR-005: Transactional Outbox Pattern for Resilient Event Publishing

| Metadata Item | Details |
|---|---|
| **ADR ID** | ADR-005 |
| **Status** | Approved |
| **Date** | 2026-11-03 |
| **Deciders** | Software Architect, Solution Architect |
| **Related Documents** | `DOC-ARCH-001`, `DOC-BA-006` |

---

## 1. Context & Problem Statement
In `task-service`, mutating task state (e.g. creating, updating, completing, or deleting a task) and publishing a corresponding event to Apache Kafka represents a classic dual-write distributed systems problem. If the database transaction commits but the network call to Kafka fails (or the JVM crashes), downstream services (like `notification-service`) never receive the event, resulting in permanent state divergence.

## 2. Decision
Implement the **Transactional Outbox Pattern** in `task-service`:
1. Add an `outbox_events` table to `task_db`.
2. When any task mutation occurs, insert an outbox event record within the *exact same* local PostgreSQL ACID transaction as the task entity change.
3. A background Spring `@Scheduled` worker (`OutboxPollerService`) polls unprocessed outbox records every 1000ms using row locking (`SELECT ... FOR UPDATE SKIP LOCKED`), publishes the message to Kafka topic `task.events.v1`, and marks the outbox row as `processed = true` upon receiving broker ACK.

## 3. Consequences
- **Positive**:
  - Zero event loss: If Kafka is down, outbox records safely accumulate in PostgreSQL without failing user transactions; the poller automatically drains the queue once Kafka recovers.
  - Zero dual-write inconsistency: Atomic DB commit guarantees an event is written if and only if the task change commits.
- **Negative / Trade-offs**:
  - At-least-once delivery: Network retries by the poller can result in duplicate events published to Kafka, requiring downstream consumers to be idempotent (addressed by ADR-006).

