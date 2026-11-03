# Low-Level Sequence Diagrams: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-LLD-005 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Software Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-LLD-003`, `DOC-LLD-004` |

---

## 1. Overview
This document provides low-level, method-signature accurate sequence diagrams detailing method invocations, parameter passing, and return types across the microservices.

---

## 2. Sequence Diagrams

### 2.1 User Registration (`user-service`)

```mermaid
sequenceDiagram
    autonumber
    participant Ctrl as AuthController
    participant Svc as AuthServiceImpl
    participant UserRepo as UserRepository
    participant Encrypt as PasswordEncoder
    participant DB as PostgreSQL (user_db)

    Ctrl->>Svc: register(RegisterRequest(email, password, fullName))
    Svc->>UserRepo: existsByEmail(email)
    UserRepo->>DB: SELECT COUNT(1) FROM users WHERE email = ?
    DB-->>UserRepo: 0
    UserRepo-->>Svc: false
    
    Svc->>Encrypt: encode(rawPassword)
    Encrypt-->>Svc: "$2a$12$e8YQJ1..." (BCrypt hash)
    
    Svc->>UserRepo: save(new UserEntity(UUID.randomUUID(), email, hash, fullName, ACTIVE))
    UserRepo->>DB: INSERT INTO users VALUES (...)
    DB-->>UserRepo: 1 row
    UserRepo-->>Svc: UserEntity
    
    Svc-->>Ctrl: AuthResponse(id, email, fullName, createdAt)
```

---

### 2.2 Task Creation with Transactional Outbox (`task-service`)

```mermaid
sequenceDiagram
    autonumber
    participant Ctrl as TaskController
    participant Svc as TaskServiceImpl
    participant Mapper as TaskMapper
    participant TaskRepo as TaskRepository
    participant OutboxRepo as OutboxEventRepository
    participant DB as PostgreSQL (task_db)

    Ctrl->>Svc: createTask(CreateTaskRequest(title, description, priority, dueDate))
    Note over Svc: Retrieves userId from UserContextHolder.getUserId()
    Svc->>Mapper: toEntity(request, userId)
    Mapper-->>Svc: TaskEntity(status=TODO, priority=MEDIUM, isDeleted=false)
    
    rect rgb(240, 248, 255)
        Note over Svc, DB: @Transactional Method Boundary
        Svc->>TaskRepo: save(taskEntity)
        TaskRepo->>DB: INSERT INTO tasks VALUES (...)
        DB-->>TaskRepo: TaskEntity persisted
        
        Svc->>Svc: buildEventEnvelope(TASK_CREATED, taskEntity)
        Svc->>OutboxRepo: save(OutboxEventEntity(aggregateId, TASK_CREATED, jsonPayload))
        OutboxRepo->>DB: INSERT INTO outbox_events VALUES (...)
        DB-->>OutboxRepo: OutboxEventEntity persisted
    end
    
    Svc->>Mapper: toResponse(taskEntity)
    Mapper-->>Svc: TaskResponse record
    Svc-->>Ctrl: TaskResponse
```

---

### 2.3 Dashboard Metrics Aggregation (`task-service`)

```mermaid
sequenceDiagram
    autonumber
    participant Ctrl as TaskMetricsController
    participant Svc as TaskMetricsServiceImpl
    participant TaskRepo as TaskRepository
    participant DB as PostgreSQL (task_db)

    Ctrl->>Svc: getMetricsSummary()
    Note over Svc: Retrieves current userId from UserContextHolder
    
    Svc->>TaskRepo: countStatusBreakdownByUserId(userId)
    TaskRepo->>DB: SELECT status, COUNT(*) FROM tasks WHERE user_id = ? AND is_deleted = false GROUP BY status
    DB-->>TaskRepo: List~StatusCountProjection~ (TODO=5, IN_PROGRESS=2, COMPLETED=8, CANCELLED=1)
    
    Svc->>TaskRepo: countHighPriorityTasks(userId)
    TaskRepo->>DB: SELECT COUNT(*) FROM tasks WHERE user_id = ? AND priority IN ('HIGH', 'URGENT') AND status NOT IN ('COMPLETED', 'CANCELLED') AND is_deleted = false
    DB-->>TaskRepo: 3
    
    Svc->>TaskRepo: countOverdueTasks(userId, Instant.now())
    TaskRepo->>DB: SELECT COUNT(*) FROM tasks WHERE user_id = ? AND due_date < ? AND status NOT IN ('COMPLETED', 'CANCELLED') AND is_deleted = false
    DB-->>TaskRepo: 2
    
    Svc->>Svc: assemble TaskMetricsResponse(total=16, todo=5, inProgress=2, completed=8, cancelled=1, highPriority=3, overdue=2)
    Svc-->>Ctrl: TaskMetricsResponse record
```

---

## 3. Review Checklist
- [x] Precise method names, arguments, and return types documented.
- [x] `@Transactional` boundaries demarcated in Mermaid rect regions.
- [x] Multi-tenancy scoping and SQL parameterization illustrated.

---

## 4. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Software Architect | Initial baseline Low-Level Sequence Diagrams. |

