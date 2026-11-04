package com.taskmanager.task.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.task.domain.TaskPriority;
import com.taskmanager.task.domain.TaskStatus;
import com.taskmanager.task.dto.request.ChangeTaskStatusRequest;
import com.taskmanager.task.dto.request.CreateTaskRequest;
import com.taskmanager.task.dto.request.UpdateTaskRequest;
import com.taskmanager.task.dto.response.TaskMetricsResponse;
import com.taskmanager.task.dto.response.TaskPageResponse;
import com.taskmanager.task.dto.response.TaskResponse;
import com.taskmanager.task.entity.OutboxEventEntity;
import com.taskmanager.task.entity.TaskEntity;
import com.taskmanager.task.exception.InvalidStatusTransitionException;
import com.taskmanager.task.exception.ResourceNotFoundException;
import com.taskmanager.task.mapper.TaskMapper;
import com.taskmanager.task.repository.OutboxEventRepository;
import com.taskmanager.task.repository.TaskRepository;
import com.taskmanager.task.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private OutboxEventRepository outboxRepository;

    @Mock
    private TaskMapper taskMapper;

    private ObjectMapper objectMapper;
    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        taskService = new TaskServiceImpl(taskRepository, outboxRepository, taskMapper, objectMapper);
    }

    @Test
    @DisplayName("Should create task and record TASK_CREATED outbox event atomically in local transaction")
    void testCreateTaskSuccess() {
        UUID userId = UUID.randomUUID();
        CreateTaskRequest request = new CreateTaskRequest(
                "Design Architecture", "Microservices design", TaskPriority.HIGH, Instant.now().plusSeconds(3600)
        );

        UUID taskId = UUID.randomUUID();
        TaskEntity savedTask = TaskEntity.builder()
                .id(taskId)
                .userId(userId)
                .title(request.title())
                .description(request.description())
                .status(TaskStatus.TODO)
                .priority(request.priority())
                .dueDate(request.dueDate())
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(taskRepository.save(any(TaskEntity.class))).thenReturn(savedTask);

        TaskResponse expectedResponse = new TaskResponse(
                taskId, userId, request.title(), request.description(),
                TaskStatus.TODO, TaskPriority.HIGH, request.dueDate(), false,
                savedTask.getCreatedAt(), savedTask.getUpdatedAt()
        );
        when(taskMapper.toResponse(savedTask)).thenReturn(expectedResponse);

        TaskResponse response = taskService.createTask(request, userId, "test-correlation-id");

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(taskId);

        // Verify task persisted
        verify(taskRepository).save(any(TaskEntity.class));

        // Verify outbox record created
        ArgumentCaptor<OutboxEventEntity> outboxCaptor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxRepository).save(outboxCaptor.capture());

        OutboxEventEntity capturedEvent = outboxCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo("TASK_CREATED");
        assertThat(capturedEvent.getAggregateId()).isEqualTo(taskId.toString());
        assertThat(capturedEvent.isProcessed()).isFalse();
        assertThat(capturedEvent.getStatus()).isEqualTo("PENDING");
        assertThat(capturedEvent.getPayload()).contains("TASK_CREATED");
        assertThat(capturedEvent.getPayload()).contains("test-correlation-id");
    }

    @Test
    @DisplayName("Should retrieve task for owner and map to response")
    void testGetTaskByIdSuccess() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TaskEntity task = TaskEntity.builder()
                .id(taskId).userId(userId).title("Existing Task").status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).deleted(false).build();

        when(taskRepository.findByIdAndUserIdAndDeletedFalse(taskId, userId)).thenReturn(Optional.of(task));

        TaskResponse expected = new TaskResponse(taskId, userId, "Existing Task", null, TaskStatus.TODO, TaskPriority.MEDIUM, null, false, Instant.now(), Instant.now());
        when(taskMapper.toResponse(task)).thenReturn(expected);

        TaskResponse actual = taskService.getTaskById(taskId, userId);
        assertThat(actual).isNotNull();
        assertThat(actual.title()).isEqualTo("Existing Task");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException (404) when task does not exist or belongs to another user")
    void testGetTaskByIdNotFoundAntiEnumeration() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(taskRepository.findByIdAndUserIdAndDeletedFalse(taskId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(taskId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with ID: " + taskId);
    }

    @Test
    @DisplayName("Should update task and record TASK_UPDATED outbox event")
    void testUpdateTaskSuccess() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TaskEntity existing = TaskEntity.builder()
                .id(taskId).userId(userId).title("Old Title").status(TaskStatus.TODO).priority(TaskPriority.LOW).deleted(false).build();

        when(taskRepository.findByIdAndUserIdAndDeletedFalse(taskId, userId)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(existing);

        UpdateTaskRequest updateRequest = new UpdateTaskRequest("New Title", "New Desc", TaskPriority.HIGH, Instant.now().plusSeconds(7200));

        TaskResponse expected = new TaskResponse(taskId, userId, "New Title", "New Desc", TaskStatus.TODO, TaskPriority.HIGH, updateRequest.dueDate(), false, Instant.now(), Instant.now());
        when(taskMapper.toResponse(existing)).thenReturn(expected);

        TaskResponse result = taskService.updateTask(taskId, updateRequest, userId, "corr-id-123");
        assertThat(result.title()).isEqualTo("New Title");

        verify(outboxRepository).save(any(OutboxEventEntity.class));
    }

    @Test
    @DisplayName("Should change task status and emit TASK_COMPLETED when transitioned to COMPLETED")
    void testChangeStatusToCompleted() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TaskEntity existing = TaskEntity.builder()
                .id(taskId).userId(userId).title("Task In Progress").status(TaskStatus.IN_PROGRESS).priority(TaskPriority.HIGH).deleted(false).build();

        when(taskRepository.findByIdAndUserIdAndDeletedFalse(taskId, userId)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(existing);

        TaskResponse expected = new TaskResponse(taskId, userId, "Task In Progress", null, TaskStatus.COMPLETED, TaskPriority.HIGH, null, false, Instant.now(), Instant.now());
        when(taskMapper.toResponse(existing)).thenReturn(expected);

        TaskResponse response = taskService.changeStatus(taskId, new ChangeTaskStatusRequest(TaskStatus.COMPLETED), userId, "corr-456");
        assertThat(response.status()).isEqualTo(TaskStatus.COMPLETED);

        ArgumentCaptor<OutboxEventEntity> outboxCaptor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().getEventType()).isEqualTo("TASK_COMPLETED");
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException and omit outbox save when transition is prohibited")
    void testProhibitedStatusTransitionRejection() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TaskEntity completedTask = TaskEntity.builder()
                .id(taskId).userId(userId).title("Completed Task").status(TaskStatus.COMPLETED).priority(TaskPriority.MEDIUM).deleted(false).build();

        when(taskRepository.findByIdAndUserIdAndDeletedFalse(taskId, userId)).thenReturn(Optional.of(completedTask));

        // COMPLETED -> CANCELLED is prohibited by DOC-BA-007
        assertThatThrownBy(() -> taskService.changeStatus(taskId, new ChangeTaskStatusRequest(TaskStatus.CANCELLED), userId, "corr-789"))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("Cannot transition task from COMPLETED to CANCELLED");

        verify(taskRepository, never()).save(any());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft delete task and emit TASK_DELETED outbox event")
    void testSoftDeleteTask() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TaskEntity activeTask = TaskEntity.builder()
                .id(taskId).userId(userId).title("Task to Delete").status(TaskStatus.TODO).priority(TaskPriority.LOW).deleted(false).build();

        when(taskRepository.findByIdAndUserIdAndDeletedFalse(taskId, userId)).thenReturn(Optional.of(activeTask));

        taskService.deleteTask(taskId, userId, "corr-delete");

        assertThat(activeTask.isDeleted()).isTrue();
        assertThat(activeTask.getDeletedAt()).isNotNull();

        verify(taskRepository).save(activeTask);

        ArgumentCaptor<OutboxEventEntity> outboxCaptor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().getEventType()).isEqualTo("TASK_DELETED");
    }

    @Test
    @DisplayName("Should list and search user tasks with pagination")
    void testListTasks() {
        UUID userId = UUID.randomUUID();
        TaskEntity task = TaskEntity.builder()
                .id(UUID.randomUUID()).userId(userId).title("Searchable").status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).deleted(false).build();

        when(taskRepository.findTasksWithFilters(eq(userId), eq("Search"), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1));

        TaskResponse responseDto = new TaskResponse(task.getId(), userId, "Searchable", null, TaskStatus.TODO, TaskPriority.MEDIUM, null, false, Instant.now(), Instant.now());
        when(taskMapper.toResponse(task)).thenReturn(responseDto);

        TaskPageResponse pageResponse = taskService.listTasks(userId, "Search", null, null, null, null, PageRequest.of(0, 10));

        assertThat(pageResponse.totalElements()).isEqualTo(1);
        assertThat(pageResponse.content().get(0).title()).isEqualTo("Searchable");
    }

    @Test
    @DisplayName("Should aggregate metric counts for user dashboard")
    void testGetMetrics() {
        UUID userId = UUID.randomUUID();

        when(taskRepository.countByUserIdAndDeletedFalse(userId)).thenReturn(10L);
        when(taskRepository.countByUserIdAndStatusAndDeletedFalse(userId, TaskStatus.TODO)).thenReturn(4L);
        when(taskRepository.countByUserIdAndStatusAndDeletedFalse(userId, TaskStatus.IN_PROGRESS)).thenReturn(3L);
        when(taskRepository.countByUserIdAndStatusAndDeletedFalse(userId, TaskStatus.COMPLETED)).thenReturn(2L);
        when(taskRepository.countByUserIdAndStatusAndDeletedFalse(userId, TaskStatus.CANCELLED)).thenReturn(1L);
        when(taskRepository.countByUserIdAndPriorityInAndDeletedFalse(eq(userId), any())).thenReturn(5L);
        when(taskRepository.countOverdueTasks(eq(userId), any())).thenReturn(2L);

        TaskMetricsResponse metrics = taskService.getMetrics(userId);

        assertThat(metrics.totalTasks()).isEqualTo(10L);
        assertThat(metrics.todoCount()).isEqualTo(4L);
        assertThat(metrics.inProgressCount()).isEqualTo(3L);
        assertThat(metrics.completedCount()).isEqualTo(2L);
        assertThat(metrics.cancelledCount()).isEqualTo(1L);
        assertThat(metrics.highPriorityCount()).isEqualTo(5L);
        assertThat(metrics.overdueCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should default priority to MEDIUM and resolve fallback correlation ID when not provided")
    void testCreateTaskWithNullPriorityAndNullCorrelationId() {
        UUID userId = UUID.randomUUID();
        CreateTaskRequest request = new CreateTaskRequest("Default Priority Task", "Desc", null, null);

        TaskEntity savedTask = TaskEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title(request.title())
                .description(request.description())
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .deleted(false)
                .build();

        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> {
            TaskEntity entity = invocation.getArgument(0);
            assertThat(entity.getPriority()).isEqualTo(TaskPriority.MEDIUM);
            return savedTask;
        });

        TaskResponse expected = new TaskResponse(savedTask.getId(), userId, "Default Priority Task", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, null, false, Instant.now(), Instant.now());
        when(taskMapper.toResponse(savedTask)).thenReturn(expected);

        TaskResponse response = taskService.createTask(request, userId, null);

        assertThat(response).isNotNull();
        ArgumentCaptor<OutboxEventEntity> outboxCaptor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().getPayload()).contains("\"correlationId\"");
    }

    @Test
    @DisplayName("Should preserve existing priority when update request priority is null")
    void testUpdateTaskWithNullPriority() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TaskEntity existing = TaskEntity.builder()
                .id(taskId).userId(userId).title("Existing").status(TaskStatus.TODO).priority(TaskPriority.URGENT).deleted(false).build();

        when(taskRepository.findByIdAndUserIdAndDeletedFalse(taskId, userId)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(existing);

        UpdateTaskRequest updateRequest = new UpdateTaskRequest("New Title", "New Desc", null, null);

        TaskResponse expected = new TaskResponse(taskId, userId, "New Title", "New Desc", TaskStatus.TODO, TaskPriority.URGENT, null, false, Instant.now(), Instant.now());
        when(taskMapper.toResponse(existing)).thenReturn(expected);

        TaskResponse result = taskService.updateTask(taskId, updateRequest, userId, "corr-update");
        assertThat(result.priority()).isEqualTo(TaskPriority.URGENT);
        assertThat(existing.getPriority()).isEqualTo(TaskPriority.URGENT);
    }

    @Test
    @DisplayName("Should return early without persisting or outbox write when status is unchanged (no-op)")
    void testChangeStatusNoOp() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TaskEntity existing = TaskEntity.builder()
                .id(taskId).userId(userId).title("Task").status(TaskStatus.IN_PROGRESS).priority(TaskPriority.MEDIUM).deleted(false).build();

        when(taskRepository.findByIdAndUserIdAndDeletedFalse(taskId, userId)).thenReturn(Optional.of(existing));

        TaskResponse expected = new TaskResponse(taskId, userId, "Task", null, TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, null, false, Instant.now(), Instant.now());
        when(taskMapper.toResponse(existing)).thenReturn(expected);

        TaskResponse response = taskService.changeStatus(taskId, new ChangeTaskStatusRequest(TaskStatus.IN_PROGRESS), userId, "corr-noop");
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);

        verify(taskRepository, never()).save(any());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should emit TASK_UPDATED when task is transitioned to CANCELLED")
    void testChangeStatusToCancelledEmitsTaskUpdated() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TaskEntity existing = TaskEntity.builder()
                .id(taskId).userId(userId).title("Task").status(TaskStatus.TODO).priority(TaskPriority.LOW).deleted(false).build();

        when(taskRepository.findByIdAndUserIdAndDeletedFalse(taskId, userId)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(existing);

        TaskResponse expected = new TaskResponse(taskId, userId, "Task", null, TaskStatus.CANCELLED, TaskPriority.LOW, null, false, Instant.now(), Instant.now());
        when(taskMapper.toResponse(existing)).thenReturn(expected);

        TaskResponse response = taskService.changeStatus(taskId, new ChangeTaskStatusRequest(TaskStatus.CANCELLED), userId, "corr-cancel");
        assertThat(response.status()).isEqualTo(TaskStatus.CANCELLED);

        ArgumentCaptor<OutboxEventEntity> outboxCaptor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().getEventType()).isEqualTo("TASK_UPDATED");
    }
}

