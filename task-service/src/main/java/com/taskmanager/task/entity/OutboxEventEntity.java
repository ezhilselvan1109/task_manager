package com.taskmanager.task.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping the outbox_events table in task_db.
 * Implements the Transactional Outbox pattern for dual-write elimination according to ADR-005.
 */
@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_pending_poller", columnList = "created_at"),
        @Index(name = "idx_outbox_aggregate", columnList = "aggregate_id")
})
public class OutboxEventEntity {

    @Id
    private UUID id;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private boolean processed = false;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "processed_at")
    private Instant processedAt;

    public OutboxEventEntity() {
    }

    public OutboxEventEntity(UUID id, String aggregateId, String eventType, String payload,
                             boolean processed, String status, int retryCount,
                             Instant createdAt, Instant processedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.processed = processed;
        this.status = status != null ? status : "PENDING";
        this.retryCount = retryCount;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.processedAt = processedAt;
    }

    public void markPublished() {
        this.processed = true;
        this.status = "PUBLISHED";
        this.processedAt = Instant.now();
    }

    public void incrementRetry() {
        this.retryCount++;
        this.status = "FAILED_RETRY";
    }

    public static OutboxEventEntityBuilder builder() {
        return new OutboxEventEntityBuilder();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public static class OutboxEventEntityBuilder {
        private UUID id;
        private String aggregateId;
        private String eventType;
        private String payload;
        private boolean processed = false;
        private String status = "PENDING";
        private int retryCount = 0;
        private Instant createdAt = Instant.now();
        private Instant processedAt;

        OutboxEventEntityBuilder() {
        }

        public OutboxEventEntityBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public OutboxEventEntityBuilder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public OutboxEventEntityBuilder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public OutboxEventEntityBuilder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public OutboxEventEntityBuilder processed(boolean processed) {
            this.processed = processed;
            return this;
        }

        public OutboxEventEntityBuilder status(String status) {
            this.status = status;
            return this;
        }

        public OutboxEventEntityBuilder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public OutboxEventEntityBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public OutboxEventEntityBuilder processedAt(Instant processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public OutboxEventEntity build() {
            return new OutboxEventEntity(id, aggregateId, eventType, payload, processed, status, retryCount, createdAt, processedAt);
        }
    }
}

