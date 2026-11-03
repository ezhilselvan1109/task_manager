package com.taskmanager.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Standard envelope wrapping all domain events emitted to Apache Kafka topic task-events.
 * Conforms to JSON Schema specification DOC-EVT-005.
 *
 * @param <T> Concrete payload type associated with the event.
 */
public record TaskEventEnvelope<T>(
    @NotNull
    @JsonProperty("eventId")
    UUID eventId,

    @NotBlank
    @JsonProperty("eventType")
    String eventType,

    @NotBlank
    @JsonProperty("eventVersion")
    String eventVersion,

    @NotNull
    @JsonProperty("aggregateId")
    UUID aggregateId,

    @NotNull
    @JsonProperty("userId")
    UUID userId,

    @NotBlank
    @JsonProperty("producer")
    String producer,

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    @JsonProperty("occurredAt")
    Instant occurredAt,

    @NotBlank
    @JsonProperty("correlationId")
    String correlationId,

    @NotNull
    @JsonProperty("payload")
    T payload
) {
    public static final String DEFAULT_VERSION = "1.0";
    public static final String PRODUCER_TASK_SERVICE = "task-service";

    /**
     * Convenience factory for creating a new TaskEventEnvelope from task-service.
     */
    public static <T> TaskEventEnvelope<T> create(
            TaskEventType eventType,
            UUID aggregateId,
            UUID userId,
            String correlationId,
            T payload
    ) {
        return new TaskEventEnvelope<>(
                UUID.randomUUID(),
                eventType.name(),
                DEFAULT_VERSION,
                aggregateId,
                userId,
                PRODUCER_TASK_SERVICE,
                Instant.now(),
                correlationId,
                payload
        );
    }
}

