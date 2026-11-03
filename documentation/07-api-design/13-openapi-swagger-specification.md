# OpenAPI 3.0 Specification: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-API-013 |
| **Document Version** | 1.0.0 |
| **OpenAPI Version** | 3.0.3 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | API Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-API-003`, `DOC-API-004`, `DOC-API-005` |

---

## 1. OpenAPI 3.0.3 Definition (YAML)

```yaml
openapi: 3.0.3
info:
  title: Task Management System API
  description: Distributed microservices REST API for personal task management, authentication, and in-app notifications.
  version: 1.0.0
servers:
  - url: http://localhost:8080
    description: Local API Gateway Ingress

paths:
  /api/v1/auth/register:
    post:
      summary: Register User
      tags: [Authentication]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RegisterRequest'
      responses:
        '201':
          description: User registered successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserProfileResponse'
        '400':
          $ref: '#/components/responses/ValidationError'
        '409':
          $ref: '#/components/responses/ConflictError'

  /api/v1/auth/login:
    post:
      summary: User Login
      tags: [Authentication]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LoginRequest'
      responses:
        '200':
          description: Authentication successful
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthResponse'
        '401':
          $ref: '#/components/responses/UnauthorizedError'

  /api/v1/auth/refresh:
    post:
      summary: Refresh Access Token
      tags: [Authentication]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RefreshTokenRequest'
      responses:
        '200':
          description: Token rotated successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthResponse'
        '401':
          $ref: '#/components/responses/UnauthorizedError'

  /api/v1/users/me:
    get:
      summary: Get Current Profile
      tags: [User Profile]
      security:
        - BearerAuth: []
      responses:
        '200':
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserProfileResponse'
        '401':
          $ref: '#/components/responses/UnauthorizedError'
    put:
      summary: Update Profile
      tags: [User Profile]
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateProfileRequest'
      responses:
        '200':
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserProfileResponse'

  /api/v1/tasks:
    post:
      summary: Create Task
      tags: [Tasks]
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateTaskRequest'
      responses:
        '201':
          description: Task created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TaskResponse'
        '400':
          $ref: '#/components/responses/ValidationError'
    get:
      summary: List & Filter Tasks
      tags: [Tasks]
      security:
        - BearerAuth: []
      parameters:
        - name: query
          in: query
          schema: { type: string }
        - name: status
          in: query
          schema: { type: string }
        - name: priority
          in: query
          schema: { type: string }
        - name: page
          in: query
          schema: { type: integer, default: 0 }
        - name: size
          in: query
          schema: { type: integer, default: 20 }
        - name: sort
          in: query
          schema: { type: string, default: "createdAt,desc" }
      responses:
        '200':
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TaskPageResponse'

  /api/v1/tasks/{id}:
    get:
      summary: Get Task Details
      tags: [Tasks]
      security:
        - BearerAuth: []
      parameters:
        - name: id
          in: path
          required: true
          schema: { type: string, format: uuid }
      responses:
        '200':
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TaskResponse'
        '404':
          $ref: '#/components/responses/NotFoundError'
    put:
      summary: Replace Task
      tags: [Tasks]
      security:
        - BearerAuth: []
      parameters:
        - name: id
          in: path
          required: true
          schema: { type: string, format: uuid }
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateTaskRequest'
      responses:
        '200':
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TaskResponse'
    delete:
      summary: Soft Delete Task
      tags: [Tasks]
      security:
        - BearerAuth: []
      parameters:
        - name: id
          in: path
          required: true
          schema: { type: string, format: uuid }
      responses:
        '204':
          description: Task soft deleted
        '404':
          $ref: '#/components/responses/NotFoundError'

  /api/v1/tasks/{id}/status:
    patch:
      summary: Update Task Status
      tags: [Tasks]
      security:
        - BearerAuth: []
      parameters:
        - name: id
          in: path
          required: true
          schema: { type: string, format: uuid }
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateStatusRequest'
      responses:
        '200':
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TaskResponse'

  /api/v1/tasks/{id}/priority:
    patch:
      summary: Update Task Priority
      tags: [Tasks]
      security:
        - BearerAuth: []
      parameters:
        - name: id
          in: path
          required: true
          schema: { type: string, format: uuid }
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdatePriorityRequest'
      responses:
        '200':
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TaskResponse'

  /api/v1/tasks/metrics/summary:
    get:
      summary: Dashboard Productivity Summary
      tags: [Tasks]
      security:
        - BearerAuth: []
      responses:
        '200':
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TaskMetricsResponse'

  /api/v1/notifications:
    get:
      summary: List In-App Notifications
      tags: [Notifications]
      security:
        - BearerAuth: []
      responses:
        '200':
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/NotificationPageResponse'

  /api/v1/notifications/{id}/read:
    patch:
      summary: Mark Single Notification Read
      tags: [Notifications]
      security:
        - BearerAuth: []
      parameters:
        - name: id
          in: path
          required: true
          schema: { type: string, format: uuid }
      responses:
        '200':
          description: Notification marked read

  /api/v1/notifications/read-all:
    patch:
      summary: Mark All Notifications Read
      tags: [Notifications]
      security:
        - BearerAuth: []
      responses:
        '200':
          description: All notifications marked read

