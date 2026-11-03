# Workflow 04: Kafka Event Producer Development

## 1. Overview & Purpose
Defines the procedure for implementing a domain event publisher in `task-service`, adhering to the standard event envelope and Kafka partitioning rules.

- **Workflow ID**: `WF-004`
- **Trigger**: Domain event publishing requirement (`TASK_CREATED`, `TASK_UPDATED`, `TASK_COMPLETED`, `TASK_DELETED`).

## 2. Preconditions
- Apache Kafka broker configured and reachable.
- Topic `task-events` created with 3 partitions.

## 3. Required Inputs
- Event type and version (`TASK_CREATED`, `1.0`).
- Aggregate ID (Task UUID) and event payload data.

## 4. Execution Steps
1. **Define Event Envelope Record**:
   - Ensure payload implements standard `TaskEventEnvelope`:
     - `eventId`, `eventType`, `eventVersion`, `aggregateId`, `occurredAt`, `producer`, `correlationId`, `payload`.
2. **Configure KafkaTemplate**:
   - Configure producer serializer (`StringSerializer` for keys, `JsonSerializer` for envelope values).
   - Configure `acks=all`, `retries=3`.
3. **Implement Producer Service**:
   - Inject `KafkaTemplate<String, Object>`.
   - Send with `aggregateId.toString()` as the Kafka record key to ensure partition affinity.
4. **Attach Correlation ID Header**:
   - Extract `X-Correlation-ID` from SLF4J MDC and add as Kafka record header.
5. **Handle Asynchronous Send Callbacks**:
   - Attach `CompletableFuture` callback logging success/failure.

## 5. Skills to Use
- [`kafka-producer-consumer`](file:///d:/learning/task_manager/.agents/skills/kafka-producer-consumer/SKILL.md)
- [`java-21-development`](file:///d:/learning/task_manager/.agents/skills/java-21-development/SKILL.md)

## 6. Rules to Follow
- [Rule 06: Apache Kafka & Event-Driven Architecture Standards](file:///d:/learning/task_manager/.agents/rules/06-kafka-and-events.md)
- [Rule 07: Exception Handling & Logging Standards](file:///d:/learning/task_manager/.agents/rules/07-exception-handling-and-logging.md)

## 7. Tests and Validation
- Embedded Kafka or Testcontainers Kafka test asserting message is published to topic `task-events` with correct key.

## 8. Failure Handling
- If broker is unreachable, ensure retry backoff fires and does not block the caller synchronously.

## 9. Review Checklist
- [ ] Partition key is set to aggregate task ID.
- [ ] Standard event envelope contains all required metadata fields.
- [ ] Correlation ID is propagated in record headers.

## 10. Expected Outputs
- Kafka producer component.
- Event envelope records.
- Passing Kafka integration test.

