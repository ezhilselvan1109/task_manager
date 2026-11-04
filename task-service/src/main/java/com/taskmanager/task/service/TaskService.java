package com.taskmanager.task.service;

import com.taskmanager.task.domain.TaskPriority;
import com.taskmanager.task.domain.TaskStatus;
import com.taskmanager.task.dto.request.ChangeTaskStatusRequest;
import com.taskmanager.task.dto.request.CreateTaskRequest;
import com.taskmanager.task.dto.request.UpdateTaskRequest;
import com.taskmanager.task.dto.response.TaskMetricsResponse;
import com.taskmanager.task.dto.response.TaskPageResponse;
import com.taskmanager.task.dto.response.TaskResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service interface defining task lifecycle operations, filtering, metrics, and outbox event creation.
 */
public interface TaskService {

    TaskResponse createTask(CreateTaskRequest request, UUID userId, String correlationId);

    TaskResponse getTaskById(UUID taskId, UUID userId);

    TaskResponse updateTask(UUID taskId, UpdateTaskRequest request, UUID userId, String correlationId);

    TaskResponse changeStatus(UUID taskId, ChangeTaskStatusRequest request, UUID userId, String correlationId);

    void deleteTask(UUID taskId, UUID userId, String correlationId);

    TaskPageResponse listTasks(UUID userId, String query, List<TaskStatus> statuses,
                               List<TaskPriority> priorities, Instant dueFrom, Instant dueTo,
                               Pageable pageable);

    TaskMetricsResponse getMetrics(UUID userId);
}

