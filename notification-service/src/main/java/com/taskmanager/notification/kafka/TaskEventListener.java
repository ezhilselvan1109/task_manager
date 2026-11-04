package com.taskmanager.notification.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.common.constants.CorrelationConstants;
import com.taskmanager.common.event.TaskEventEnvelope;
import com.taskmanager.notification.dto.event.TaskEventPayload;
import com.taskmanager.notification.exception.EventProcessingException;
import com.taskmanager.notification.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Spring Kafka event listener consuming task domain events from topic task-events.
 * Manually acknowledges offsets only after database transaction commit (RULE-07).
 */
@Component
public class TaskEventListener {

    private static final Logger log = LoggerFactory.getLogger(TaskEventListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public TaskEventListener(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${kafka.topics.task-events:task-events}",
            groupId = "${spring.kafka.consumer.group-id:notification-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTaskEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String correlationId = null;
        try {
            TaskEventEnvelope<TaskEventPayload> envelope = objectMapper.readValue(
                    record.value(), new TypeReference<>() {}
            );

            correlationId = envelope.correlationId();
            if (correlationId != null && !correlationId.isBlank()) {
                MDC.put(CorrelationConstants.MDC_KEY, correlationId);
            }

            log.info("Received task event: ID {}, Type {}, Aggregate {}",
                    envelope.eventId(), envelope.eventType(), envelope.aggregateId());

            // Process event within database transaction
            notificationService.processTaskEvent(envelope);

            // Acknowledge Kafka record offset ONLY after successful DB transaction
            if (ack != null) {
                ack.acknowledge();
            }

            log.debug("Acknowledged Kafka record offset for event ID: {}", envelope.eventId());

        } catch (Exception e) {
            log.error("Failed to process task event record at offset {}: {}", record.offset(), e.getMessage(), e);
            throw (e instanceof RuntimeException re) ? re : new EventProcessingException("Error processing Kafka record", e);
        } finally {
            if (correlationId != null) {
                MDC.remove(CorrelationConstants.MDC_KEY);
            }
        }
    }
}

