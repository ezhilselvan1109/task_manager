package com.taskmanager.notification.repository;

import com.taskmanager.notification.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for managing NotificationEntity persistence.
 * Enforces tenant-scoped lookups for anti-enumeration compliance (RULE-05).
 */
@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    Page<NotificationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<NotificationEntity> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndReadFalse(UUID userId);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = true, n.readAt = :readAt WHERE n.userId = :userId AND n.read = false")
    int markAllAsReadForUser(@Param("userId") UUID userId, @Param("readAt") Instant readAt);
}

