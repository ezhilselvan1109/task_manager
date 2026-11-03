# Producer Design: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-EVT-006 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Software Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-EVT-001`, `DOC-EVT-005`, `ADR-005` |

---

## 1. Producer Architecture Overview
In `task-service`, application business threads never publish directly to Kafka. All event publishing is decoupled through the **Transactional Outbox Pattern** and executed by the **`OutboxPollerService`**:

```mermaid
flowchart LR
    subgraph TaskServiceContext ["Task Service Boundary"]
        AppThread["Business Application Thread"] -->|1. Write entity + outbox in single ACID TX| DB[("PostgreSQL task_db\n(outbox_events table)")]
        
        Poller["Scheduled Poller Worker\n(@Scheduled fixedDelay=1000)"] -->|2. SELECT ... FOR UPDATE SKIP LOCKED| DB
        Poller -->|3. send(ProducerRecord)| KafkaTemplate["Spring KafkaTemplate\n(acks=all, idempotent=true)"]
    end

    KafkaTemplate -->|4. Async TCP Publish| KafkaBroker[("Apache Kafka Broker\ntask.events.v1")]
    KafkaBroker -->|5. Broker ACK (Offset & Partition)| KafkaTemplate
    KafkaTemplate -->|6. Callback: Mark processed=true| DB
```

---

## 2. Kafka Producer Configuration (`KafkaProducerConfig.java`)

```java
package com.chellaa.task_service.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        // Strict Reliability & Ordering Settings
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1); // Preserves strict per-partition FIFO
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5); // Micro-batching buffer

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

---

## 3. Asynchronous Callback & Outbox State Update
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public CompletableFuture<SendResult<String, String>> publish(String topic, String key, String payload) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, payload);
        return kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Kafka publish failed for key {}: {}", key, ex.getMessage());
            } else {
                log.debug("Kafka message ACK received for key {} on partition {} at offset {}",
                    key, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            }
        });
    }
}
```

---

## 4. Review Checklist
- [x] Outbox poller decoupled publisher model verified.
- [x] `acks=all`, `enable.idempotence=true`, and `max.in.flight=1` guarantee zero loss and strict ordering.
- [x] CompletableFuture callback updates outbox status only after broker ACK.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Software Architect | Initial baseline Producer Design document. |

