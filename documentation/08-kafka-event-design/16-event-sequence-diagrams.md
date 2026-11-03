# Event Sequence Diagrams

**Document Identifier:** DOC-ARCH-EVT-SEQ-001  
**Traceability References:** EVT-001, EVT-002, EVT-003, EVT-004, FR-011, FR-012, NFR-006, BRULE-009, BRULE-010  
**Target Services:** `task-service`, `notification-service`, Apache Kafka Cluster  
**Status:** Approved Baseline  

---

## 1. Overview
This document visualizes the complete lifecycle of asynchronous events across the Task Management Microservices platform. These sequence diagrams illustrate the Transactional Outbox pattern, idempotent event ingestion, consumer retry loops, and Dead Letter Topic (DLT) routing for poison pills.

---

## 2. Diagram 1: Task Mutation and Transactional Outbox Publishing
Illustrates the dual-write elimination pattern in `task-service`, where the task entity update and the outbox event are persisted in the same local ACID transaction, followed by asynchronous relay to Kafka.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Web Frontend / Client
    participant Controller as TaskController
    participant Service as TaskService
    participant TaskRepo as TaskRepository
    participant OutboxRepo as OutboxEventRepository
    participant DB as PostgreSQL (task_db)
    participant Poller as OutboxPoller (Scheduled)
    participant Producer as KafkaProducerService
    participant Broker as Apache Kafka (task.events.v1)

    Client->>Controller: POST /api/v1/tasks (Create Task)
    Controller->>Service: createTask(CreateTaskRequest, userId)
    activate Service
    Note over Service,DB: Transaction Boundary: @Transactional
    Service->>TaskRepo: save(taskEntity)
    TaskRepo->>DB: INSERT INTO tasks (...)
    Service->>OutboxRepo: save(outboxEventEntity)
    OutboxRepo->>DB: INSERT INTO outbox_events (status='PENDING', ...)
    Service-->>Controller: TaskResponse DTO
    deactivate Service
    Controller-->>Client: 201 Created (Task Details)

    loop Polling Every 500ms
        Poller->>DB: SELECT * FROM outbox_events WHERE processed = FALSE FOR UPDATE SKIP LOCKED LIMIT 50
        DB-->>Poller: List<OutboxEvent> (Unprocessed batch)
        alt Records Found
            loop For Each OutboxEvent
                Poller->>Producer: send(topic, key, payload)
                Producer->>Broker: Produce Record (key=aggregateId, payload=EventEnvelope)
                Broker-->>Producer: RecordMetadata (Ack: acks=all, partition, offset)
                Poller->>DB: UPDATE outbox_events SET processed = TRUE, processed_at = NOW() WHERE id = :id
            end
        else No Records
            Poller->>Poller: Sleep until next polling cycle
        end
    end
```

---

## 3. Diagram 2: Consumer Ingestion and Idempotent Processing
Illustrates consumer deduplication in `notification-service` to ensure exactly-once business outcome despite at-least-once Kafka delivery.

```mermaid
sequenceDiagram
    autonumber
    participant Broker as Apache Kafka (task.events.v1)
    participant Listener as TaskEventListener (@KafkaListener)
    participant IdempotencyService as EventDeduplicationService
    participant NotifRepo as NotificationRepository
    participant ConsumedRepo as ConsumedEventRepository
    participant DB as PostgreSQL (notification_db)
    participant Ack as Acknowledgment (Manual)

    Broker->>Listener: ConsumerRecord(eventId, aggregateId, eventType, payload)
    activate Listener
    Listener->>IdempotencyService: isAlreadyProcessed(eventId)
    IdempotencyService->>DB: SELECT EXISTS(SELECT 1 FROM consumed_events WHERE event_id = :eventId)
    DB-->>IdempotencyService: Result (true / false)

    alt Event Already Processed (Duplicate Delivery)
        IdempotencyService-->>Listener: true (Duplicate Detected)
        Note over Listener: Log warning: Duplicate event skipped [eventId]
        Listener->>Ack: acknowledge()
        Ack-->>Broker: Commit Consumer Offset
    else Event Is New (First Delivery)
        IdempotencyService-->>Listener: false (New Event)
        Note over Listener,DB: Transaction Boundary: @Transactional
        Listener->>NotifRepo: save(notificationEntity)
        NotifRepo->>DB: INSERT INTO notifications (...)
        Listener->>ConsumedRepo: recordConsumedEvent(eventId, eventType)
        ConsumedRepo->>DB: INSERT INTO consumed_events (event_id, event_type, processed_at)
        Listener->>Ack: acknowledge()
        Ack-->>Broker: Commit Consumer Offset
    end
    deactivate Listener
```

---

## 4. Diagram 3: Poison Pill & Retries Routing to Dead Letter Topic (DLT)
Illustrates resilient failure recovery: transient errors are retried up to 3 times with exponential backoff; deserialization errors or persistent business exceptions are routed to `task.events.v1.DLT`.

```mermaid
sequenceDiagram
    autonumber
    participant Broker as Apache Kafka (task.events.v1)
    participant Listener as TaskEventListener
    participant ErrorHandler as DefaultErrorHandler
    participant DLTProducer as DeadLetterPublishingRecoverer
    participant DLTBroker as Apache Kafka (task.events.v1.DLT)
    participant Metrics as Micrometer / Alerting

    Broker->>Listener: ConsumerRecord (Malformed or triggering runtime exception)
    activate Listener
    Listener->>Listener: Process Event -> FAILS (e.g. transient DB lock or corrupt payload)
    Listener->>ErrorHandler: Handle Exception(exception, ConsumerRecord)
    deactivate Listener
    
    loop Retry Policy (Up to 3 Attempts with Exponential Backoff)
        ErrorHandler->>ErrorHandler: Backoff: 1s, 2s, 4s
        ErrorHandler->>Listener: Retry invocation
        activate Listener
        Listener-->>ErrorHandler: Fails again (Retries Exhausted)
        deactivate Listener
    end

    Note over ErrorHandler,DLTProducer: Retries Exhausted -> Recoverer Invoked
    ErrorHandler->>DLTProducer: recover(ConsumerRecord, Exception)
    DLTProducer->>DLTProducer: Add Header: x-original-topic, x-exception-message, x-stacktrace
    DLTProducer->>DLTBroker: Publish Record to task.events.v1.DLT
    DLTBroker-->>DLTProducer: ACK Record Saved
    DLTProducer->>Metrics: incrementCounter("kafka.consumer.dlt.routed")
    Note over Metrics: Prometheus alert triggers for SRE / DevOps oncall
    DLTProducer-->>Broker: Commit Offset of original record on task.events.v1
```

---

## 5. Architectural Guarantees Summary
1. **At-Least-Once Delivery**: Guaranteed via `acks=all`, outbox persistence, and transactional polling with status update.
2. **Idempotence & Exactly-Once Semantics**: Guaranteed by client-side deduplication table `consumed_events` using unique primary key `event_id`.
3. **Partition Ordering**: Ensured by using `aggregateId` (`taskId`) as the Kafka message partition key.
4. **Deadlock & Poller Concurrency Control**: Ensured by `FOR UPDATE SKIP LOCKED` during outbox batch polling.
5. **Fail-Safe Processing**: Poison pills are isolated in `task.events.v1.DLT` without blocking pipeline throughput or subsequent consumer offsets.