components:
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  responses:
    ValidationError:
      description: Bad Request (Field Validation Failed)
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    UnauthorizedError:
      description: Unauthorized (Token Missing, Invalid, or Expired)
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    NotFoundError:
      description: Resource Not Found
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    ConflictError:
      description: Conflict (Unique Constraint Violation)
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'

  schemas:
    RegisterRequest:
      type: object
      required: [email, password, fullName]
      properties:
        email: { type: string, format: email }
        password: { type: string, minLength: 8 }
        fullName: { type: string, minLength: 1 }

    LoginRequest:
      type: object
      required: [email, password]
      properties:
        email: { type: string }
        password: { type: string }

    RefreshTokenRequest:
      type: object
      required: [refreshToken]
      properties:
        refreshToken: { type: string }

    UpdateProfileRequest:
      type: object
      required: [fullName]
      properties:
        fullName: { type: string, minLength: 1, maxLength: 100 }

    UserProfileResponse:
      type: object
      properties:
        id: { type: string, format: uuid }
        email: { type: string }
        fullName: { type: string }
        createdAt: { type: string, format: date-time }

    AuthResponse:
      type: object
      properties:
        accessToken: { type: string }
        refreshToken: { type: string }
        tokenType: { type: string, example: "Bearer" }
        expiresIn: { type: integer, example: 900 }

    CreateTaskRequest:
      type: object
      required: [title]
      properties:
        title: { type: string, minLength: 1, maxLength: 255 }
        description: { type: string, maxLength: 2000 }
        priority: { type: string, enum: [LOW, MEDIUM, HIGH, URGENT], default: MEDIUM }
        dueDate: { type: string, format: date-time }

    UpdateTaskRequest:
      type: object
      required: [title]
      properties:
        title: { type: string, minLength: 1, maxLength: 255 }
        description: { type: string, maxLength: 2000 }
        priority: { type: string, enum: [LOW, MEDIUM, HIGH, URGENT] }
        dueDate: { type: string, format: date-time }

    UpdateStatusRequest:
      type: object
      required: [status]
      properties:
        status: { type: string, enum: [TODO, IN_PROGRESS, COMPLETED, CANCELLED] }

    UpdatePriorityRequest:
      type: object
      required: [priority]
      properties:
        priority: { type: string, enum: [LOW, MEDIUM, HIGH, URGENT] }

    TaskResponse:
      type: object
      properties:
        id: { type: string, format: uuid }
        userId: { type: string, format: uuid }
        title: { type: string }
        description: { type: string }
        status: { type: string, enum: [TODO, IN_PROGRESS, COMPLETED, CANCELLED] }
        priority: { type: string, enum: [LOW, MEDIUM, HIGH, URGENT] }
        dueDate: { type: string, format: date-time }
        isOverdue: { type: boolean }
        createdAt: { type: string, format: date-time }
        updatedAt: { type: string, format: date-time }

    TaskMetricsResponse:
      type: object
      properties:
        totalTasks: { type: integer }
        todoCount: { type: integer }
        inProgressCount: { type: integer }
        completedCount: { type: integer }
        cancelledCount: { type: integer }
        highPriorityCount: { type: integer }
        overdueCount: { type: integer }

    TaskPageResponse:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/TaskResponse'
        page: { type: integer }
        size: { type: integer }
        totalElements: { type: integer }
        totalPages: { type: integer }
        isLast: { type: boolean }

    NotificationPageResponse:
      type: object
      properties:
        content:
          type: array
          items:
            type: object
            properties:
              id: { type: string, format: uuid }
              eventType: { type: string }
              message: { type: string }
              read: { type: boolean }
              createdAt: { type: string, format: date-time }
        page: { type: integer }
        size: { type: integer }
        totalElements: { type: integer }
        totalPages: { type: integer }
        unreadCount: { type: integer }

    ErrorResponse:
      type: object
      properties:
        timestamp: { type: string, format: date-time }
        status: { type: integer }
        error: { type: string }
        errorCode: { type: string }
        message: { type: string }
        path: { type: string }
        correlationId: { type: string, format: uuid }
        fieldErrors:
          type: array
          items:
            type: object
            properties:
              field: { type: string }
              rejectedValue: { type: string }
              message: { type: string }
```

---

## 2. Review Checklist
- [x] Full OpenAPI 3.0.3 specification covering all 16 endpoints in valid YAML.
- [x] Security schemes configured for BearerAuth (JWT).
- [x] Reusable component schemas defined for all requests, responses, and errors.

---

## 3. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | API Architect | Initial baseline OpenAPI 3.0 Specification. |

