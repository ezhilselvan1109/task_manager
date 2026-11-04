package com.taskmanager.task.domain;

import com.taskmanager.task.entity.TaskEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaskEntityTest {

    @Test
    @DisplayName("Should instantiate TaskEntity with default values and builder")
    void testTaskEntityBuilderAndDefaults() {
        UUID userId = UUID.randomUUID();
        Instant dueDate = Instant.now().plusSeconds(86400);

        TaskEntity task = TaskEntity.builder()
                .userId(userId)
                .title("Complete TDD implementation")
                .description("Write failing tests before code")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(dueDate)
                .deleted(false)
                .build();

        assertThat(task.getUserId()).isEqualTo(userId);
        assertThat(task.getTitle()).isEqualTo("Complete TDD implementation");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(task.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(task.isDeleted()).isFalse();
        assertThat(task.getDueDate()).isEqualTo(dueDate);
    }

    @Test
    @DisplayName("Should execute soft deletion helper method")
    void testSoftDeletion() {
        TaskEntity task = TaskEntity.builder()
                .userId(UUID.randomUUID())
                .title("Task to delete")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .deleted(false)
                .build();

        assertThat(task.isDeleted()).isFalse();
        assertThat(task.getDeletedAt()).isNull();

        task.markDeleted();

        assertThat(task.isDeleted()).isTrue();
        assertThat(task.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should evaluate overdue state dynamically")
    void testIsOverdueEvaluation() {
        Instant past = Instant.now().minusSeconds(3600);
        Instant future = Instant.now().plusSeconds(3600);

        // Past due date with TODO status -> overdue
        TaskEntity overdueTask = TaskEntity.builder()
                .status(TaskStatus.TODO)
                .dueDate(past)
                .deleted(false)
                .build();
        assertThat(overdueTask.isOverdue()).isTrue();

        // Past due date with COMPLETED status -> NOT overdue
        TaskEntity completedTask = TaskEntity.builder()
                .status(TaskStatus.COMPLETED)
                .dueDate(past)
                .deleted(false)
                .build();
        assertThat(completedTask.isOverdue()).isFalse();

        // Past due date with CANCELLED status -> NOT overdue
        TaskEntity cancelledTask = TaskEntity.builder()
                .status(TaskStatus.CANCELLED)
                .dueDate(past)
                .deleted(false)
                .build();
        assertThat(cancelledTask.isOverdue()).isFalse();

        // Future due date -> NOT overdue
        TaskEntity futureTask = TaskEntity.builder()
                .status(TaskStatus.IN_PROGRESS)
                .dueDate(future)
                .deleted(false)
                .build();
        assertThat(futureTask.isOverdue()).isFalse();

        // Null due date -> NOT overdue
        TaskEntity noDueDateTask = TaskEntity.builder()
                .status(TaskStatus.TODO)
                .dueDate(null)
                .deleted(false)
                .build();
        assertThat(noDueDateTask.isOverdue()).isFalse();
    }
}

