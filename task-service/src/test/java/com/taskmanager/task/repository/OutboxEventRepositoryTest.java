package com.taskmanager.task.repository;

import com.taskmanager.task.entity.OutboxEventEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OutboxEventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    @DisplayName("Should fetch unprocessed events ordered by creation timestamp")
    void testFetchUnprocessedEvents() {
        Instant t1 = Instant.now().minusSeconds(100);
        Instant t2 = Instant.now().minusSeconds(50);

        OutboxEventEntity event1 = OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .aggregateId(UUID.randomUUID().toString())
                .eventType("TASK_CREATED")
                .payload("{\"seq\":1}")
                .processed(false)
                .status("PENDING")
                .createdAt(t1)
                .build();

        OutboxEventEntity event2 = OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .aggregateId(UUID.randomUUID().toString())
                .eventType("TASK_UPDATED")
                .payload("{\"seq\":2}")
                .processed(false)
                .status("PENDING")
                .createdAt(t2)
                .build();

        OutboxEventEntity processedEvent = OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .aggregateId(UUID.randomUUID().toString())
                .eventType("TASK_COMPLETED")
                .payload("{\"seq\":3}")
                .processed(true)
                .status("PUBLISHED")
                .createdAt(Instant.now().minusSeconds(200))
                .processedAt(Instant.now().minusSeconds(190))
                .build();

        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.persist(processedEvent);
        entityManager.flush();

        List<OutboxEventEntity> pending = outboxEventRepository.fetchUnprocessedEvents(10);

        assertThat(pending).hasSize(2);
        assertThat(pending.get(0).getId()).isEqualTo(event1.getId());
        assertThat(pending.get(1).getId()).isEqualTo(event2.getId());
    }
}

