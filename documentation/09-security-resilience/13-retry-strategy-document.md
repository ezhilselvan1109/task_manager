# Retry Strategy Document

**Document Identifier:** DOC-RES-003  
**Traceability References:** NFR-002, NFR-006, EVT-010, DOC-RES-001  
**Target Services:** All Platform Components  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the retry policies, backoff mathematical models, and idempotency safeguards across synchronous HTTP communications, database transactions, and asynchronous messaging streams. Retries are restricted strictly to transient faults on idempotent operations to prevent duplicate mutations and thundering herd conditions.

---

## 2. Fundamental Retry Golden Rule

> [!CRITICAL]
> **Retries must only be executed on idempotent operations.**
> - Synchronous `POST` mutations (such as user creation or task creation) must **never** be retried blindly by intermediate network proxies without unique client request tokens, as this causes duplicate database entities.
> - Retries are reserved for idempotent HTTP verbs (`GET`, `PUT`, `DELETE`), database optimistic locking collisions, and idempotent Kafka event consumers.

---

## 3. Retry Domains and Policies

### 3.1 Database Optimistic Locking Retries (`task-service`)
When concurrent updates target the same task version, Hibernate throws `OptimisticLockException`. Spring Retry intercepts this exception and retries the transaction:

```java
@Retryable(
    retryFor = { ObjectOptimisticLockingFailureException.class, CannotAcquireLockException.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 50, multiplier = 2.0, random = true)
)
@Transactional
public TaskResponse updateTask(UUID id, UpdateTaskRequest request, UUID userId) {
    TaskEntity task = findTaskOrThrow(id, userId);
    taskMapper.updateEntityFromDto(request, task);
    return taskMapper.toResponseDto(taskRepository.save(task));
}
```

### 3.2 Outbox Poller Kafka Publish Retries
- If the Kafka broker is temporarily unreachable when `OutboxPoller` attempts to publish an event, `KafkaTemplate.send()` fails.
- **Handling**: The poller catches the exception, logs a warning, and skips updating `processed = true` for that batch.
- On the subsequent polling tick (500ms later), the poller re-queries `processed = FALSE` and attempts transmission again. No outbox records are lost.

### 3.3 Asynchronous Kafka Consumer Retries (`notification-service`)
- **Container**: Spring Kafka `DefaultErrorHandler`.
- **Policy**: 3 retry attempts with exponential backoff and jitter.
- **Backoff Formula**:
  $$\text{Delay}_n = \min(\text{InitialInterval} \times \text{Multiplier}^{(n-1)} \times (1 + \text{Jitter}), \text{MaxInterval})$$
  - Initial Interval: 1,000 ms
  - Multiplier: 2.0
  - Jitter: Uniform random $\pm 20\%$
  - Max Interval: 10,000 ms
- **Exhaustion**: After 3 failed attempts, the record is routed to `task.events.v1.DLT` via `DeadLetterPublishingRecoverer`.

---

## 4. Retry vs Fail-Fast Matrix

| Operation | Fault Type | Retry Strategy | Max Attempts | Backoff Profile |
|---|---|---|---|---|
| `POST /api/v1/tasks` | Network Timeout | **Fail Fast** (Return 504) | 0 | None (Client handles retry) |
| `GET /api/v1/tasks/{id}` | Read Timeout | **Gateway Retry** | 2 | 200 ms linear |
| JPA Save Task | Optimistic Lock | **Spring Retry** | 3 | 50ms, 100ms, 200ms (Jittered) |
| Outbox Poller Send | Broker Unreachable | **Scheduler Retry** | Unlimited | 500 ms scheduled polling |
| Kafka Event Consume | Transient DB Down | **Spring Kafka Retry** | 3 | 1s, 2s, 4s (Exponential + Jitter) |
| Kafka Event Consume | Deserialization Error | **Fail Fast $\rightarrow$ DLT** | 0 | Immediate route to DLT |

