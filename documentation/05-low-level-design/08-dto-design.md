# DTO Design Document: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-LLD-008 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Software Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-LLD-001`, `DOC-REQ-004` |

---

## 1. DTO Architectural Standards
All Data Transfer Objects (DTOs) in the Task Management System are implemented using modern **Java 21 `record` classes**:
1. **Immutability by Default**: Thread-safe, shallowly immutable representations of API payloads.
2. **Compact Syntax**: Automatic `equals()`, `hashCode()`, and `toString()` implementations.
3. **Explicit Validation**: Jakarta validation annotations (`@NotBlank`, `@Size`, `@NotNull`) decorate record components.

---

## 2. Request DTO Records

### 2.1 Task Service Request Records
```java
package com.chellaa.task_service.dto.request;

import com.chellaa.task_service.entity.Priority;
import com.chellaa.task_service.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateTaskRequest(
    @NotBlank(message = "Task title must not be blank")
    @Size(min = 1, max = 255, message = "Task title must be between 1 and 255 characters")
    String title,

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description,

    Priority priority, // Defaults to MEDIUM in service if null
    Instant dueDate
) {}

public record UpdateTaskRequest(
    @NotBlank(message = "Task title must not be blank")
    @Size(min = 1, max = 255, message = "Task title must be between 1 and 255 characters")
    String title,

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description,

    Priority priority,
    Instant dueDate
) {}

public record UpdateTaskStatusRequest(
    @NotNull(message = "Status is mandatory")
    Status status
) {}

public record UpdateTaskPriorityRequest(
    @NotNull(message = "Priority is mandatory")
    Priority priority
) {}
```

### 2.2 User Service Request Records
```java
package com.chellaa.user_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be a valid email address")
    String email,

    @NotBlank(message = "Password is mandatory")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must be at least 8 characters long, contain 1 uppercase, 1 lowercase, 1 digit, and 1 special character"
    )
    String password,

    @NotBlank(message = "Full name is mandatory")
    @Size(min = 1, max = 100, message = "Full name must be between 1 and 100 characters")
    String fullName
) {}

public record LoginRequest(
    @NotBlank(message = "Email is mandatory")
    String email,

    @NotBlank(message = "Password is mandatory")
    String password
) {}

public record RefreshTokenRequest(
    @NotBlank(message = "Refresh token is mandatory")
    String refreshToken
) {}
```

---

## 3. Response DTO Records

### 3.1 Task Service Response Records
```java
package com.chellaa.task_service.dto.response;

import com.chellaa.task_service.entity.Priority;
import com.chellaa.task_service.entity.Status;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TaskResponse(
    UUID id,
    UUID userId,
    String title,
    String description,
    Status status,
    Priority priority,
    Instant dueDate,
    boolean isOverdue,
    Instant createdAt,
    Instant updatedAt
) {}

public record TaskMetricsResponse(
    long totalTasks,
    long todoCount,
    long inProgressCount,
    long completedCount,
    long cancelledCount,
    long highPriorityCount,
    long overdueCount
) {}

public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean isLast
) {}
```

---

## 4. Review Checklist
- [x] Java 21 `record` types used exclusively for request and response models.
- [x] Comprehensive Jakarta Bean Validation annotations applied.
- [x] Generic `PageResponse<T>` provides consistent pagination metadata.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Software Architect | Initial baseline DTO Design document. |

