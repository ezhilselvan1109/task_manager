# ADR-002: Selection of Apache Kafka over RabbitMQ for Event Streaming

| Metadata Item | Details |
|---|---|
| **ADR ID** | ADR-002 |
| **Status** | Approved |
| **Date** | 2026-11-03 |
| **Deciders** | Solution Architect, DevOps Lead |
| **Related Documents** | `DOC-ARCH-001`, `DOC-ARCH-010` |

---

## 1. Context & Problem Statement
The Task Management System requires an asynchronous event distribution platform to disseminate task mutations (`TASK_CREATED`, `TASK_UPDATED`, `TASK_COMPLETED`, `TASK_DELETED`) to downstream consumers like `notification-service`. The architecture evaluated RabbitMQ (AMQP message queue) vs. Apache Kafka (distributed event log).

## 2. Decision
Select **Apache Kafka** (version 3.7+) as the exclusive asynchronous event streaming backbone. RabbitMQ is explicitly excluded.

## 3. Rationale
1. **Immutable Distributed Commit Log**: Kafka treats events as an append-only, durable log rather than transient queue messages that are deleted upon consumption.
2. **Replayability & Time-Travel Ingestion**: New or re-started services (or future analytics services) can replay historical events from offset zero, enabling retroactive event sourcing and notification backfilling.
3. **Partitioning & Deterministic Ordering**: By keying messages on `aggregateId` (Task UUID), Kafka guarantees that all lifecycle events for a specific task are processed in strict sequential order.
4. **Consumer Group Scalability**: Kafka’s consumer group model allows `notification-service` to scale horizontally without message contention or complex broker routing topologies.

## 4. Consequences
- **Positive**: High throughput, native partition ordering, replayable audit trail, zero message loss when combined with Transactional Outbox.
- **Negative / Trade-offs**: Slightly higher container memory footprint than RabbitMQ in local Docker Compose (managed by allocating 1GB RAM to Kafka and configuring single-broker mode).

