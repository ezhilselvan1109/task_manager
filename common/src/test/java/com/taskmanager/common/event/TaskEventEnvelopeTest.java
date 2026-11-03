package com.taskmanager.common.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaskEventEnvelopeTest {

    @Test
    @DisplayName("TaskEventEnvelope factory method creates complete event envelope")
    void testFactoryCreation() {
        UUID aggregateId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String correlationId = "corr-" + UUID.randomUUID();
        Map<String, Object> payload = Map.of("title", "Test Task", "status", "TODO");

        TaskEventEnvelope<Map<String, Object>> envelope = TaskEventEnvelope.create(
                TaskEventType.TASK_CREATED,
                aggregateId,
                userId,
                correlationId,
                payload
        );

        assertThat(envelope.eventId()).isNotNull();
        assertThat(envelope.eventType()).isEqualTo("TASK_CREATED");
        assertThat(envelope.eventVersion()).isEqualTo("1.0");
        assertThat(envelope.aggregateId()).isEqualTo(aggregateId);
        assertThat(envelope.userId()).isEqualTo(userId);
        assertThat(envelope.producer()).isEqualTo("task-service");
        assertThat(envelope.occurredAt()).isNotNull();
        assertThat(envelope.correlationId()).isEqualTo(correlationId);
        assertThat(envelope.payload()).isEqualTo(payload);
    }

    @Test
    @DisplayName("TaskEventType enum contains all required domain event types")
    void testTaskEventTypeEnumValues() {
        assertThat(TaskEventType.values()).containsExactly(
                TaskEventType.TASK_CREATED,
                TaskEventType.TASK_UPDATED,
                TaskEventType.TASK_COMPLETED,
                TaskEventType.TASK_DELETED
        );
    }
}

