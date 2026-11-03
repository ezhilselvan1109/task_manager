# API Testing Strategy

**Document Identifier:** DOC-TEST-007  
**Traceability References:** DOC-API-003, DOC-API-005, DOC-API-007, DOC-TEST-001  
**Harness:** Spring MockMvc (`@WebMvcTest`), REST-Assured  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the REST API testing methodology across the Task Management platform. API slice tests validate HTTP request validation, JSON serialization, security header evaluation, status codes, and RFC 7807 problem details formatting without loading full server contexts.

---

## 2. API Slice Test Implementation (`TaskControllerTest.java`)

```java
@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security for isolated controller testing
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    @DisplayName("POST /api/v1/tasks returns 201 Created on valid input")
    void createTask_ValidInput_Returns201Created() throws Exception {
        UUID userId = UUID.randomUUID();
        CreateTaskRequest request = new CreateTaskRequest("Deliverable 1", "Desc", TaskPriority.HIGH, null);
        TaskResponse response = new TaskResponse(UUID.randomUUID(), userId, "Deliverable 1", "Desc",
                TaskStatus.TODO, TaskPriority.HIGH, null, Instant.now(), Instant.now());

        when(taskService.createTask(any(CreateTaskRequest.class), eq(userId))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.title").value("Deliverable 1"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    @DisplayName("POST /api/v1/tasks returns 400 Bad Request on blank title")
    void createTask_BlankTitle_Returns400ProblemDetail() throws Exception {
        UUID userId = UUID.randomUUID();
        CreateTaskRequest invalidRequest = new CreateTaskRequest("", "Desc", TaskPriority.HIGH, null);

        mockMvc.perform(post("/api/v1/tasks")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));
    }
}
```

