package com.taskmanager.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.common.constants.CorrelationConstants;
import com.taskmanager.common.event.TaskEventEnvelope;
import com.taskmanager.common.event.TaskEventType;
import com.taskmanager.notification.dto.event.TaskEventPayload;
import com.taskmanager.notification.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Acknowledgment acknowledgment;

    private ObjectMapper objectMapper;
    private TaskEventListener taskEventListener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        taskEventListener = new TaskEventListener(notificationService, objectMapper);
    }

    @Test
    @DisplayName("Should deserialize record, process event, propagate MDC correlation ID, and acknowledge offset")
    void testHandleTaskEventSuccess() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        String correlationId = "corr-event-999";

        TaskEventPayload payload = new TaskEventPayload(
                taskId, userId, "Build Feature", "Description", "TODO", "HIGH", Instant.now().plusSeconds(3600)
        );

        TaskEventEnvelope<TaskEventPayload> envelope = new TaskEventEnvelope<>(
                eventId, TaskEventType.TASK_CREATED.name(), "1.0", taskId, userId, "task-service", Instant.now(), correlationId, payload
        );

        String jsonRecord = objectMapper.writeValueAsString(envelope);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("task-events", 0, 100L, taskId.toString(), jsonRecord);

        taskEventListener.handleTaskEvent(record, acknowledgment);

        // Verify service processed envelope
        ArgumentCaptor<TaskEventEnvelope<TaskEventPayload>> captor = ArgumentCaptor.forClass(TaskEventEnvelope.class);
        verify(notificationService).processTaskEvent(captor.capture());
        assertThat(captor.getValue().eventId()).isEqualTo(eventId);
        assertThat(captor.getValue().correlationId()).isEqualTo(correlationId);

        // Verify Kafka acknowledgment
        verify(acknowledgment).acknowledge();

        // Verify MDC cleaned up
        assertThat(MDC.get(CorrelationConstants.MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("Should throw exception and omit acknowledgment when payload cannot be processed")
    void testHandleTaskEventFailureDoesNotAcknowledge() {
        ConsumerRecord<String, String> invalidRecord = new ConsumerRecord<>("task-events", 0, 101L, "key", "not-a-valid-json");

        assertThatThrownBy(() -> taskEventListener.handleTaskEvent(invalidRecord, acknowledgment))
                .isInstanceOf(RuntimeException.class);

        verify(notificationService, never()).processTaskEvent(any());
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("Should throw exception and omit acknowledgment when service throws error (allowing retry)")
    void testHandleTaskEventServiceFailureDoesNotAcknowledge() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        TaskEventPayload payload = new TaskEventPayload(taskId, userId, "T", "D", "TODO", "LOW", null);
        TaskEventEnvelope<TaskEventPayload> envelope = new TaskEventEnvelope<>(
                eventId, "TASK_CREATED", "1.0", taskId, userId, "task-service", Instant.now(), "c", payload
        );

        ConsumerRecord<String, String> record = new ConsumerRecord<>("task-events", 0, 102L, taskId.toString(), objectMapper.writeValueAsString(envelope));

        doThrow(new RuntimeException("Database down")).when(notificationService).processTaskEvent(any());

        assertThatThrownBy(() -> taskEventListener.handleTaskEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database down");

        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("Should handle event with null/blank correlationId and null acknowledgment safely")
    void testHandleTaskEventNullCorrelationIdAndNullAck() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        TaskEventPayload payload = new TaskEventPayload(taskId, userId, "T", "D", "TODO", "LOW", null);
        TaskEventEnvelope<TaskEventPayload> envelope = new TaskEventEnvelope<>(
                eventId, "TASK_CREATED", "1.0", taskId, userId, "task-service", Instant.now(), null, payload
        );

        ConsumerRecord<String, String> record = new ConsumerRecord<>("task-events", 0, 103L, taskId.toString(), objectMapper.writeValueAsString(envelope));

        taskEventListener.handleTaskEvent(record, null);

        verify(notificationService).processTaskEvent(any());
        assertThat(MDC.get(CorrelationConstants.MDC_KEY)).isNull();
    }
}

