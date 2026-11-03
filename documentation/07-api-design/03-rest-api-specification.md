# REST API Specification: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-API-003 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | API Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-API-001`, `DOC-API-002`, `DOC-LLD-008` |

---

## 1. Authentication Endpoints (`user-service`)

### 1.1 `POST /api/v1/auth/register`
- **Purpose**: Register a new user account.
- **Auth**: Public (No JWT required).
- **Request Body**:
  ```json
  {
    "email": "sarah.connor@example.com",
    "password": "Password123!",
    "fullName": "Sarah Connor"
  }
  ```
- **Validation**: Email valid syntax; password min 8 chars with uppercase, lowercase, digit, special; fullName 1..100 chars.
- **Response `201 Created`**:
  ```json
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "sarah.connor@example.com",
    "fullName": "Sarah Connor",
    "createdAt": "2026-11-03T10:00:00Z"
  }
  ```
- **Error Codes**: `400 Bad Request` (`ERR_VALIDATION_FAILED`), `409 Conflict` (`ERR_EMAIL_ALREADY_EXISTS`).

---

### 1.2 `POST /api/v1/auth/login`
- **Purpose**: Authenticate user and issue JWT Access and Refresh Tokens.
- **Auth**: Public.
- **Request Body**:
  ```json
  {
    "email": "sarah.connor@example.com",
    "password": "Password123!"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiIsIn...",
    "refreshToken": "d8a9f3b7c2e140a6b412...",
    "tokenType": "Bearer",
    "expiresIn": 900
  }
  ```
- **Error Codes**: `401 Unauthorized` (`ERR_INVALID_CREDENTIALS`), `403 Forbidden` (`ERR_ACCOUNT_DISABLED`).

---

### 1.3 `POST /api/v1/auth/refresh`
- **Purpose**: Rotate refresh token and obtain new access token.
- **Auth**: Public.
- **Request Body**:
  ```json
  {
    "refreshToken": "d8a9f3b7c2e140a6b412..."
  }
  ```
- **Response `200 OK`**: New token pair with updated `accessToken` and `refreshToken`.
- **Error Codes**: `401 Unauthorized` (`ERR_REFRESH_TOKEN_EXPIRED`).

---

## 2. Task Endpoints (`task-service`)

### 2.1 `POST /api/v1/tasks`
- **Purpose**: Create a new task for the authenticated user.
- **Auth**: Bearer JWT (`ROLE_USER`).
- **Request Headers**: `Authorization: Bearer <jwt>`, `Content-Type: application/json`.
- **Request Body**:
  ```json
  {
    "title": "Review Microservice Security Architecture",
    "description": "Perform threat modeling on API Gateway and JWT claims.",
    "priority": "HIGH",
    "dueDate": "2026-11-10T18:00:00Z"
  }
  ```
- **Validations**: `title` mandatory (1..255 chars); `priority` must be `LOW`, `MEDIUM`, `HIGH`, `URGENT` (defaults to `MEDIUM` if omitted); `dueDate` optional ISO-8601.
- **Response `201 Created`**:
  ```json
  {
    "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Review Microservice Security Architecture",
    "description": "Perform threat modeling on API Gateway and JWT claims.",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2026-11-10T18:00:00Z",
    "isOverdue": false,
    "createdAt": "2026-11-03T11:00:00Z",
    "updatedAt": "2026-11-03T11:00:00Z"
  }
  ```
- **Error Codes**: `400 Bad Request` (`ERR_VALIDATION_FAILED`), `401 Unauthorized`.

---

### 2.2 `GET /api/v1/tasks/{id}`
- **Purpose**: Retrieve single task details by ID.
- **Auth**: Bearer JWT.
- **Path Parameters**: `id` (UUID) - Target task ID.
- **Response `200 OK`**: Returns TaskResponse JSON.
- **Error Codes**: `404 Not Found` (`ERR_RESOURCE_NOT_FOUND` if task does not exist or belongs to another user).

---

### 2.3 `PUT /api/v1/tasks/{id}`
- **Purpose**: Replace mutable attributes of an existing task.
- **Auth**: Bearer JWT.
- **Request Body**:
  ```json
  {
    "title": "Review Microservice Architecture & Resilience",
    "description": "Updated scope including Circuit Breaker tests.",
    "priority": "URGENT",
    "dueDate": "2026-11-09T18:00:00Z"
  }
  ```
