# Entity Relationship Diagrams: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-DB-003 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Database Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-DB-001`, `DOC-DB-002`, `DOC-BA-008` |

---

## 1. Subsystem Entity-Relationship Diagrams

### 1.1 `user_db` ERD (User Service)

```mermaid
erDiagram
    users {
        uuid id PK "Canonical UUID v4"
        varchar email UK "Unique RFC 5322 Email"
        varchar password_hash "BCrypt Salted Hash (cost 12)"
        varchar full_name "User Display Name (1..100)"
        varchar status "ACTIVE, SUSPENDED, DEACTIVATED"
        timestamptz created_at "UTC Timestamp"
        timestamptz updated_at "UTC Timestamp"
    }

    refresh_tokens {
        uuid id PK "Canonical UUID v4"
        uuid user_id FK "References users(id)"
        varchar token UK "Cryptographically random token"
        timestamptz expires_at "Token Expiration UTC"
        boolean revoked "Revocation flag"
        timestamptz created_at "UTC Timestamp"
    }

    users ||--o{ refresh_tokens : "owns"
```

---

### 1.2 `task_db` ERD (Task Service)

```mermaid
erDiagram
    tasks {
        uuid id PK "Canonical UUID v4"
        uuid user_id "Owner User UUID (Multi-tenant partition key)"
        varchar title "Task Title (1..255 chars, not blank)"
        text description "Detailed notes (max 2000 chars)"
        varchar status "TODO, IN_PROGRESS, COMPLETED, CANCELLED"
        varchar priority "LOW, MEDIUM, HIGH, URGENT"
        timestamptz due_date "Optional UTC target deadline"
        boolean is_deleted "Soft delete flag"
        timestamptz deleted_at "Soft delete timestamp"
        bigint version "Optimistic locking counter"
        timestamptz created_at "UTC Creation Timestamp"
        timestamptz updated_at "UTC Modification Timestamp"
    }

    outbox_events {
        uuid id PK "Unique Event UUID"
        varchar aggregate_id "Task UUID (Kafka Partition Key)"
        varchar event_type "TASK_CREATED, TASK_UPDATED, etc."
        text payload "Serialized JSON Event Envelope"
        boolean processed "Publication status flag"
        timestamptz created_at "Creation UTC Timestamp"
        timestamptz processed_at "Broker ACK UTC Timestamp"
    }
```

---

### 1.3 `notification_db` ERD (Notification Service)

```mermaid
erDiagram
    notifications {
        uuid id PK "Canonical UUID v4"
        uuid user_id "Recipient User UUID"
        varchar event_type "Triggering Domain Event Type"
        varchar message "Human readable notification text (1..500)"
        boolean read "Read status flag"
        timestamptz read_at "Read UTC Timestamp"
        timestamptz created_at "UTC Timestamp"
    }

    consumed_events {
        uuid event_id PK "Kafka Message Event UUID"
        timestamptz consumed_at "Deduplication UTC Timestamp"
    }
```

---

## 2. Global Cross-Context Logical Data Map

```mermaid
flowchart LR
    subgraph user_db ["user_db"]
        u["users (id = PK)"]
    end

    subgraph task_db ["task_db"]
        t["tasks (user_id = UUID)"]
        o["outbox_events (aggregate_id = task.id)"]
    end

    subgraph notification_db ["notification_db"]
        n["notifications (user_id = UUID)"]
        c["consumed_events (event_id = outbox.id)"]
    end

    u -.->|"Logical Reference (No DB FK)"| t
    u -.->|"Logical Reference (No DB FK)"| n
    o -.->|"Asynchronous Event Projection via Kafka"| c
```

---

## 3. Review Checklist
- [x] Standard Mermaid `erDiagram` syntax used for all three service databases.
- [x] Primary keys (PK), foreign keys (FK), unique constraints (UK), and data types specified.
- [x] Global logical map clearly illustrates decoupled asynchronous relationships.

---

## 4. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Database Architect | Initial baseline Entity Relationship Diagrams. |

