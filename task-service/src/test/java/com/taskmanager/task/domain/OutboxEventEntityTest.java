package com.taskmanager.task.domain;

import com.taskmanager.task.entity.OutboxEventEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventEntityTest {

    @Test
    @DisplayName("Should instantiate OutboxEventEntity with default PENDING status")
    void testOutboxEventCreation() {
        UUID eventId = UUID.randomUUID();
        String aggregateId = UUID.randomUUID().toString();

        OutboxEventEntity event = OutboxEventEntity.builder()
                .id(eventId)
                .aggregateId(aggregateId)
                .eventType("TASK_CREATED")
                .payload("{\"id\":\"" + aggregateId + "\"}")
                .processed(false)
                .status("PENDING")
                .retryCount(0)
                .createdAt(Instant.now())
                .build();

        assertThat(event.getId()).isEqualTo(eventId);
        assertThat(event.getAggregateId()).isEqualTo(aggregateId);
        assertThat(event.getEventType()).isEqualTo("TASK_CREATED");
        assertThat(event.isProcessed()).isFalse();
        assertThat(event.getStatus()).isEqualTo("PENDING");
        assertThat(event.getRetryCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should mark outbox event as published and record processed timestamp")
    void testMarkPublished() {
        OutboxEventEntity event = OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .aggregateId(UUID.randomUUID().toString())
                .eventType("TASK_COMPLETED")
                .payload("{}")
                .processed(false)
                .status("PENDING")
                .build();

        assertThat(event.isProcessed()).isFalse();
        assertThat(event.getProcessedAt()).isNull();

        event.markPublished();

        assertThat(event.isProcessed()).isTrue();
        assertThat(event.getStatus()).isEqualTo("PUBLISHED");
        assertThat(event.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should increment retry count and update status on publication failure")
    void testIncrementRetry() {
        OutboxEventEntity event = OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .aggregateId(UUID.randomUUID().toString())
                .eventType("TASK_UPDATED")
                .payload("{}")
                .processed(false)
                .status("PENDING")
                .retryCount(0)
                .build();

        event.incrementRetry();

        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getStatus()).isEqualTo("FAILED_RETRY");
    }
}

