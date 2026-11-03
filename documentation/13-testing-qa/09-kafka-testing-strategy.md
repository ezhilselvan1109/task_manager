# Kafka and Event Testing Strategy

**Document Identifier:** DOC-TEST-009  
**Traceability References:** EVT-001 to EVT-015, DOC-EVT-013, DOC-EVT-011  
**Harnesses:** Spring Embedded Kafka (`@EmbeddedKafka`), Testcontainers Kafka  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the testing methodology for asynchronous event-driven communications between `task-service` and `notification-service`. It covers partition key verification, consumer deduplication testing, retry backoff validation, and Dead Letter Topic (DLT) routing assertions.

---

## 2. Test Scenarios for Event Verification

| Test Scenario | Verification Focus | Expected Assertion |
|---|---|---|
| **Partition Key Ordering** | Verify `aggregateId` (`taskId`) is used as record key. | Records for same `taskId` land on identical Kafka partition. |
| **Payload Schema Conformity** | Verify JSON envelope structure matches JSON Schema 2020-12. | Contains `eventId`, `eventType`, `aggregateId`, `occurredAt`, `payload`. |
| **Consumer Idempotency** | Verify second delivery of same `eventId` is ignored. | Zero duplicate rows in `notifications` table; offset commits cleanly. |
| **Poison Pill Isolation** | Verify corrupted record is moved to `.DLT`. | Record appears on `task.events.v1.DLT` with `x-exception-message` header. |

---

## 3. Embedded Kafka Consumer Test Implementation

```java
@SpringBootTest
@EmbeddedKafka(partitions = 3, topics = { "task.events.v1", "task.events.v1.DLT" })
@DirtiesContext
class NotificationEventListenerEmbeddedTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ConsumedEventRepository consumedEventRepository;

    @Test
    @DisplayName("Should ingest event once and ignore duplicate on second delivery")
    void handleEvent_DuplicateDelivery_IsIdempotent() {
        UUID eventId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String eventJson = """
        {
          "eventId": "%s",
          "eventType": "TASK_CREATED",
          "eventVersion": "1.0",
          "aggregateId": "%s",
          "occurredAt": "2026-11-03T18:00:00Z",
          "producer": "task-service",
          "payload": {
            "taskId": "%s",
            "userId": "%s",
            "title": "Idempotency Test",
            "status": "TODO"
          }
        }
        """.formatted(eventId, taskId, taskId, userId);

        // Send first message
        kafkaTemplate.send("task.events.v1", taskId.toString(), eventJson);

        // Wait and verify first processing
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(consumedEventRepository.existsById(eventId)).isTrue();
            assertThat(notificationRepository.findAllByUserId(userId)).hasSize(1);
        });

        // Send second duplicate message
        kafkaTemplate.send("task.events.v1", taskId.toString(), eventJson);

        // Verify still exactly 1 notification exists
        await().pollDelay(2, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(notificationRepository.findAllByUserId(userId)).hasSize(1);
        });
    }
}
```

