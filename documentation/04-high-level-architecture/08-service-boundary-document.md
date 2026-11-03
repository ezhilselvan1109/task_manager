# Service Boundary Document: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-ARCH-008 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Solution Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-ARCH-002`, `DOC-ARCH-007` |

---

## 1. Domain-Driven Design (DDD) Context Mapping
The Task Management System defines three core domain bounded contexts and maps their relationships using DDD strategic integration patterns:

```mermaid
flowchart LR
    subgraph IdentityContext ["Identity & Security Bounded Context (User Service)"]
        UserAggregate["User Aggregate\n• Credentials & Password Hash\n• JWT Token Generation"]
    end

    subgraph TaskOperationsContext ["Task Operations Bounded Context (Task Service)"]
        TaskAggregate["Task Aggregate\n• Task Entity & Status Machine\n• Transactional Outbox"]
    end

    subgraph NotificationContext ["Notification Bounded Context (Notification Service)"]
        NotificationAggregate["Notification Aggregate\n• In-App Notice\n• Idempotent Consumer"]
    end

    IdentityContext -->|Downstream Header: X-User-Id\n[Upstream / Downstream Customer-Supplier]| TaskOperationsContext
    TaskOperationsContext -->|Asynchronous Event Stream: task.events.v1\n[Published Language / Event-Driven]| NotificationContext
```

---

## 2. Ubiquitous Language by Bounded Context

| Bounded Context | Ubiquitous Term | Context-Specific Meaning | Canonical Representation |
|---|---|---|---|
| **Identity Context** | `User` | The security subject with credentials, role, and active status. | `User(id, email, passwordHash, status)` |
| **Task Operations Context** | `User` (Foreign Reference) | An opaque UUID identifier (`userId`) acting as a multi-tenant scoping boundary. No profile data is stored. | `UUID userId` |
| **Task Operations Context** | `Task` | An actionable item with lifecycle states (`TODO`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`). | `Task(id, userId, title, status, priority)` |
| **Notification Context** | `Recipient` | The destination user UUID (`userId`) who receives in-app notifications. | `UUID userId` |
| **Notification Context** | `Notification` | A message derived from an event projection explaining what changed. | `Notification(id, userId, message, read)` |

---

## 3. Data Boundary Isolation Guarantees
1. **Schema Partitioning**: Each service possesses a unique database user credential with permissions restricted to its database schema (`tms_user_usr`, `tms_task_usr`, `tms_notif_usr`).
2. **Zero Cross-Service SQL Joins**: Any requirement combining task data with user profiles (e.g., displaying "Created by Jane") is resolved by passing the user name in the JWT payload or having the client fetch profile metadata via `GET /users/me`.
3. **Eventual Consistency**: State synchronization across service boundaries is strictly asynchronous and eventually consistent. Downstream notification records reflect upstream task state changes within $< 500\text{ ms}$ under normal Kafka broker operation.

---

## 4. Review Checklist
- [x] Strategic DDD context map rendered.
- [x] Ubiquitous language differences across contexts clarified.
- [x] Technical enforcement of data isolation and eventual consistency verified.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Solution Architect | Initial baseline Service Boundary Document. |