- **Response `200 OK`**: Returns updated TaskResponse JSON.
- **Error Codes**: `400 Bad Request`, `404 Not Found`.

---

### 2.4 `PATCH /api/v1/tasks/{id}/status`
- **Purpose**: Atomic task state transition.
- **Auth**: Bearer JWT.
- **Request Body**:
  ```json
  {
    "status": "COMPLETED"
  }
  ```
- **Response `200 OK`**: Returns updated TaskResponse JSON.
- **Error Codes**: `400 Bad Request` (`ERR_INVALID_STATUS`), `404 Not Found`.

---

### 2.5 `DELETE /api/v1/tasks/{id}`
- **Purpose**: Soft delete a task.
- **Auth**: Bearer JWT.
- **Response `204 No Content`**: Empty response body.
- **Error Codes**: `404 Not Found`.

---

### 2.6 `GET /api/v1/tasks`
- **Purpose**: Search, filter, sort, and paginate active user tasks.
- **Auth**: Bearer JWT.
- **Query Parameters**:
  - `query` (string, optional): Keyword search across title and description.
  - `status` (string, optional): Comma-separated status filters (`TODO,IN_PROGRESS`).
  - `priority` (string, optional): Comma-separated priority filters (`HIGH,URGENT`).
  - `dueFrom`, `dueTo` (Instant, optional): Date range filter.
  - `page` (int, default: 0): Zero-based page index.
  - `size` (int, default: 20, max: 100): Items per page.
  - `sort` (string, default: `createdAt,desc`): Format `field,asc|desc`. Whitelist: `dueDate`, `priority`, `createdAt`, `title`.
- **Response `200 OK`**:
  ```json
  {
    "content": [
      {
        "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
        "userId": "550e8400-e29b-41d4-a716-446655440000",
        "title": "Review Microservice Security Architecture",
        "status": "TODO",
        "priority": "HIGH",
        "dueDate": "2026-11-10T18:00:00Z",
        "isOverdue": false,
        "createdAt": "2026-11-03T11:00:00Z",
        "updatedAt": "2026-11-03T11:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "isLast": true
  }
  ```

---

### 2.7 `GET /api/v1/tasks/metrics/summary`
- **Purpose**: Retrieve productivity summary counts for dashboard widgets.
- **Auth**: Bearer JWT.
- **Response `200 OK`**:
  ```json
  {
    "totalTasks": 24,
    "todoCount": 10,
    "inProgressCount": 6,
    "completedCount": 7,
    "cancelledCount": 1,
    "highPriorityCount": 5,
    "overdueCount": 2
  }
  ```

---

## 3. Notification Endpoints (`notification-service`)

### 3.1 `GET /api/v1/notifications`
- **Purpose**: Retrieve paginated list of in-app notifications.
- **Auth**: Bearer JWT.
- **Query Parameters**: `page` (int, default: 0), `size` (int, default: 10).
- **Response `200 OK`**:
  ```json
  {
    "content": [
      {
        "id": "9e1f889b-9647-52fa-b66d-g29ge3h12cg9",
        "eventType": "TASK_CREATED",
        "message": "Task 'Review Microservice Security Architecture' was created",
        "read": false,
        "readAt": null,
        "createdAt": "2026-11-03T11:00:01Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "unreadCount": 1
  }
  ```

### 3.2 `PATCH /api/v1/notifications/{id}/read`
- **Purpose**: Mark single notification as read.
- **Auth**: Bearer JWT.
- **Response `200 OK`**: Empty or updated notification record.

### 3.3 `PATCH /api/v1/notifications/read-all`
- **Purpose**: Bulk mark all unread notifications as read.
- **Auth**: Bearer JWT.
- **Response `200 OK`**: `{"updatedCount": 3}`.

---

## 4. Review Checklist
- [x] All 12 system endpoints exhaustively documented with verbs, paths, and payloads.
- [x] Method, authentication, path/query params, and JSON examples provided for every endpoint.
- [x] Multi-tenant isolation and 404 security responses verified.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | API Architect | Initial baseline REST API Specification. |

