# Task Service Implementation Guide

**Document Identifier:** DOC-DEV-005  
**Traceability References:** FR-005 to FR-012, SEC-002, EVT-001 to EVT-004, DOC-EVT-014  
**Target Module:** `task-service`  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the implementation guide for `task-service`. The service manages task creation, retrieval, updates, status transitions, priority changes, dynamic filtering, pagination, and dashboard metrics. It strictly enforces tenant ownership isolation and implements the Transactional Outbox pattern to emit asynchronous domain events to Apache Kafka.

---

## 2. Dependencies (`task-service/pom.xml`)

```xml
<dependencies>
    <!-- Web & Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Persistence: JPA, PostgreSQL, Flyway -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>

    <!-- Apache Kafka Messaging -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>

    <!-- MapStruct & Lombok -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${org.mapstruct.version}</version>
    </dependency>

    <!-- Eureka & Actuator -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
</dependencies>
```

---

## 3. Core Implementation Classes

### 3.1 Task Entity (`TaskEntity.java`)
```java
@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_tasks_user_status", columnList = "user_id, status"),
    @Index(name = "idx_tasks_user_due_date", columnList = "user_id, due_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;
}
```

### 3.2 Task Service with Outbox Emission (`TaskServiceImpl.java`)
```java
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final OutboxEventRepository outboxRepository;
    private final TaskMapper taskMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, UUID userId) {
        TaskEntity task = TaskEntity.builder()
                .userId(userId)
                .title(request.title())
                .description(request.description())
                .status(TaskStatus.TODO)
                .priority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM)
                .dueDate(request.dueDate())
                .deleted(false)
                .build();

        TaskEntity savedTask = taskRepository.save(task);

        // Transactional Outbox: Save event in same local ACID transaction
        saveOutboxEvent("TASK_CREATED", savedTask);

        return taskMapper.toResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID taskId, UUID userId) {
        // Anti-enumeration: returns 404 if record doesn't belong to caller
        TaskEntity task = taskRepository.findByIdAndUserIdAndDeletedFalse(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
        return taskMapper.toResponse(task);
    }

    private void saveOutboxEvent(String eventType, TaskEntity task) {
        try {
            TaskEventPayload payload = new TaskEventPayload(task.getId(), task.getUserId(), 
                    task.getTitle(), task.getStatus().name(), task.getPriority().name(), task.getDueDate());
            
            EventEnvelope<TaskEventPayload> envelope = new EventEnvelope<>(
                    UUID.randomUUID(), eventType, "1.0", task.getId(), Instant.now(),
                    "task-service", MDC.get("correlationId"), payload
            );

            OutboxEventEntity outbox = OutboxEventEntity.builder()
                    .aggregateType("TASK")
                    .aggregateId(task.getId())
                    .eventType(eventType)
                    .payload(objectMapper.writeValueAsString(envelope))
                    .status("PENDING")
                    .processed(false)
                    .build();

            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new EventSerializationException("Failed to serialize outbox event", e);
        }
    }
}
```

### 3.3 Outbox Poller (`OutboxPoller.java`)
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void pollAndPublish() {
        List<OutboxEventEntity> pendingEvents = outboxRepository.fetchUnprocessedEventsForUpdate(50);
        
        for (OutboxEventEntity event : pendingEvents) {
            try {
                // Key = aggregateId (guarantees partition order per task)
                kafkaTemplate.send("task.events.v1", event.getAggregateId().toString(), event.getPayload())
                        .get(5, TimeUnit.SECONDS);

                event.setProcessed(true);
                event.setStatus("PUBLISHED");
                event.setProcessedAt(Instant.now());
            } catch (Exception e) {
                log.error("Failed to publish outbox event: {}", event.getId(), e);
                event.setRetryCount(event.getRetryCount() + 1);
                break; // Stop batch on broker failure to maintain sequential ordering
            }
        }
    }
}
```

