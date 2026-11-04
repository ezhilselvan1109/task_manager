package com.taskmanager.task.service;

import com.taskmanager.task.entity.OutboxEventEntity;
import com.taskmanager.task.repository.OutboxEventRepository;
import com.taskmanager.task.service.outbox.OutboxPoller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPollerTest {

    @Mock
    private OutboxEventRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private OutboxPoller outboxPoller;

    @BeforeEach
    void setUp() {
        outboxPoller = new OutboxPoller(outboxRepository, kafkaTemplate, "task-events", 50, true);
    }

    @Test
    @DisplayName("Should poll pending outbox events using SKIP LOCKED and successfully publish to Kafka")
    void testPollAndPublishSuccess() {
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

        when(outboxRepository.fetchUnprocessedEventsForUpdate(50)).thenReturn(List.of(event));

        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("task-events"), eq(aggregateId), eq(event.getPayload()))).thenReturn(future);

        outboxPoller.pollAndPublish();

        assertThat(event.isProcessed()).isTrue();
        assertThat(event.getStatus()).isEqualTo("PUBLISHED");
        assertThat(event.getProcessedAt()).isNotNull();

        verify(kafkaTemplate).send(eq("task-events"), eq(aggregateId), eq(event.getPayload()));
        verify(outboxRepository).save(event);
    }

    @Test
    @DisplayName("Should fallback to standard query when SKIP LOCKED query is unsupported or throws")
    void testPollAndPublishFallback() {
        UUID eventId = UUID.randomUUID();
        String aggregateId = UUID.randomUUID().toString();
        OutboxEventEntity event = OutboxEventEntity.builder()
                .id(eventId)
                .aggregateId(aggregateId)
                .eventType("TASK_CREATED")
                .payload("{\"id\":\"" + aggregateId + "\"}")
                .processed(false)
                .status("PENDING")
                .build();

        when(outboxRepository.fetchUnprocessedEventsForUpdate(50)).thenThrow(new RuntimeException("SKIP LOCKED unsupported"));
        when(outboxRepository.fetchUnprocessedEvents(50)).thenReturn(List.of(event));

        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("task-events"), eq(aggregateId), eq(event.getPayload()))).thenReturn(future);

        outboxPoller.pollAndPublish();

        assertThat(event.isProcessed()).isTrue();
        assertThat(event.getStatus()).isEqualTo("PUBLISHED");
    }

    @Test
    @DisplayName("Should handle Kafka publication failure, increment retry count, and stop batch to maintain ordering")
    void testPollAndPublishFailureStopsBatch() {
        UUID eventId1 = UUID.randomUUID();
        String aggId1 = UUID.randomUUID().toString();
        OutboxEventEntity event1 = OutboxEventEntity.builder()
                .id(eventId1).aggregateId(aggId1).eventType("TASK_CREATED").payload("{\"seq\":1}")
                .processed(false).status("PENDING").retryCount(0).build();

        UUID eventId2 = UUID.randomUUID();
        String aggId2 = UUID.randomUUID().toString();
        OutboxEventEntity event2 = OutboxEventEntity.builder()
                .id(eventId2).aggregateId(aggId2).eventType("TASK_UPDATED").payload("{\"seq\":2}")
                .processed(false).status("PENDING").retryCount(0).build();

        when(outboxRepository.fetchUnprocessedEventsForUpdate(50)).thenReturn(List.of(event1, event2));

        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka Broker Offline"));
        when(kafkaTemplate.send(eq("task-events"), eq(aggId1), anyString())).thenReturn(failedFuture);

        outboxPoller.pollAndPublish();

        // First event should fail and increment retry count
        assertThat(event1.isProcessed()).isFalse();
        assertThat(event1.getStatus()).isEqualTo("FAILED_RETRY");
        assertThat(event1.getRetryCount()).isEqualTo(1);
        verify(outboxRepository).save(event1);

        // Second event must NOT be published in this cycle to maintain in-order sequencing
        assertThat(event2.isProcessed()).isFalse();
        verify(kafkaTemplate, never()).send(eq("task-events"), eq(aggId2), anyString());
    }

    @Test
    @DisplayName("Should skip polling when disabled")
    void testDisabledPoller() {
        OutboxPoller disabledPoller = new OutboxPoller(outboxRepository, kafkaTemplate, "task-events", 50, false);
        disabledPoller.pollAndPublish();

        verify(outboxRepository, never()).fetchUnprocessedEvents(any(Integer.class));
    }
}
