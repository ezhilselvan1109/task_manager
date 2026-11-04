package com.taskmanager.task.repository;

import com.taskmanager.task.domain.TaskPriority;
import com.taskmanager.task.domain.TaskStatus;
import com.taskmanager.task.entity.TaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for TaskEntity.
 * Enforces tenant ownership isolation and soft-delete filtering across all queries.
 */
@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {

    /**
     * Anti-enumeration lookup: returns task only if ID matches, belongs to caller, and is active.
     */
    Optional<TaskEntity> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);

    /**
     * Total active tasks for a user.
     */
    long countByUserIdAndDeletedFalse(UUID userId);

    /**
     * Count active tasks for a user matching a specific status.
     */
    long countByUserIdAndStatusAndDeletedFalse(UUID userId, TaskStatus status);

    /**
     * Count active tasks for a user matching any of the specified priorities.
     */
    long countByUserIdAndPriorityInAndDeletedFalse(UUID userId, Collection<TaskPriority> priorities);

    /**
     * Count overdue tasks for a user: dueDate < now, active, and not in COMPLETED or CANCELLED status.
     */
    @Query("""
        SELECT COUNT(t) FROM TaskEntity t
        WHERE t.userId = :userId
          AND t.deleted = false
          AND t.dueDate IS NOT NULL
          AND t.dueDate < :now
          AND t.status NOT IN (com.taskmanager.task.domain.TaskStatus.COMPLETED, com.taskmanager.task.domain.TaskStatus.CANCELLED)
    """)
    long countOverdueTasks(@Param("userId") UUID userId, @Param("now") Instant now);

    /**
     * Multi-attribute filtering, search, and pagination scoped strictly to the authenticated user.
     */
    @Query("""
        SELECT t FROM TaskEntity t
        WHERE t.userId = :userId
          AND t.deleted = false
          AND (:query IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR (t.description IS NOT NULL AND LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))))
          AND (:statuses IS NULL OR t.status IN :statuses)
          AND (:priorities IS NULL OR t.priority IN :priorities)
          AND (CAST(:dueFrom AS timestamp) IS NULL OR t.dueDate >= :dueFrom)
          AND (CAST(:dueTo AS timestamp) IS NULL OR t.dueDate <= :dueTo)
    """)
    Page<TaskEntity> findTasksWithFilters(
            @Param("userId") UUID userId,
            @Param("query") String query,
            @Param("statuses") Collection<TaskStatus> statuses,
            @Param("priorities") Collection<TaskPriority> priorities,
            @Param("dueFrom") Instant dueFrom,
            @Param("dueTo") Instant dueTo,
            Pageable pageable
    );
}

