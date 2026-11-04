package com.taskmanager.notification.repository;

import com.taskmanager.notification.entity.ConsumedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA Repository for managing ConsumedEventEntity.
 * Serves as the database-level idempotency gate (RULE-07).
 */
@Repository
public interface ConsumedEventRepository extends JpaRepository<ConsumedEventEntity, UUID> {
}

