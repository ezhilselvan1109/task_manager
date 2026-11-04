package com.taskmanager.task.controller;

import com.taskmanager.common.constants.CorrelationConstants;
import com.taskmanager.common.constants.SecurityConstants;
import com.taskmanager.task.domain.TaskPriority;
import com.taskmanager.task.domain.TaskStatus;
import com.taskmanager.task.dto.request.ChangeTaskStatusRequest;
import com.taskmanager.task.dto.request.CreateTaskRequest;
import com.taskmanager.task.dto.request.UpdateTaskRequest;
import com.taskmanager.task.dto.response.TaskMetricsResponse;
import com.taskmanager.task.dto.response.TaskPageResponse;
import com.taskmanager.task.dto.response.TaskResponse;
import com.taskmanager.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing endpoints for task creation, retrieval, updates, status transitions,
 * search, filtering, and productivity metrics.
 * Conforms to DOC-API-003 and DOC-DEV-005.
 */
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = CorrelationConstants.HEADER_NAME, required = false) String correlationId) {

        UUID userId = extractUserId(userIdHeader);
        TaskResponse response = taskService.createTask(request, userId, correlationId);
        URI location = URI.create("/api/v1/tasks/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable("id") UUID id,
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader) {

        UUID userId = extractUserId(userIdHeader);
        TaskResponse response = taskService.getTaskById(id, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateTaskRequest request,
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = CorrelationConstants.HEADER_NAME, required = false) String correlationId) {

        UUID userId = extractUserId(userIdHeader);
        TaskResponse response = taskService.updateTask(id, request, userId, correlationId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> changeStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ChangeTaskStatusRequest request,
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = CorrelationConstants.HEADER_NAME, required = false) String correlationId) {

        UUID userId = extractUserId(userIdHeader);
        TaskResponse response = taskService.changeStatus(id, request, userId, correlationId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TaskResponse> completeTask(
            @PathVariable("id") UUID id,
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = CorrelationConstants.HEADER_NAME, required = false) String correlationId) {

        UUID userId = extractUserId(userIdHeader);
        TaskResponse response = taskService.changeStatus(id, new ChangeTaskStatusRequest(TaskStatus.COMPLETED), userId, correlationId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable("id") UUID id,
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = CorrelationConstants.HEADER_NAME, required = false) String correlationId) {

        UUID userId = extractUserId(userIdHeader);
        taskService.deleteTask(id, userId, correlationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<TaskPageResponse> listTasks(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "status", required = false) List<TaskStatus> status,
            @RequestParam(value = "priority", required = false) List<TaskPriority> priority,
            @RequestParam(value = "dueFrom", required = false) Instant dueFrom,
            @RequestParam(value = "dueTo", required = false) Instant dueTo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort,
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader) {

        UUID userId = extractUserId(userIdHeader);
        Pageable pageable = parsePageable(page, size, sort);
        TaskPageResponse response = taskService.listTasks(userId, query, status, priority, dueFrom, dueTo, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics/summary")
    public ResponseEntity<TaskMetricsResponse> getMetrics(
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader) {

        UUID userId = extractUserId(userIdHeader);
        TaskMetricsResponse response = taskService.getMetrics(userId);
        return ResponseEntity.ok(response);
    }

    private UUID extractUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing trusted user identity header");
        }
        try {
            return UUID.fromString(userIdHeader.trim());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user identity header format");
        }
    }

    private Pageable parsePageable(int page, int size, String sortParam) {
        int boundedSize = Math.max(1, Math.min(size, 100));
        int boundedPage = Math.max(0, page);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sortParam != null && !sortParam.isBlank()) {
            String[] parts = sortParam.split(",");
            String field = parts[0].trim();
            Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            if (List.of("createdAt", "updatedAt", "dueDate", "priority", "title", "status").contains(field)) {
                sort = Sort.by(direction, field);
            }
        }

        return PageRequest.of(boundedPage, boundedSize, sort);
    }
}

