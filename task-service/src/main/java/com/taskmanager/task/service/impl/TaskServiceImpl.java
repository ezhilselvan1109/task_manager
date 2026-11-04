package com.taskmanager.task.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.common.event.TaskEventEnvelope;
import com.taskmanager.common.event.TaskEventType;
import com.taskmanager.task.domain.TaskPriority;
import com.taskmanager.task.domain.TaskStateMachine;
import com.taskmanager.task.domain.TaskStatus;
import com.taskmanager.task.dto.event.TaskEventPayload;
import com.taskmanager.task.dto.request.ChangeTaskStatusRequest;
import com.taskmanager.task.dto.request.CreateTaskRequest;
import com.taskmanager.task.dto.request.UpdateTaskRequest;
import com.taskmanager.task.dto.response.TaskMetricsResponse;
import com.taskmanager.task.dto.response.TaskPageResponse;
import com.taskmanager.task.dto.response.TaskResponse;
import com.taskmanager.task.entity.OutboxEventEntity;
import com.taskmanager.task.entity.TaskEntity;
import com.taskmanager.task.exception.EventSerializationException;
import com.taskmanager.task.exception.ResourceNotFoundException;
import com.taskmanager.task.mapper.TaskMapper;
import com.taskmanager.task.repository.OutboxEventRepository;
import com.taskmanager.task.repository.TaskRepository;
import com.taskmanager.task.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Production implementation of TaskService.
 * Coordinates domain persistence, state machine transitions, anti-enumeration scoping,
 * and Transactional Outbox event serialization in atomic local transactions.
 */
@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final OutboxEventRepository outboxRepository;
    private final TaskMapper taskMapper;
    private final ObjectMapper objectMapper;

    public TaskServiceImpl(TaskRepository taskRepository,
                           OutboxEventRepository outboxRepository,
                           TaskMapper taskMapper,
                           ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.outboxRepository = outboxRepository;
        this.taskMapper = taskMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, UUID userId, String correlationId) {
        TaskEntity task = TaskEntity.builder()
                .userId(userId)
                .title(request.title().trim())
                .description(request.description())
                .status(TaskStatus.TODO)
                .priority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM)
                .dueDate(request.dueDate())
                .deleted(false)
                .build();

        TaskEntity savedTask = taskRepository.save(task);

        // Transactional Outbox: atomic write with task creation in same local ACID transaction
        saveOutboxEvent(TaskEventType.TASK_CREATED.name(), savedTask, correlationId);

        log.info("Task created successfully: ID {} for User {}", savedTask.getId(), userId);
        return taskMapper.toResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID taskId, UUID userId) {
        TaskEntity task = findTaskForOwner(taskId, userId);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request, UUID userId, String correlationId) {
        TaskEntity task = findTaskForOwner(taskId, userId);

        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        task.setDueDate(request.dueDate());

        TaskEntity updatedTask = taskRepository.save(task);
        saveOutboxEvent(TaskEventType.TASK_UPDATED.name(), updatedTask, correlationId);

        log.info("Task updated successfully: ID {}", taskId);
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse changeStatus(UUID taskId, ChangeTaskStatusRequest request, UUID userId, String correlationId) {
        TaskEntity task = findTaskForOwner(taskId, userId);

        TaskStateMachine.validateTransition(task.getStatus(), request.status());

        if (task.getStatus() == request.status()) {
            return taskMapper.toResponse(task);
        }

        task.setStatus(request.status());
        TaskEntity savedTask = taskRepository.save(task);

        String eventType = (request.status() == TaskStatus.COMPLETED)
                ? TaskEventType.TASK_COMPLETED.name()
                : TaskEventType.TASK_UPDATED.name();

        saveOutboxEvent(eventType, savedTask, correlationId);

        log.info("Task status changed: ID {} from {} to {}", taskId, task.getStatus(), request.status());
        return taskMapper.toResponse(savedTask);
    }

    @Override
    @Transactional
    public void deleteTask(UUID taskId, UUID userId, String correlationId) {
        TaskEntity task = findTaskForOwner(taskId, userId);

        task.markDeleted();
        taskRepository.save(task);

        saveOutboxEvent(TaskEventType.TASK_DELETED.name(), task, correlationId);
        log.info("Task soft-deleted: ID {}", taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskPageResponse listTasks(UUID userId, String query, List<TaskStatus> statuses,
                                      List<TaskPriority> priorities, Instant dueFrom, Instant dueTo,
                                      Pageable pageable) {
        Page<TaskEntity> taskPage = taskRepository.findTasksWithFilters(
                userId, query, statuses, priorities, dueFrom, dueTo, pageable
        );

        List<TaskResponse> content = taskPage.getContent().stream()
                .map(taskMapper::toResponse)
                .toList();

        return new TaskPageResponse(
                content,
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TaskMetricsResponse getMetrics(UUID userId) {
        long totalTasks = taskRepository.countByUserIdAndDeletedFalse(userId);
        long todoCount = taskRepository.countByUserIdAndStatusAndDeletedFalse(userId, TaskStatus.TODO);
        long inProgressCount = taskRepository.countByUserIdAndStatusAndDeletedFalse(userId, TaskStatus.IN_PROGRESS);
        long completedCount = taskRepository.countByUserIdAndStatusAndDeletedFalse(userId, TaskStatus.COMPLETED);
        long cancelledCount = taskRepository.countByUserIdAndStatusAndDeletedFalse(userId, TaskStatus.CANCELLED);
        long highPriorityCount = taskRepository.countByUserIdAndPriorityInAndDeletedFalse(
                userId, List.of(TaskPriority.HIGH, TaskPriority.URGENT)
        );
        long overdueCount = taskRepository.countOverdueTasks(userId, Instant.now());

        return new TaskMetricsResponse(
                totalTasks, todoCount, inProgressCount, completedCount, cancelledCount, highPriorityCount, overdueCount
        );
    }

    private TaskEntity findTaskForOwner(UUID taskId, UUID userId) {
        return taskRepository.findByIdAndUserIdAndDeletedFalse(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
    }

    private void saveOutboxEvent(String eventType, TaskEntity task, String correlationId) {
        try {
            String resolvedCorrelationId = (correlationId != null && !correlationId.isBlank())
                    ? correlationId
                    : UUID.randomUUID().toString();

            TaskEventPayload payload = new TaskEventPayload(
                    task.getId(),
                    task.getUserId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus(),
                    task.getPriority(),
                    task.getDueDate()
            );

            TaskEventEnvelope<TaskEventPayload> envelope = new TaskEventEnvelope<>(
                    UUID.randomUUID(),
                    eventType,
                    "1.0",
                    task.getId(),
                    task.getUserId(),
                    "task-service",
                    Instant.now(),
                    resolvedCorrelationId,
                    payload
            );

            OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                    .id(envelope.eventId())
                    .aggregateId(task.getId().toString())
                    .eventType(eventType)
                    .payload(objectMapper.writeValueAsString(envelope))
                    .processed(false)
                    .status("PENDING")
                    .retryCount(0)
                    .createdAt(Instant.now())
                    .build();

            outboxRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event payload for task ID: {}", task.getId(), e);
            throw new EventSerializationException("Failed to serialize outbox event payload", e);
        }
    }
}

