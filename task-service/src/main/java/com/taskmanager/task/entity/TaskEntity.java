package com.taskmanager.task.entity;

import com.taskmanager.task.domain.TaskPriority;
import com.taskmanager.task.domain.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing tasks in task_db.
 * Conforms to DOC-DB-004 and DOC-DEV-005.
 */
@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_tasks_user_active", columnList = "user_id"),
        @Index(name = "idx_tasks_user_status", columnList = "user_id, status"),
        @Index(name = "idx_tasks_user_priority", columnList = "user_id, priority"),
        @Index(name = "idx_tasks_user_due_date", columnList = "user_id, due_date")
})
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public TaskEntity() {
    }

    public TaskEntity(UUID id, UUID userId, String title, String description, TaskStatus status,
                      TaskPriority priority, Instant dueDate, boolean deleted, Instant deletedAt,
                      Long version, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.status = status != null ? status : TaskStatus.TODO;
        this.priority = priority != null ? priority : TaskPriority.MEDIUM;
        this.dueDate = dueDate;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void markDeleted() {
        this.deleted = true;
        this.deletedAt = Instant.now();
    }

    public boolean isOverdue() {
        return dueDate != null
                && dueDate.isBefore(Instant.now())
                && status != TaskStatus.COMPLETED
                && status != TaskStatus.CANCELLED;
    }

    public static TaskEntityBuilder builder() {
        return new TaskEntityBuilder();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public Instant getDueDate() {
        return dueDate;
    }

    public void setDueDate(Instant dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class TaskEntityBuilder {
        private UUID id;
        private UUID userId;
        private String title;
        private String description;
        private TaskStatus status = TaskStatus.TODO;
        private TaskPriority priority = TaskPriority.MEDIUM;
        private Instant dueDate;
        private boolean deleted = false;
        private Instant deletedAt;
        private Long version;
        private Instant createdAt;
        private Instant updatedAt;

        TaskEntityBuilder() {
        }

        public TaskEntityBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public TaskEntityBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public TaskEntityBuilder title(String title) {
            this.title = title;
            return this;
        }

        public TaskEntityBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TaskEntityBuilder status(TaskStatus status) {
            this.status = status;
            return this;
        }

        public TaskEntityBuilder priority(TaskPriority priority) {
            this.priority = priority;
            return this;
        }

        public TaskEntityBuilder dueDate(Instant dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public TaskEntityBuilder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public TaskEntityBuilder deletedAt(Instant deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public TaskEntityBuilder version(Long version) {
            this.version = version;
            return this;
        }

        public TaskEntityBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TaskEntityBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public TaskEntity build() {
            return new TaskEntity(id, userId, title, description, status, priority, dueDate, deleted, deletedAt, version, createdAt, updatedAt);
        }
    }
}

