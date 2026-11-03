# Topic Catalogue: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-EVT-003 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Solution Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-EVT-002`, `DOC-EVT-004` |

---

## 1. Topic Naming Conventions
Kafka topics adhere to the canonical enterprise naming standard:
```
<bounded-context>.<entity>.<major-version>[.<qualifier>]
```
- `<bounded-context>`: Business domain (e.g., `task`).
- `<entity>`: Aggregate name (e.g., `events`).
- `<major-version>`: Interface schema version (e.g., `v1`).
- `[qualifier]`: Optional channel suffix (e.g., `DLT` for Dead Letter Topic).

---

## 2. Master Topic Catalogue Table

| Topic Name | Purpose | Partitions | Partition Key | Serialization Format | Retention | Producer | Consumer Group |
|---|---|:---:|---|---|---|---|---|
| **`task.events.v1`** | Primary stream for all task lifecycle domain events (`CREATED`, `UPDATED`, `COMPLETED`, `DELETED`). | 3 | `aggregateId` (Task UUID) | JSON (UTF-8) | 7 days (604,800,000 ms) | `task-service` (Outbox Poller) | `notification-service-group` (`notification-service`) |
| **`task.events.v1.DLT`** | Dead Letter Topic storing unprocessable or poison pill messages after retry exhaustion. | 3 | `aggregateId` (Task UUID) | JSON (UTF-8) | 14 days (1,209,600,000 ms) | `notification-service` (Error Handler) | Operational Forensic Tooling / Manual Replay Worker |

---

## 3. Partitioning & Ordering Rationale
- **3 Partitions**: Selected to provide balanced parallelism for up to 3 consumer instances in a consumer group while keeping broker file descriptors and memory overhead low in local Docker Compose.
- **Partition Key**: Keying strictly on `aggregateId` guarantees that all events for a specific task (e.g., `TASK_CREATED` followed by `TASK_COMPLETED`) hash to the **exact same partition**, guaranteeing strict FIFO ordering per task entity.

---

## 4. Review Checklist
- [x] Canonical enterprise naming conventions standardized.
- [x] Complete topic specifications, retention periods, and producer/consumer mappings cataloged.
- [x] Partition key strategy guarantees per-task message ordering.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Solution Architect | Initial baseline Topic Catalogue. |

