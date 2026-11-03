---
name: transactional-outbox-idempotency
description: >-
  Use this skill when implementing the Transactional Outbox pattern for dual-write elimination,
  outbox event polling with SKIP LOCKED, and consumer idempotency tables.
---

# Transactional Outbox and Idempotency Skill

## Purpose
Enforces distributed transactional consistency without distributed locks or 2PC by implementing the Transactional Outbox pattern on the producer side and an Idempotency Deduplication table on the consumer side.

## When to Use
- Implementing or modifying task lifecycle event publishing in `task-service`.
- Writing the scheduled background worker that polls and drains the `outbox_events` table.
- Implementing duplicate message prevention in `notification-service`.

## Required Inputs
- Producer database table `outbox_events` (`task_db`).
- Consumer database table `consumed_events` (`notification_db`).

## Step-by-Step Procedure
1. **Persist Outbox Record in Local ACID Transaction**:
   When creating or updating a task, save the domain event in `outbox_events` inside the same `@Transactional` method:
   ```java
   @Transactional
   public TaskResponse createTask(CreateTaskRequest request, UUID userId) {
       Task task = taskRepository.save(buildTask(request, userId));
       TaskOutboxEvent outbox = TaskOutboxEvent.builder()
           .id(UUID.randomUUID())
           .aggregateType("TASK")
           .aggregateId(task.getId())
           .eventType("TASK_CREATED")
           .payload(objectMapper.writeValueAsString(buildPayload(task)))
           .status("PENDING")
           .createdAt(Instant.now())
           .build();
       outboxRepository.save(outbox);
       return taskMapper.toResponse(task);
   }
   ```
2. **Implement Outbox Poller with SKIP LOCKED**:
   Query pending outbox events using PostgreSQL concurrency-safe row locking:
   ```java
   @Query(value = """
       SELECT * FROM outbox_events 
       WHERE status = 'PENDING' 
       ORDER BY created_at ASC 
       LIMIT 50 
       FOR UPDATE SKIP LOCKED
       """, nativeQuery = true)
   List<TaskOutboxEvent> findPendingEventsForProcessing();
   ```
3. **Publish to Kafka & Mark Sent**:
   In `@Scheduled(fixedDelay = 500)`:
   - For each event, send to Kafka: `kafkaTemplate.send(...)`.
   - Update status to `PROCESSED` with `processed_at = Instant.now()`.
4. **Implement Consumer Deduplication Table**:
   In `notification-service`:
   - Store incoming `eventId` into `consumed_events(event_id, consumer_group, processed_at)`.
   - If a duplicate `eventId` arrives, `DataIntegrityViolationException` occurs on primary key; catch it and acknowledge message without creating duplicate notifications.

## Applicable Project Standards
- [Rule 06: Apache Kafka & Event-Driven Architecture Standards](file:///d:/learning/task_manager/.agents/rules/06-kafka-and-events.md)
- [Transactional Outbox Pattern Design](file:///d:/learning/task_manager/documentation/08-kafka-event-design/03-transactional-outbox-pattern-design.md)
- [Idempotent Consumer Design](file:///d:/learning/task_manager/documentation/08-kafka-event-design/04-idempotent-consumer-design.md)

## Validation Checklist
- [ ] Outbox record and business entity are saved in the same `@Transactional` boundary.
- [ ] Poller uses `FOR UPDATE SKIP LOCKED` to allow multiple service replicas to run safely.
- [ ] Consumer checks `consumed_events` table before applying business logic.
- [ ] Duplicate event delivery does not create duplicate notification records.

## Common Mistakes
- Publishing directly to Kafka inside the HTTP controller or service method (creates dual-write vulnerability if Kafka fails).
- Omitting `SKIP LOCKED` in outbox queries, causing deadlocks between concurrent publisher instances.

## Completion Criteria
Domain events are guaranteed to be published reliably even if Kafka is temporarily offline; consumers process each event exactly once logically.

