package com.taskmanager.notification.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumedEventEntityTest {

    @Test
    @DisplayName("Should create ConsumedEventEntity and verify fields")
    void testConsumedEventEntity() {
        UUID eventId = UUID.randomUUID();
        Instant consumedAt = Instant.now();

        ConsumedEventEntity entity = new ConsumedEventEntity(eventId, "TASK_CREATED", "notification-service-group", consumedAt);

        assertThat(entity.getEventId()).isEqualTo(eventId);
        assertThat(entity.getEventType()).isEqualTo("TASK_CREATED");
        assertThat(entity.getConsumerGroup()).isEqualTo("notification-service-group");
        assertThat(entity.getConsumedAt()).isEqualTo(consumedAt);
    }
}

