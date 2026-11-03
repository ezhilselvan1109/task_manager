# Database Testing Strategy

**Document Identifier:** DOC-TEST-010  
**Traceability References:** DOC-DB-008, DOC-DB-010, DOC-DB-011, DOC-TEST-001  
**Harness:** `@DataJpaTest`, Testcontainers PostgreSQL 16, Flyway Test Extension  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the database and persistence testing strategy across the Task Management platform. Persistence tests validate database constraints, Flyway migration script idempotency, optimistic locking collisions, and ACID transaction rollback boundaries.

---

## 2. Persistence Verification Scenarios

| Verification Area | Test Technique | Success Criteria |
|---|---|---|
| **Flyway Migration** | Clean container execution of all `V1_0` scripts | All tables, indexes, and extensions created without SQL errors. |
| **Check Constraints** | Insert task with invalid status string `'PENDING'` | PostgreSQL throws check constraint violation (`chk_task_status`). |
| **Unique Constraints** | Insert duplicate username or email | PostgreSQL throws unique constraint violation (`uq_users_username`). |
| **Optimistic Locking** | Simultaneous updates to same entity version | Second transaction throws `OptimisticLockingFailureException`. |
| **Transaction Rollback**| Runtime exception thrown after task save | Task row is rolled back cleanly; zero orphan records in DB. |

---

## 3. Optimistic Locking Test Example (`TaskConcurrencyTest.java`)

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskConcurrencyTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @DisplayName("Concurrent update to same task version triggers OptimisticLockingFailureException")
    void concurrentUpdate_ThrowsOptimisticLockException() {
        TaskEntity task = taskRepository.save(TaskEntity.builder()
                .userId(UUID.randomUUID())
                .title("Concurrent Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build());

        // First thread loads entity
        TaskEntity thread1Copy = taskRepository.findById(task.getId()).orElseThrow();
        // Second thread loads entity
        TaskEntity thread2Copy = taskRepository.findById(task.getId()).orElseThrow();

        // First thread updates and commits
        thread1Copy.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.saveAndFlush(thread1Copy);

        // Second thread attempts update with stale version
        thread2Copy.setStatus(TaskStatus.COMPLETED);
        assertThatThrownBy(() -> taskRepository.saveAndFlush(thread2Copy))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
```

