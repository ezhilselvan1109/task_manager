# Event Catalogue: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-EVT-004 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Software Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-EVT-003`, `DOC-EVT-005` |

---

## 1. Overview
The Event Catalogue defines the four core domain events published to topic `task.events.v1`, detailing the business triggers, payload semantics, and downstream consumer side effects.

---

## 2. Master Event Catalogue

| Event Type | Event Version | Triggering Operation | Producing Service | Consuming Service | Downstream Action / Side Effect |
|---|:---:|---|---|---|---|
| **`TASK_CREATED`** | `1.0` | `POST /api/v1/tasks` (Task created with status `TODO`). | `task-service` | `notification-service` | Generates in-app notification: *"Task '{title}' was created"*. |
| **`TASK_UPDATED`** | `1.0` | `PUT /api/v1/tasks/{id}` or `PATCH /tasks/{id}/priority` (Attributes or priority modified). | `task-service` | `notification-service` | Generates in-app notification: *"Task '{title}' was updated"*. |
| **`TASK_COMPLETED`**| `1.0` | `PATCH /api/v1/tasks/{id}/status` with `status = COMPLETED`. | `task-service` | `notification-service` | Generates celebratory in-app notification: *"Task '{title}' was marked COMPLETED"*. |
| **`TASK_DELETED`** | `1.0` | `DELETE /api/v1/tasks/{id}` (Soft deletion). | `task-service` | `notification-service` | Generates in-app notification: *"Task '{title}' was deleted"*. |

---

## 3. Event Envelope Architecture
Every event shares an identical standard top-level metadata envelope conforming to CloudEvents principles:
- `eventId`: UUID v4 unique identifier of the message.
- `eventType`: The discriminator string (`TASK_CREATED`, `TASK_UPDATED`, etc.).
- `eventVersion`: Schema version (e.g., `"1.0"`).
- `aggregateId`: Task UUID (used as Kafka partition routing key).
- `userId`: Tenant UUID for recipient routing.
- `producer`: Originating microservice (`"task-service"`).
- `occurredAt`: UTC ISO-8601 timestamp.
- `correlationId`: Distributed trace ID propagated from incoming HTTP request.
- `payload`: Domain-specific event details.

---

## 4. Review Checklist
- [x] All 4 task lifecycle events documented with exact trigger operations.
- [x] CloudEvents-inspired envelope structure standardized.
- [x] Downstream notification messages and consumer actions defined.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Software Architect | Initial baseline Event Catalogue. |

