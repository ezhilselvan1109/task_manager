# Notification Service Implementation Guide

**Document Identifier:** DOC-DEV-006  
**Traceability References:** FR-011 to FR-014, EVT-007, EVT-013, DOC-EVT-007  
**Target Module:** `notification-service`  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the implementation guide for `notification-service`. The service consumes task lifecycle events from Apache Kafka, enforces consumer idempotency to prevent duplicate notifications, and exposes REST APIs for users to retrieve and acknowledge in-app notifications.

---

## 2. Dependencies (`notification-service/pom.xml`)

```xml
<dependencies>
    <!-- Web & Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
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

    <!-- Spring Kafka Consumer -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>

    <!-- Eureka & Observability -->
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

### 3.1 Domain Entities

```java
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_user_read", columnList = "user_id, is_read")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

@Entity
@Table(name = "consumed_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsumedEventEntity {
    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "consumer_group", nullable = false, length = 100)
    private String consumerGroup;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;
}
```

### 3.2 Idempotent Kafka Listener (`TaskEventListener.java`)
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventListener {

    private final ConsumedEventRepository consumedEventRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "task.events.v1",
        groupId = "notification-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleTaskEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            EventEnvelope<TaskEventPayload> envelope = objectMapper.readValue(
                    record.value(), new TypeReference<>() {});

            UUID eventId = envelope.eventId();

            // Idempotency Gate: Check if event was already processed
            if (consumedEventRepository.existsById(eventId)) {
                log.warn("Duplicate event detected, skipping: {}", eventId);
                ack.acknowledge();
                return;
            }

            // Generate In-App Notification
            TaskEventPayload payload = envelope.payload();
            String title = "Task Update: " + payload.title();
            String message = "Your task has transitioned to status " + payload.status();

            NotificationEntity notification = NotificationEntity.builder()
                    .userId(payload.userId())
                    .taskId(payload.taskId())
                    .eventType(envelope.eventType())
                    .title(title)
                    .message(message)
                    .read(false)
                    .build();

            notificationRepository.save(notification);

            // Record Consumed Event to guarantee idempotency
            consumedEventRepository.save(new ConsumedEventEntity(
                    eventId, envelope.eventType(), "notification-service-group", Instant.now()));

            ack.acknowledge();
            log.info("Successfully processed event {} and created notification", eventId);

        } catch (Exception e) {
            log.error("Failed to process event record: {}", record.value(), e);
            throw new EventProcessingException("Error processing Kafka record", e);
        }
    }
}
```

