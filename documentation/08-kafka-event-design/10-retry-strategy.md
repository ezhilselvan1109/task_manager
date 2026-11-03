# Kafka Retry Strategy: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-EVT-010 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Software Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-EVT-007`, `DOC-EVT-011`, `DOC-EVT-015` |

---

## 1. Multi-Level Retry Architecture
Retries handle transient glitches (network blips, momentary DB connection timeouts) without losing messages or halting consumer partitions:

```mermaid
flowchart TD
    Receive["Consumer Receives Record from task.events.v1"] --> Process{Attempt Processing}
    
    Process -->|Success| Commit[Commit Offset & Proceed]
    
    Process -->|Exception Thrown| Classify{Is Exception Fatal?}
    
    Classify -->|Fatal: Deserialization / Malformed JSON| DLT["Immediate Forward to task.events.v1.DLT"]
    Classify -->|Transient: DB Timeout / Lock Contention| Retry1["Retry 1: Wait 1s"]
    
    Retry1 --> Process1{Attempt 2}
    Process1 -->|Success| Commit
    Process1 -->|Fail| Retry2["Retry 2: Wait 2s (Exp Backoff)"]
    
    Retry2 --> Process2{Attempt 3}
    Process2 -->|Success| Commit
    Process2 -->|Fail (3/3 Retries Exhausted)| DLT
```

---

## 2. Spring Kafka Error Handler Configuration

```java
package com.chellaa.notification_service.kafka;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaRetryConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaOperations<Object, Object> kafkaOperations) {
        // 1. Recoverer forwards exhausted records to DLT
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
            kafkaOperations,
            (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );

        // 2. Exponential backoff: initial 1s, multiplier 2.0, max 3 attempts
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxAttempts(3);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // 3. Mark non-retryable fatal exceptions
        errorHandler.addNotRetryableExceptions(
            org.springframework.kafka.support.serializer.DeserializationException.class,
            com.fasterxml.jackson.core.JsonParseException.class,
            IllegalArgumentException.class
        );

        return errorHandler;
    }
}
```

---

## 3. Review Checklist
- [x] Multi-level retry flow mapped with transient vs fatal exception branching.
- [x] `DefaultErrorHandler` with `ExponentialBackOff` and `DeadLetterPublishingRecoverer` configured.
- [x] Deserialization exceptions bypass retry loop to prevent partition stalling.

---

## 4. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Software Architect | Initial baseline Kafka Retry Strategy document. |

