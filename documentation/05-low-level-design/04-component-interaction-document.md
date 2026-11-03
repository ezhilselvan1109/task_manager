# Component Interaction Document: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-LLD-004 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Software Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-LLD-001`, `DOC-LLD-003` |

---

## 1. Overview
This document specifies how internal Spring Boot components (Filters, Interceptors, Controllers, Services, Repositories, Transaction Managers, and Async Workers) interact during request and event execution lifecycles.

---

## 2. Inbound Request Execution Pipeline

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client App
    participant Netty as Tomcat / Netty Ingress
    participant Filter as UserContextFilter
    participant Ctrl as TaskController
    participant Tx as Spring PlatformTransactionManager
    participant Svc as TaskServiceImpl
    participant Repo as TaskRepository
    participant DB as PostgreSQL Database
    participant Advice as GlobalExceptionHandler

    Client->>Netty: HTTP POST /api/v1/tasks (X-User-Id, JSON Body)
    Netty->>Filter: doFilter(ServletRequest, ServletResponse)
    Filter->>Filter: Extract X-User-Id header & set UserContextHolder
    Filter->>Ctrl: invoke createTask(@Valid CreateTaskRequest)
    
    alt Validation Annotation Failure (@NotBlank title)
        Ctrl-->>Advice: MethodArgumentNotValidException
        Advice-->>Client: HTTP 400 Bad Request (Field Error JSON)
    else Validation Passes
        Ctrl->>Svc: createTask(request)
        Svc->>Tx: @Transactional begin()
        Tx->>DB: BEGIN TRANSACTION
        Svc->>Svc: Apply defaults (status=TODO, priority=MEDIUM)
        Svc->>Repo: save(taskEntity)
        Repo->>DB: INSERT INTO tasks (...)
        Svc->>Repo: save(outboxEventEntity)
        Repo->>DB: INSERT INTO outbox_events (...)
        Svc->>Tx: commit()
        Tx->>DB: COMMIT TRANSACTION
        Svc-->>Ctrl: TaskResponse Record
        Ctrl-->>Client: HTTP 201 Created (JSON Response)
    end
    
    Note over Filter: finally { UserContextHolder.clear(); }
```

---

## 3. Outbox Poller Execution & Lock Isolation
1. **Trigger**: Spring `@Scheduled(fixedDelayString = "${app.outbox.poll-interval:1000}")`.
2. **Isolation Level**: Read Committed with row-level locks.
3. **Locking Query**:
   ```sql
   SELECT * FROM outbox_events 
   WHERE processed = false 
   ORDER BY created_at ASC 
   LIMIT 50 
   FOR UPDATE SKIP LOCKED;
   ```
4. **Benefit of `SKIP LOCKED`**: If multiple application instances of `task-service` run concurrently, each poller skips rows currently locked by other instances, enabling lock-free horizontal scaling without duplicate Kafka publications.

---

## 4. Review Checklist
- [x] Inbound request lifecycle from Filter to Transaction Manager and Global Exception Advice mapped.
- [x] Context cleanup (`finally { UserContextHolder.clear(); }`) prevents thread pool context leakage.
- [x] Row-level locking concurrency mechanics with `SKIP LOCKED` documented.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Software Architect | Initial baseline Component Interaction document. |

