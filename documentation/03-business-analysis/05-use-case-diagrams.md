# Use Case Diagrams: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-BA-005 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Senior Business Analyst |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-REQ-007`, `DOC-BA-003` |

---

## 1. Overview
This document catalogs subsystem-specific Use Case diagrams, illustrating actors, system boundaries, and functional relationships across the microservices domain.

---

## 2. Subsystem Use Case Diagrams

### 2.1 User Service & Authentication Subsystem

```mermaid
flowchart LR
    Visitor((Anonymous Visitor))
    AuthUser((Authenticated User))

    subgraph UserServiceBoundary ["User Service Boundary (:8081)"]
        UC01["UC-001: Register Account"]
        UC02["UC-002: Authenticate (Login)"]
        UC03["UC-003: Refresh JWT Token"]
        UC04["UC-004: Retrieve Profile"]
        UC05["UC-005: Update Profile"]
        
        ValidateInput["<<include>> Validate Input Schema"]
        HashPassword["<<include>> BCrypt Salt & Hash"]
        IssueJWT["<<include>> Sign JWT Token"]
    end

    Visitor --> UC01
    Visitor --> UC02
    AuthUser --> UC03
    AuthUser --> UC04
    AuthUser --> UC05

    UC01 -.->|includes| ValidateInput
    UC01 -.->|includes| HashPassword
    UC02 -.->|includes| IssueJWT
    UC03 -.->|includes| IssueJWT
```

---

### 2.2 Task Service Subsystem

```mermaid
flowchart LR
    User((Authenticated User))
    PollerWorker((Outbox Poller Worker))

    subgraph TaskServiceBoundary ["Task Service Boundary (:8082)"]
        CreateTask["UC-006: Create Task"]
        ViewTask["UC-007: View Task Detail"]
        UpdateTask["UC-008: Update Task Details"]
        ChangeStatus["UC-009: Change Task Status"]
        ChangePriority["UC-010: Change Task Priority"]
        SoftDelete["UC-011: Soft Delete Task"]
        ListTasks["UC-012: Query, Filter & Sort Tasks"]
        ViewMetrics["UC-013: View Dashboard Metrics"]
        PublishOutbox["UC-014: Publish Outbox Events to Kafka"]

        EnforceTenant["<<include>> Enforce User Tenant Scope"]
        WriteOutbox["<<include>> Record Outbox Event"]
    end

    User --> CreateTask
    User --> ViewTask
    User --> UpdateTask
    User --> ChangeStatus
    User --> ChangePriority
    User --> SoftDelete
    User --> ListTasks
    User --> ViewMetrics
    PollerWorker --> PublishOutbox

    CreateTask -.->|includes| EnforceTenant
    CreateTask -.->|includes| WriteOutbox
    UpdateTask -.->|includes| EnforceTenant
    UpdateTask -.->|includes| WriteOutbox
    ChangeStatus -.->|includes| WriteOutbox
    SoftDelete -.->|includes| WriteOutbox
    ViewTask -.->|includes| EnforceTenant
    ListTasks -.->|includes| EnforceTenant
    ViewMetrics -.->|includes| EnforceTenant
```

---

### 2.3 Notification Service Subsystem

```mermaid
flowchart LR
    KafkaBroker((Apache Kafka Broker))
    User((Authenticated User))

    subgraph NotificationBoundary ["Notification Service Boundary (:8083)"]
        ConsumeEvent["UC-015: Consume Task Event"]
        Deduplicate["<<include>> Check Idempotency Table"]
        PersistNotif["<<include>> Insert In-App Notification"]
        
        ListNotif["UC-016: List User Notifications"]
        MarkRead["UC-017: Mark Notification as Read"]
        MarkAllRead["UC-018: Mark All Notifications as Read"]
    end

    KafkaBroker --> ConsumeEvent
    ConsumeEvent -.->|includes| Deduplicate
    ConsumeEvent -.->|includes| PersistNotif

    User --> ListNotif
    User --> MarkRead
    User --> MarkAllRead
```

---

## 3. Review Checklist
- [x] Clear subsystem boundary boxes defined for each microservice.
- [x] Actors (Human and System/Workers) mapped with distinct roles.
- [x] `<<include>>` and dependency associations explicitly diagrammed.

---

## 4. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Senior Business Analyst | Initial baseline Use Case Diagrams. |

