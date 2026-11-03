# Event Failure Handling: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-EVT-015 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Software Architect & DevOps Lead |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-EVT-010`, `DOC-EVT-011`, `DOC-EVT-014` |

---

## 1. Overview
This document categorizes all potential failure modes in the asynchronous event pipeline, defining automated recovery paths, dead letter mechanisms, and operational escalation procedures.

---

## 2. Failure Modes & Automated Recovery Matrix

| Failure Mode | Root Cause | System Blast Radius | Automated Countermeasure & Recovery | Operational Alert Trigger |
|---|---|---|---|---|
| **FM-01: Broker Down During Production** | Kafka broker crashed or unreachable from `task-service`. | None for user. Task creation continues in PostgreSQL. | Outbox poller catches `KafkaException`; leaves `processed = false`. Drains automatically when broker restarts. | Prometheus alert if outbox queue depth $> 100$ for $> 5\text{ mins}$. |
| **FM-02: Poison Pill Payload** | Corrupted JSON or unexpected field format on `task.events.v1`. | Isolated to the single invalid message. | `ErrorHandlingDeserializer` intercepts error; forwards immediately to `task.events.v1.DLT` without retries. | Prometheus alert on `records_consumed_total{topic="task.events.v1.DLT"}`. |
| **FM-03: Consumer DB Unreachable** | PostgreSQL `notification_db` connection pool exhausted. | Notification generation paused for incoming events. | `DefaultErrorHandler` executes 3 exponential retries (1s, 2s). If DB remains down, record diverts to DLT. | Prometheus alert on consecutive consumer retry exhaustions. |
| **FM-04: Consumer Rebalance Mid-Batch** | Slow consumer thread causes consumer group coordinator to trigger rebalance. | Transient duplicate delivery of in-flight messages. | `CooperativeStickyAssignor` minimizes partition reassignment; `consumed_events` table ignores duplicate events. | Warning logged; offset commits cleanly on next poll. |
| **FM-05: Outbox Table Bloat** | Millions of processed rows accumulate in `outbox_events`. | Disk storage consumption in `task_db`. | Scheduled maintenance worker deletes rows where `processed = true AND processed_at < now() - 7 days`. | Disk utilization alert if table exceeds 5GB. |

---

## 3. Review Checklist
- [x] All 5 major messaging failure modes mapped with root cause and blast radius.
- [x] Automated recovery mechanisms protect against partition stalling and data loss.
- [x] Prometheus alert thresholds documented for operational monitoring.

---

## 4. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Software Architect | Initial baseline Event Failure Handling document. |

