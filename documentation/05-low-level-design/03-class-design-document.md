# Class Design Document: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-LLD-003 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Software Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-LLD-001`, `DOC-LLD-002` |

---

## 1. Task Service Class Design

```mermaid
classDiagram
    class TaskEntity {
        -UUID id
        -UUID userId
        -String title
        -String description
        -Status status
        -Priority priority
        -Instant dueDate
        -boolean isDeleted
        -Instant deletedAt
        -Instant createdAt
        -Instant updatedAt
        +onCreate()
        +onUpdate()
    }

    class OutboxEventEntity {
        -UUID id
        -String aggregateId
        -String eventType
        -String payload
        -boolean processed
        -Instant createdAt
        -Instant processedAt
    }

    class Status {
        <<enumeration>>
        TODO
        IN_PROGRESS
        COMPLETED
        CANCELLED
    }

    class Priority {
        <<enumeration>>
        LOW
        MEDIUM
        HIGH
        URGENT
    }

    class TaskController {
        -TaskService taskService
        +createTask(CreateTaskRequest) TaskResponse
        +getTaskById(UUID) TaskResponse
        +updateTask(UUID, UpdateTaskRequest) TaskResponse
        +updateStatus(UUID, UpdateTaskStatusRequest) TaskResponse
        +updatePriority(UUID, UpdateTaskPriorityRequest) TaskResponse
        +deleteTask(UUID) void
        +listTasks(TaskQueryCriteria, Pageable) PageResponse~TaskResponse~
    }

    class TaskService {
        <<interface>>
        +createTask(CreateTaskRequest) TaskResponse
        +getTaskById(UUID) TaskResponse
        +updateTask(UUID, UpdateTaskRequest) TaskResponse
        +updateStatus(UUID, Status) TaskResponse
        +updatePriority(UUID, Priority) TaskResponse
        +deleteTask(UUID) void
        +listTasks(TaskQueryCriteria, Pageable) PageResponse~TaskResponse~
    }

    class TaskServiceImpl {
        -TaskRepository taskRepo
        -OutboxEventRepository outboxRepo
        -TaskMapper mapper
        -ObjectMapper objectMapper
    }

    class OutboxPollerService {
        -OutboxEventRepository outboxRepo
        -KafkaEventPublisher kafkaPublisher
        +pollAndPublishEvents() void
    }

    TaskEntity --> Status
    TaskEntity --> Priority
    TaskController --> TaskService
    TaskService <|.. TaskServiceImpl
    TaskServiceImpl --> TaskEntity
    TaskServiceImpl --> OutboxEventEntity
    OutboxPollerService --> OutboxEventEntity
```

---

## 2. User Service Class Design

```mermaid
classDiagram
    class UserEntity {
        -UUID id
        -String email
        -String passwordHash
        -String fullName
        -UserStatus status
        -Instant createdAt
        -Instant updatedAt
    }

    class RefreshTokenEntity {
        -UUID id
        -UUID userId
        -String token
        -Instant expiresAt
        -boolean revoked
    }

    class AuthController {
        -AuthService authService
        +register(RegisterRequest) AuthResponse
        +login(LoginRequest) AuthResponse
        +refreshToken(RefreshTokenRequest) AuthResponse
    }

    class AuthService {
        <<interface>>
        +register(RegisterRequest) AuthResponse
        +login(LoginRequest) AuthResponse
        +refreshToken(RefreshTokenRequest) AuthResponse
    }

    class AuthServiceImpl {
        -UserRepository userRepo
        -RefreshTokenRepository tokenRepo
        -PasswordEncoder passwordEncoder
        -JwtTokenProvider jwtProvider
    }

    AuthController --> AuthService
    AuthService <|.. AuthServiceImpl
    AuthServiceImpl --> UserEntity
    AuthServiceImpl --> RefreshTokenEntity
```

---

## 3. Notification Service Class Design

```mermaid
classDiagram
    class NotificationEntity {
        -UUID id
        -UUID userId
        -String eventType
        -String message
        -boolean read
        -Instant readAt
        -Instant createdAt
    }

    class ConsumedEventEntity {
        -UUID eventId
        -Instant consumedAt
    }

    class NotificationEventListener {
        -NotificationService notificationService
        +handleTaskEvent(ConsumerRecord) void
    }

    class NotificationService {
        <<interface>>
        +processEvent(TaskEventEnvelope) void
        +listNotifications(UUID, Pageable) NotificationPageResponse
        +markAsRead(UUID, UUID) void
        +markAllAsRead(UUID) void
    }

    class NotificationServiceImpl {
        -NotificationRepository notificationRepo
        -ConsumedEventRepository consumedEventRepo
    }

    NotificationEventListener --> NotificationService
    NotificationService <|.. NotificationServiceImpl
    NotificationServiceImpl --> NotificationEntity
    NotificationServiceImpl --> ConsumedEventEntity
```

---

## 4. Review Checklist
- [x] Complete class diagrams and interface specifications for Task, User, and Notification services.
- [x] Enums, entities, repositories, services, controllers, and listeners mapped.
- [x] Clean architecture dependency rules verified (high-level services depend on abstractions).

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Software Architect | Initial baseline Class Design document. |

