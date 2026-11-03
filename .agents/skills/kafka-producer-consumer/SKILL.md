---
name: kafka-producer-consumer
description: >-
  Use this skill when configuring Apache Kafka producers, consumers, serializers, deserializers,
  error handlers, and Dead Letter Topics (DLT).
---

# Apache Kafka Producer and Consumer Skill

## Purpose
Specifies the architecture, configuration, and implementation patterns for Apache Kafka message production, consumption, error recovery, and Dead Letter Topic handling.

## When to Use
- Producing domain events to Kafka topics in `task-service`.
- Consuming events and handling message deserialization in `notification-service`.
- Configuring retry policies and error handling for Kafka listeners.

## Required Inputs
- Target topic name (`task-events`).
- Event envelope schema definition.

## Step-by-Step Procedure
1. **Configure Kafka Producer**:
   In `task-service`:
   - Use `KafkaTemplate<String, Object>` with `StringSerializer` for keys and `JsonSerializer` for values.
   - Set `acks=all`, `retries=3`, `enable.idempotence=true`.
   - Send with aggregate ID as the partition key to guarantee per-task ordering:
     ```java
     kafkaTemplate.send("task-events", event.aggregateId().toString(), event);
     ```
2. **Configure Kafka Consumer**:
   In `notification-service`:
   - Group ID: `notification-service-group`.
   - Use `ErrorHandlingDeserializer` wrapping `StringDeserializer` (keys) and `JsonDeserializer` (values).
   - Set `auto.offset.reset=earliest`, `enable.auto.commit=false` (manual container ACK).
3. **Implement Listener Method**:
   ```java
   @KafkaListener(topics = "task-events", groupId = "notification-service-group")
   public void handleTaskEvent(
       @Payload TaskEventEnvelope envelope,
       @Header(KafkaHeaders.RECEIVED_KEY) String key,
       Acknowledgment acknowledgment
   ) {
       notificationProcessor.process(envelope);
       acknowledgment.acknowledge();
   }
   ```
4. **Configure Dead Letter Topic (DLT)**:
   - Configure `DefaultErrorHandler` with `DeadLetterPublishingRecoverer` targeting `task-events.DLT`.
   - Configure `FixedBackOff(1000L, 3L)` (3 retries with 1-second delay).

## Applicable Project Standards
- [Rule 06: Apache Kafka & Event-Driven Architecture Standards](file:///d:/learning/task_manager/.agents/rules/06-kafka-and-events.md)
- [Kafka Configuration Specification](file:///d:/learning/task_manager/documentation/08-kafka-event-design/06-kafka-configuration-specification.md)

## Validation Checklist
- [ ] Partition key is set to task UUID for ordering.
- [ ] Consumer uses manual acknowledgment mode (`AckMode.MANUAL_IMMEDIATE`).
- [ ] Error handler forwards poisoned messages to DLT without stalling the consumer partition.
- [ ] JSON deserializer trusted packages are configured (`*` or specific packages).

## Common Mistakes
- Omitting partition keys, causing random round-robin partitioning and out-of-order event consumption.
- Enabling auto-commit, causing potential message loss if processing fails after commit.

## Completion Criteria
Kafka producer emits structured envelopes; consumer processes events with manual acknowledgment and safely routes unrecoverable errors to DLT.

