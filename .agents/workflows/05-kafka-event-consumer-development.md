# Workflow 05: Kafka Event Consumer Development

## 1. Overview & Purpose
Defines the procedure for implementing a reliable, idempotent Kafka event consumer in `notification-service`.

- **Workflow ID**: `WF-005`
- **Trigger**: New event consumption requirement in `notification-service`.

## 2. Preconditions
- Kafka broker running with target topic `task-events`.
- Consumer group `notification-service-group` designated.
- Database table `consumed_events` provisioned via Flyway.

## 3. Required Inputs
- Target topic name.
- Event payload schema and deserializer class.
- Notification persistence logic.

## 4. Execution Steps
1. **Configure Consumer Factory**:
   - Use `ErrorHandlingDeserializer` wrapping `JsonDeserializer`.
   - Set `AckMode.MANUAL_IMMEDIATE`.
   - Add trusted packages (`com.taskmanager.*`).
2. **Implement Consumer Listener Method**:
   - Annotate listener with `@KafkaListener(topics = "task-events", groupId = "notification-service-group")`.
   - Inject `TaskEventEnvelope`, `Acknowledgment`, and correlation headers.
3. **Propagate Correlation ID to MDC**:
   - Read correlation ID from Kafka headers and put into SLF4J MDC before processing.
4. **Invoke Business Logic**:
   - Pass envelope to notification processor service.
5. **Acknowledge Offset**:
   - Call `acknowledgment.acknowledge()` after successful processing or idempotent skip.
6. **Configure DLT Recovery**:
   - Configure `DeadLetterPublishingRecoverer` to route poisoned records to `task-events.DLT`.

## 5. Skills to Use
- [`kafka-producer-consumer`](file:///d:/learning/task_manager/.agents/skills/kafka-producer-consumer/SKILL.md)
- [`transactional-outbox-idempotency`](file:///d:/learning/task_manager/.agents/skills/transactional-outbox-idempotency/SKILL.md)

## 6. Rules to Follow
- [Rule 06: Apache Kafka & Event-Driven Architecture Standards](file:///d:/learning/task_manager/.agents/rules/06-kafka-and-events.md)
- [Rule 07: Exception Handling & Logging Standards](file:///d:/learning/task_manager/.agents/rules/07-exception-handling-and-logging.md)

## 7. Tests and Validation
- Integration test with Testcontainers publishing a mock event and verifying notification creation in `notification_db`.

## 8. Failure Handling
- On transient error: allow Spring Kafka retry backoff (3 attempts).
- On poisoned payload: send to DLT without consumer group stall.

## 9. Review Checklist
- [ ] Manual offset acknowledgment is invoked.
- [ ] MDC correlation ID is populated and cleared in `finally`.
- [ ] Poisoned messages route to `task-events.DLT`.

## 10. Expected Outputs
- Kafka listener component.
- Passing integration test with Testcontainers.

