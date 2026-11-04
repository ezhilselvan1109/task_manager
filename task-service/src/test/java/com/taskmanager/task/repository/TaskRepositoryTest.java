package com.taskmanager.task.repository;

import com.taskmanager.task.domain.TaskPriority;
import com.taskmanager.task.domain.TaskStatus;
import com.taskmanager.task.entity.TaskEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @DisplayName("Should find active task by ID and owner userId")
    void testFindByIdAndUserIdAndDeletedFalse() {
        UUID userId = UUID.randomUUID();
        TaskEntity task = TaskEntity.builder()
                .userId(userId)
                .title("Complete Outbox Implementation")
                .description("Follow ADR-005")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .deleted(false)
                .build();
        TaskEntity persisted = entityManager.persistFlushFind(task);

        Optional<TaskEntity> found = taskRepository.findByIdAndUserIdAndDeletedFalse(persisted.getId(), userId);
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Complete Outbox Implementation");

        // Anti-enumeration: querying with a different userId must return empty Optional
        UUID otherUserId = UUID.randomUUID();
        Optional<TaskEntity> crossUserLookup = taskRepository.findByIdAndUserIdAndDeletedFalse(persisted.getId(), otherUserId);
        assertThat(crossUserLookup).isEmpty();

        // Soft-deleted task must return empty Optional
        persisted.markDeleted();
        entityManager.persistFlushFind(persisted);
        Optional<TaskEntity> deletedLookup = taskRepository.findByIdAndUserIdAndDeletedFalse(persisted.getId(), userId);
        assertThat(deletedLookup).isEmpty();
    }

    @Test
    @DisplayName("Should paginate and filter tasks by status and priority for a specific owner")
    void testFindWithFiltersAndPagination() {
        UUID userId = UUID.randomUUID();

        TaskEntity task1 = TaskEntity.builder()
                .userId(userId)
                .title("Task Alpha")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .deleted(false)
                .build();
        TaskEntity task2 = TaskEntity.builder()
                .userId(userId)
                .title("Task Beta")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.LOW)
                .deleted(false)
                .build();
        TaskEntity otherUserTask = TaskEntity.builder()
                .userId(UUID.randomUUID())
                .title("Other Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .deleted(false)
                .build();

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(otherUserTask);
        entityManager.flush();

        Page<TaskEntity> allActive = taskRepository.findTasksWithFilters(
                userId, null, null, null, null, null, PageRequest.of(0, 10)
        );
        assertThat(allActive.getTotalElements()).isEqualTo(2);

        Page<TaskEntity> todoOnly = taskRepository.findTasksWithFilters(
                userId, null, List.of(TaskStatus.TODO), null, null, null, PageRequest.of(0, 10)
        );
        assertThat(todoOnly.getTotalElements()).isEqualTo(1);
        assertThat(todoOnly.getContent().get(0).getTitle()).isEqualTo("Task Alpha");

        Page<TaskEntity> keywordSearch = taskRepository.findTasksWithFilters(
                userId, "Beta", null, null, null, null, PageRequest.of(0, 10)
        );
        assertThat(keywordSearch.getTotalElements()).isEqualTo(1);
        assertThat(keywordSearch.getContent().get(0).getTitle()).isEqualTo("Task Beta");
    }

    @Test
    @DisplayName("Should aggregate metric counts accurately for user dashboard")
    void testMetricCounts() {
        UUID userId = UUID.randomUUID();
        Instant past = Instant.now().minusSeconds(7200);
        Instant future = Instant.now().plusSeconds(7200);

        // TODO task (overdue)
        entityManager.persist(TaskEntity.builder()
                .userId(userId).title("T1").status(TaskStatus.TODO).priority(TaskPriority.HIGH).dueDate(past).deleted(false).build());

        // IN_PROGRESS task (not overdue, urgent priority)
        entityManager.persist(TaskEntity.builder()
                .userId(userId).title("T2").status(TaskStatus.IN_PROGRESS).priority(TaskPriority.URGENT).dueDate(future).deleted(false).build());

        // COMPLETED task (past due date, but completed so not counted as overdue)
        entityManager.persist(TaskEntity.builder()
                .userId(userId).title("T3").status(TaskStatus.COMPLETED).priority(TaskPriority.LOW).dueDate(past).deleted(false).build());

        // Soft-deleted task (must be completely ignored)
        entityManager.persist(TaskEntity.builder()
                .userId(userId).title("T4").status(TaskStatus.TODO).priority(TaskPriority.HIGH).dueDate(past).deleted(true).build());

        entityManager.flush();

        long total = taskRepository.countByUserIdAndDeletedFalse(userId);
        assertThat(total).isEqualTo(3);

        long todoCount = taskRepository.countByUserIdAndStatusAndDeletedFalse(userId, TaskStatus.TODO);
        assertThat(todoCount).isEqualTo(1);

        long inProgressCount = taskRepository.countByUserIdAndStatusAndDeletedFalse(userId, TaskStatus.IN_PROGRESS);
        assertThat(inProgressCount).isEqualTo(1);

        long completedCount = taskRepository.countByUserIdAndStatusAndDeletedFalse(userId, TaskStatus.COMPLETED);
        assertThat(completedCount).isEqualTo(1);

        long highPriorityCount = taskRepository.countByUserIdAndPriorityInAndDeletedFalse(
                userId, List.of(TaskPriority.HIGH, TaskPriority.URGENT)
        );
        assertThat(highPriorityCount).isEqualTo(2);

        long overdueCount = taskRepository.countOverdueTasks(userId, Instant.now());
        assertThat(overdueCount).isEqualTo(1);
    }
}

