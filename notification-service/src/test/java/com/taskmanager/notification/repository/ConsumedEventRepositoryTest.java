package com.taskmanager.notification.repository;

import com.taskmanager.notification.entity.ConsumedEventEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ConsumedEventRepositoryTest {

    @Autowired
    private ConsumedEventRepository consumedEventRepository;

    @Test
    @DisplayName("Should save consumed event and detect existing eventId for idempotency check")
    void testConsumedEventIdempotencyCheck() {
        UUID eventId = UUID.randomUUID();

        assertThat(consumedEventRepository.existsById(eventId)).isFalse();

        ConsumedEventEntity entity = new ConsumedEventEntity(eventId, "TASK_CREATED", "notification-service-group", Instant.now());
        consumedEventRepository.save(entity);

        assertThat(consumedEventRepository.existsById(eventId)).isTrue();
    }
}

