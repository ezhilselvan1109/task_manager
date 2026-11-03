# Service Layer Design: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-LLD-006 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Software Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-LLD-001`, `DOC-LLD-003`, `DOC-REQ-009` |

---

## 1. Design Overview
The Service Layer encapsulates all business rules, multi-tenant checks, state machine transitions, and database transaction boundaries. All public service methods managing write operations are annotated with Spring's `@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)`. Read-only query methods use `@Transactional(readOnly = true)` to optimize Hibernate dirty-checking and JDBC connection usage.

---

## 2. Task Service Interfaces & Concrete Implementations

### 2.1 Interface: `TaskService`
```java
package com.chellaa.task_service.service;

import com.chellaa.task_service.dto.request.*;
import com.chellaa.task_service.dto.response.*;
import com.chellaa.task_service.entity.Priority;
import com.chellaa.task_service.entity.Status;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface TaskService {
    TaskResponse createTask(CreateTaskRequest request);
    TaskResponse getTaskById(UUID taskId);
    TaskResponse updateTask(UUID taskId, UpdateTaskRequest request);
    TaskResponse updateStatus(UUID taskId, Status newStatus);
    TaskResponse updatePriority(UUID taskId, Priority newPriority);
    void deleteTask(UUID taskId);
    PageResponse<TaskResponse> listTasks(TaskQueryCriteria criteria, Pageable pageable);
}
```

### 2.2 Core Business Implementation (`TaskServiceImpl`)
Key responsibilities executed within `@Transactional`:
1. **Multi-Tenancy Enforcement**: Resolves current `userId` via `UserContextHolder.getRequiredUserId()`. Queries filter by `id = taskId AND userId = currentUserId AND isDeleted = false`.
2. **State Machine Validation**: Validates transitions between `TODO`, `IN_PROGRESS`, `COMPLETED`, and `CANCELLED`.
3. **Atomic Outbox Event Generation**:
   ```java
   private void recordOutboxEvent(String eventType, TaskEntity task) {
       TaskEventEnvelope envelope = TaskEventEnvelope.builder()
           .eventId(UUID.randomUUID())
           .eventType(eventType)
           .eventVersion("1.0")
           .aggregateId(task.getId().toString())
           .userId(task.getUserId().toString())
           .occurredAt(Instant.now())
           .payload(TaskEventPayload.from(task))
           .build();

       OutboxEventEntity outbox = OutboxEventEntity.builder()
           .id(envelope.eventId())
           .aggregateId(envelope.aggregateId())
           .eventType(envelope.eventType())
           .payload(objectMapper.writeValueAsString(envelope))
           .processed(false)
           .createdAt(envelope.occurredAt())
           .build();

       outboxEventRepository.save(outbox);
   }
   ```

---

## 3. Analytics Service (`TaskMetricsServiceImpl`)
- **Interface**: `TaskMetricsService`
- **Method**: `TaskMetricsResponse getMetricsSummary();`
- **Logic**: Executes a single composite query or parallel aggregation counts:
  - Total active tasks (`isDeleted = false`).
  - Status counts (`TODO`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`).
  - High priority count (`priority IN ('HIGH', 'URGENT')` and not completed/cancelled).
  - Overdue count (`dueDate < Instant.now()` and not completed/cancelled).

---

## 4. Resilient Outbox Poller (`OutboxPollerService`)
```java
package com.chellaa.task_service.service.outbox;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPollerService {

    private final OutboxEventRepository outboxRepo;
    private final KafkaEventPublisher kafkaPublisher;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void pollAndPublishPendingEvents() {
        List<OutboxEventEntity> pendingBatch = outboxRepo.fetchPendingForPublishing(50);
        for (OutboxEventEntity event : pendingBatch) {
            try {
                kafkaPublisher.publish("task.events.v1", event.getAggregateId(), event.getPayload());
                event.setProcessed(true);
                event.setProcessedAt(Instant.now());
            } catch (Exception ex) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), ex.getMessage());
                break; // Stop batch to prevent out-of-order delivery
            }
        }
    }
}
```

---

## 5. Review Checklist
- [x] Clear Java interfaces and method signatures documented for all core services.
- [x] `@Transactional` boundaries and read-only optimizations applied.
- [x] Transactional Outbox atomic write and polling logic detailed.

---

## 6. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Software Architect | Initial baseline Service Layer Design document. |

