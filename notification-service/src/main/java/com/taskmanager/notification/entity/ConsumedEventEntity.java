package com.taskmanager.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity maintaining consumed event IDs to enforce consumer idempotency (RULE-07).
 * Conforms to ADR-006, DOC-EVT-013, and DOC-DEV-006.
 */
@Entity
@Table(name = "consumed_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsumedEventEntity {

    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "event_type", length = 50)
    private String eventType;

    @Column(name = "consumer_group", length = 100)
    private String consumerGroup;

    @Column(name = "consumed_at", nullable = false)
    private Instant consumedAt;

    public ConsumedEventEntity(UUID eventId, Instant consumedAt) {
        this.eventId = eventId;
        this.consumedAt = consumedAt;
    }
}

