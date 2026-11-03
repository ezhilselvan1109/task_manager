# ADR-006: Idempotent Consumer Design for Notification Service

| Metadata Item | Details |
|---|---|
| **ADR ID** | ADR-006 |
| **Status** | Approved |
| **Date** | 2026-11-03 |
| **Deciders** | Software Architect, QA Lead |
| **Related Documents** | `ADR-005`, `DOC-BA-006` |

---

## 1. Context & Problem Statement
Because the Transactional Outbox pattern in `task-service` operates with at-least-once delivery semantics, transient network timeouts or Kafka consumer rebalancing can deliver the identical task domain event multiple times to `notification-service`. Without deduplication, users would receive duplicate in-app notifications for a single task creation or completion.

## 2. Decision
Implement the **Idempotent Consumer Pattern** in `notification-service`:
1. Maintain a dedicated `consumed_events` table in `notification_db` with `event_id` (UUID) as the primary key.
2. When consuming an event from `task.events.v1`, the listener first checks if `event_id` exists in `consumed_events`.
3. If already present, the message is immediately acknowledged and discarded without side effects.
4. If absent, the listener inserts the notification record into `notifications` and the `event_id` into `consumed_events` within the *same* database transaction.

## 3. Consequences
- **Positive**: Complete deduplication protection; users never receive duplicate notifications.
- **Negative / Trade-offs**: Slight database write overhead (inserting one row into `consumed_events` per processed event); requires periodic archiving of old `consumed_events` records after 30 days.

