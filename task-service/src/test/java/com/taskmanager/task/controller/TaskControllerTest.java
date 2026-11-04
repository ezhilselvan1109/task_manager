package com.taskmanager.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.taskmanager.task.exception.GlobalExceptionHandler;
import com.taskmanager.task.exception.InvalidStatusTransitionException;
import com.taskmanager.task.exception.ResourceNotFoundException;
import com.taskmanager.task.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class)
@Import(GlobalExceptionHandler.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    @DisplayName("POST /api/v1/tasks - Should create task and return 201 Created with Location header")
    void testCreateTaskSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        CreateTaskRequest request = new CreateTaskRequest("New Task", "Description", TaskPriority.HIGH, Instant.now().plusSeconds(3600));

        TaskResponse response = new TaskResponse(
                taskId, userId, "New Task", "Description", TaskStatus.TODO, TaskPriority.HIGH, request.dueDate(), false, Instant.now(), Instant.now()
        );

        when(taskService.createTask(any(CreateTaskRequest.class), eq(userId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString())
                        .header(CorrelationConstants.HEADER_NAME, "corr-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/tasks/" + taskId))
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    @DisplayName("POST /api/v1/tasks - Should return 401 when X-User-Id header is missing")
    void testCreateTaskMissingUserId() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest("Task", null, TaskPriority.LOW, null);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("POST /api/v1/tasks - Should return 400 when title is blank")
    void testCreateTaskValidationFailure() throws Exception {
        UUID userId = UUID.randomUUID();
        CreateTaskRequest request = new CreateTaskRequest("   ", null, TaskPriority.LOW, null);

        mockMvc.perform(post("/api/v1/tasks")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ERR_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{id} - Should return 200 OK for owner")
    void testGetTaskByIdSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        TaskResponse response = new TaskResponse(
                taskId, userId, "Existing Task", null, TaskStatus.TODO, TaskPriority.MEDIUM, null, false, Instant.now(), Instant.now()
        );

        when(taskService.getTaskById(taskId, userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/tasks/{id}", taskId)
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Existing Task"));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{id} - Should return 404 Not Found on cross-user lookup (anti-enumeration)")
    void testGetTaskByIdCrossUser404() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        when(taskService.getTaskById(taskId, userId))
                .thenThrow(new ResourceNotFoundException("Task not found with ID: " + taskId));

        mockMvc.perform(get("/api/v1/tasks/{id}", taskId)
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ERR_RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("PUT /api/v1/tasks/{id} - Should return 200 OK on valid update")
    void testUpdateTaskSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UpdateTaskRequest request = new UpdateTaskRequest("Updated Title", "Updated Desc", TaskPriority.HIGH, null);

        TaskResponse response = new TaskResponse(
                taskId, userId, "Updated Title", "Updated Desc", TaskStatus.TODO, TaskPriority.HIGH, null, false, Instant.now(), Instant.now()
        );

        when(taskService.updateTask(eq(taskId), any(UpdateTaskRequest.class), eq(userId), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @DisplayName("PATCH /api/v1/tasks/{id}/status - Should return 200 OK on valid transition")
    void testChangeStatusSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        ChangeTaskStatusRequest request = new ChangeTaskStatusRequest(TaskStatus.IN_PROGRESS);

        TaskResponse response = new TaskResponse(
                taskId, userId, "Task", null, TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, null, false, Instant.now(), Instant.now()
        );

        when(taskService.changeStatus(eq(taskId), any(ChangeTaskStatusRequest.class), eq(userId), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tasks/{id}/status", taskId)
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("PATCH /api/v1/tasks/{id}/status - Should return 400 Bad Request on prohibited transition")
    void testChangeStatusProhibitedTransition() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        ChangeTaskStatusRequest request = new ChangeTaskStatusRequest(TaskStatus.CANCELLED);

        when(taskService.changeStatus(eq(taskId), any(ChangeTaskStatusRequest.class), eq(userId), any()))
                .thenThrow(new InvalidStatusTransitionException("Cannot transition task from COMPLETED to CANCELLED"));

        mockMvc.perform(patch("/api/v1/tasks/{id}/status", taskId)
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ERR_INVALID_STATUS"));
    }

    @Test
    @DisplayName("PATCH /api/v1/tasks/{id}/complete - Should return 200 OK")
    void testCompleteTaskSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        TaskResponse response = new TaskResponse(
                taskId, userId, "Task", null, TaskStatus.COMPLETED, TaskPriority.MEDIUM, null, false, Instant.now(), Instant.now()
        );

        when(taskService.changeStatus(eq(taskId), any(ChangeTaskStatusRequest.class), eq(userId), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tasks/{id}/complete", taskId)
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("DELETE /api/v1/tasks/{id} - Should return 204 No Content")
    void testDeleteTaskSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        doNothing().when(taskService).deleteTask(eq(taskId), eq(userId), any());

        mockMvc.perform(delete("/api/v1/tasks/{id}", taskId)
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/tasks - Should return 200 OK with paginated list")
    void testListTasksSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        TaskPageResponse pageResponse = new TaskPageResponse(
                List.of(new TaskResponse(UUID.randomUUID(), userId, "T1", null, TaskStatus.TODO, TaskPriority.LOW, null, false, Instant.now(), Instant.now())),
                0, 20, 1, 1, true
        );

        when(taskService.listTasks(eq(userId), any(), any(), any(), any(), any(), any(PageRequest.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/tasks")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("T1"));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/metrics/summary - Should return 200 OK with dashboard metrics")
    void testGetMetricsSummary() throws Exception {
        UUID userId = UUID.randomUUID();
        TaskMetricsResponse metrics = new TaskMetricsResponse(24, 10, 6, 7, 1, 5, 2);

        when(taskService.getMetrics(userId)).thenReturn(metrics);

        mockMvc.perform(get("/api/v1/tasks/metrics/summary")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(24))
                .andExpect(jsonPath("$.todoCount").value(10))
                .andExpect(jsonPath("$.overdueCount").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/tasks - Should return 401 when X-User-Id header is invalid UUID")
    void testListTasksInvalidUserIdFormat() throws Exception {
        mockMvc.perform(get("/api/v1/tasks")
                        .header(SecurityConstants.HEADER_USER_ID, "not-a-valid-uuid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid user identity header format"));
    }

    @Test
    @DisplayName("GET /api/v1/tasks - Should return 401 when X-User-Id header is blank whitespace")
    void testListTasksBlankUserId() throws Exception {
        mockMvc.perform(get("/api/v1/tasks")
                        .header(SecurityConstants.HEADER_USER_ID, "   "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Missing trusted user identity header"));
    }

    @Test
    @DisplayName("GET /api/v1/tasks - Should support custom sort direction asc, priority field, and handle out-of-bounds page/size")
    void testListTasksWithCustomSortAndBounds() throws Exception {
        UUID userId = UUID.randomUUID();
        TaskPageResponse pageResponse = new TaskPageResponse(List.of(), 0, 100, 0, 0, true);

        when(taskService.listTasks(eq(userId), any(), any(), any(), any(), any(), any(PageRequest.class)))
                .thenReturn(pageResponse);

        // Test sort=title,asc with bounds testing (negative page, oversized size)
        mockMvc.perform(get("/api/v1/tasks")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString())
                        .param("sort", "title,asc")
                        .param("page", "-2")
                        .param("size", "250"))
                .andExpect(status().isOk());

        // Test sort=priority,desc with undersized size
        mockMvc.perform(get("/api/v1/tasks")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString())
                        .param("sort", "priority,desc")
                        .param("size", "-5"))
                .andExpect(status().isOk());

        // Test sort with unrecognized field (falls back to default sort)
        mockMvc.perform(get("/api/v1/tasks")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString())
                        .param("sort", "unknownField,asc"))
                .andExpect(status().isOk());
    }
}

