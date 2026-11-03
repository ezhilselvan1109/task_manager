# Repository Layer Design: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-LLD-007 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Database Architect & Software Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-LLD-001`, `DOC-LLD-006` |

---

## 1. Overview
The Repository Layer interfaces with PostgreSQL 16 using Spring Data JPA and Hibernate. All queries accessing tenant entities strictly enforce multi-tenant isolation by requiring `userId` in `WHERE` clauses.

---

## 2. Task Service Repositories

### 2.1 `TaskRepository`
```java
package com.chellaa.task_service.repository;

import com.chellaa.task_service.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID>, JpaSpecificationExecutor<TaskEntity> {

    // Multi-tenant scoped lookup
    Optional<TaskEntity> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);

    // Dashboard: Count by status
    @Query("""
        SELECT t.status AS status, COUNT(t) AS count 
        FROM TaskEntity t 
        WHERE t.userId = :userId AND t.isDeleted = false 
        GROUP BY t.status
    """)
    List<StatusCountProjection> countStatusBreakdownByUserId(@Param("userId") UUID userId);

    // Dashboard: High priority active tasks
    @Query("""
        SELECT COUNT(t) 
        FROM TaskEntity t 
        WHERE t.userId = :userId 
          AND t.priority IN ('HIGH', 'URGENT') 
          AND t.status NOT IN ('COMPLETED', 'CANCELLED') 
          AND t.isDeleted = false
    """)
    long countHighPriorityTasks(@Param("userId") UUID userId);

    // Dashboard: Dynamic overdue tasks
    @Query("""
        SELECT COUNT(t) 
        FROM TaskEntity t 
        WHERE t.userId = :userId 
          AND t.dueDate IS NOT NULL 
          AND t.dueDate < :now 
          AND t.status NOT IN ('COMPLETED', 'CANCELLED') 
          AND t.isDeleted = false
    """)
    long countOverdueTasks(@Param("userId") UUID userId, @Param("now") Instant now);
}
```

### 2.2 `OutboxEventRepository`
```java
package com.chellaa.task_service.repository;

import com.chellaa.task_service.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query(value = """
        SELECT * FROM outbox_events 
        WHERE processed = false 
        ORDER BY created_at ASC 
        LIMIT :limit 
        FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<OutboxEventEntity> fetchPendingForPublishing(@Param("limit") int limit);
}
```

---

## 3. User & Notification Repositories

### 3.1 `UserRepository` (`user-service`)
```java
@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

### 3.2 `ConsumedEventRepository` (`notification-service`)
```java
@Repository
public interface ConsumedEventRepository extends JpaRepository<ConsumedEventEntity, UUID> {
    boolean existsById(UUID eventId);
}
```

---

## 4. Review Checklist
- [x] Multi-tenant isolation parameterized across all queries.
- [x] Native `SKIP LOCKED` query defined for outbox poller.
- [x] Projections utilized for aggregation queries to avoid entity hydration overhead.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Database Architect | Initial baseline Repository Layer Design. |

