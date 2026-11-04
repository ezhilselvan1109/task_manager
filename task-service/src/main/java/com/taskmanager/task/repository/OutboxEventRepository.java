package com.taskmanager.task.repository;

import com.taskmanager.task.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for outbox_events table.
 * Implements SKIP LOCKED row-level locking for concurrent publisher scale out without race conditions.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    /**
     * Poller query locking up to batchSize unprocessed events using PostgreSQL SKIP LOCKED.
     */
    @Query(value = """
        SELECT * FROM outbox_events 
        WHERE processed = false 
        ORDER BY created_at ASC 
        LIMIT :batchSize 
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<OutboxEventEntity> fetchUnprocessedEventsForUpdate(@Param("batchSize") int batchSize);

    /**
     * Standard query for fetching unprocessed outbox events without locking.
     */
    @Query(value = """
        SELECT * FROM outbox_events 
        WHERE processed = false 
        ORDER BY created_at ASC 
        LIMIT :batchSize
        """, nativeQuery = true)
    List<OutboxEventEntity> fetchUnprocessedEvents(@Param("batchSize") int batchSize);
}

