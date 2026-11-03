# Unit Testing Strategy

**Document Identifier:** DOC-TEST-005  
**Traceability References:** DOC-AUTO-007, DOC-TEST-001  
**Frameworks:** JUnit 5 (Jupiter), Mockito 5.x, AssertJ 3.x  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the unit testing architecture and guidelines across all microservices. Unit tests isolate domain logic from frameworks, datastores, and network boundaries, executing in milliseconds to provide instant developer feedback.

---

## 2. Unit Testing Conventions and Rules

1. **Pure Java Execution**: Unit tests must not load the Spring ApplicationContext (`@SpringBootTest` is forbidden in unit tests). Use `@ExtendWith(MockitoExtension.class)`.
2. **Execution Speed Target**: Every unit test must execute in **less than 10 milliseconds**.
3. **AssertJ Fluent Assertions**: Use AssertJ (`assertThat(...)`) exclusively for assertions. Avoid legacy JUnit `assertEquals`.
4. **Behavior Verification Pattern**: Structure test methods with clear **Given-When-Then** blocks.

---

## 3. Reference Unit Test Implementation (`TaskServiceTest.java`)

```java
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private OutboxEventRepository outboxRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    @DisplayName("Should throw ResourceNotFoundException when querying another user's task (Anti-Enumeration)")
    void getTaskById_WhenNotOwnedByUser_ThrowsResourceNotFoundException() {
        // Given
        UUID taskId = UUID.randomUUID();
        UUID callerUserId = UUID.randomUUID();

        // Repository returns empty because query enforces user_id matching
        when(taskRepository.findByIdAndUserIdAndDeletedFalse(taskId, callerUserId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.getTaskById(taskId, callerUserId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(taskId.toString());

        verify(taskRepository, times(1)).findByIdAndUserIdAndDeletedFalse(taskId, callerUserId);
        verifyNoInteractions(taskMapper);
    }

    @Test
    @DisplayName("Should save task and persist outbox event atomically on creation")
    void createTask_ValidRequest_SavesTaskAndOutboxEvent() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        CreateTaskRequest request = new CreateTaskRequest("Deliverable A", "Description", TaskPriority.HIGH, null);
        TaskEntity savedEntity = TaskEntity.builder().id(UUID.randomUUID()).userId(userId).title("Deliverable A").build();
        TaskResponse expectedResponse = new TaskResponse(savedEntity.getId(), userId, "Deliverable A", "Description",
                TaskStatus.TODO, TaskPriority.HIGH, null, Instant.now(), Instant.now());

        when(taskRepository.save(any(TaskEntity.class))).thenReturn(savedEntity);
        when(taskMapper.toResponse(savedEntity)).thenReturn(expectedResponse);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventType\":\"TASK_CREATED\"}");

        // When
        TaskResponse response = taskService.createTask(request, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(savedEntity.getId());
        verify(taskRepository, times(1)).save(any(TaskEntity.class));
        verify(outboxRepository, times(1)).save(any(OutboxEventEntity.class));
    }
}
```

