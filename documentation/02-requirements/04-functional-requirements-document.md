# Functional Requirements Document (FRD): Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-REQ-004 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Senior Business Analyst |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-REQ-003`, `REG-RTM-001` |

---

## 1. Scope & Purpose
This document provides exhaustive, implementation-ready functional specifications for all 20 Functional Requirements (`FR-001` through `FR-020`) of the Task Management System, detailing workflows, validations, error responses, and state boundaries.

---

## 2. Functional Requirements Decomposition

### 2.1 User Management Domain

#### FR-001: User Registration
- **Purpose**: Allow new individual users to register an account.
- **Actor**: Anonymous Web Visitor.
- **Preconditions**: User provides an email address not currently registered.
- **Input Validations**:
  - `email`: Non-empty, valid RFC 5322 email syntax, max 255 chars.
  - `password`: Non-empty, 8..100 chars, minimum 1 uppercase letter, 1 lowercase letter, 1 digit, 1 special character (`!@#$%^&*()-_+=`).
  - `fullName`: Non-empty, 1..100 chars, trimmed.
- **Main Workflow**:
  1. Client sends `POST /api/v1/auth/register` with `{ email, password, fullName }`.
  2. Gateway routes request to User Service.
  3. User Service validates schema; queries `users` table for matching email.
  4. User Service hashes password via BCrypt (cost factor 12).
  5. User Service persists record with UUID v4 identifier, status `ACTIVE`, created timestamp UTC.
  6. Returns HTTP `201 Created` with `{ id, email, fullName, createdAt }`.
- **Alternative / Error Flows**:
  - Email already exists: Return HTTP `409 Conflict` (`ERR_EMAIL_ALREADY_EXISTS`).
  - Malformed payload: Return HTTP `400 Bad Request` with field validation errors.

#### FR-002: Secure Password Hashing
- **Purpose**: Guarantee cryptographic security of stored credentials.
- **Actor**: User Service Internal Worker.
- **Preconditions**: Raw password passed validation in FR-001.
- **Specification**: Raw password must never be logged or persisted. Hasher uses BCrypt with random 16-byte salt per hash and cost factor 12.

#### FR-003: User Login & JWT Issuance
- **Purpose**: Authenticate user credentials and return stateless session tokens.
- **Actor**: Registered User.
- **Preconditions**: Account status is `ACTIVE`.
- **Main Workflow**:
  1. Client sends `POST /api/v1/auth/login` with `{ email, password }`.
  2. User Service fetches user by email.
  3. User Service executes `BCrypt.checkpw(password, user.passwordHash)`.
  4. Generates JWT Access Token (TTL: 15 minutes, payload: `sub=userId`, `email`, `role=ROLE_USER`).
  5. Generates Refresh Token (UUID or opaque cryptographically random string, TTL: 7 days), persists in `refresh_tokens` table.
  6. Returns HTTP `200 OK` with `{ accessToken, refreshToken, tokenType: "Bearer", expiresIn: 900 }`.
- **Alternative / Error Flows**:
  - Invalid email or wrong password: Return HTTP `401 Unauthorized` (`ERR_INVALID_CREDENTIALS`).
  - Account disabled: Return HTTP `403 Forbidden` (`ERR_ACCOUNT_DISABLED`).

#### FR-004: Refresh Token Rotation
- **Purpose**: Renew expired Access Token without re-prompting for password.
- **Actor**: Authenticated Client Application.
- **Main Workflow**:
  1. Client sends `POST /api/v1/auth/refresh` with `{ refreshToken }`.
  2. User Service verifies token existence, expiry, and revoked flag.
  3. User Service invalidates used refresh token (`revoked = true`).
  4. Issues new Access Token and newly generated Refresh Token.
  5. Returns HTTP `200 OK` with new token pair.
- **Alternative Flows**: Expired or revoked refresh token: Return HTTP `401 Unauthorized` (`ERR_REFRESH_TOKEN_EXPIRED`).

#### FR-005 & FR-006: User Profile Retrieval & Update
- **Purpose**: Retrieve and update authenticated user details.
- **Actor**: Authenticated User.
- **Endpoints**: `GET /api/v1/users/me` and `PUT /api/v1/users/me`.
- **Preconditions**: Valid Bearer JWT passed in `Authorization` header.
- **Workflow**: Gateway validates JWT and injects `X-User-Id` header. User Service fetches profile matching `X-User-Id`. On `PUT`, updates `fullName`. Returns HTTP `200 OK`.

---

### 2.2 Task Management Domain

#### FR-007: Task Creation
- **Purpose**: Create a new personal task.
- **Actor**: Authenticated User.
- **Input Validations**:
  - `title`: Mandatory, 1..255 characters.
  - `description`: Optional, max 2000 characters.
  - `priority`: Optional enum (`LOW`, `MEDIUM`, `HIGH`, `URGENT`), defaults to `MEDIUM`.
  - `dueDate`: Optional ISO-8601 UTC timestamp. If provided, must not be in the past at creation (warning/allowable).
- **Main Workflow**:
  1. Client sends `POST /api/v1/tasks` with task payload.
  2. Gateway injects `X-User-Id`.
  3. Task Service creates task entity: `id = UUID.randomUUID()`, `userId = X-User-Id`, `status = TODO`, timestamps in UTC.
  4. Task Service inserts task into `tasks` table and creates `TASK_CREATED` event record in `outbox_events` within same database transaction.
  5. Returns HTTP `201 Created` with full task representation.

#### FR-008: Task Detail Retrieval
- **Purpose**: Fetch a single task by ID.
- **Workflow**: `GET /api/v1/tasks/{id}`. Task Service executes `SELECT * FROM tasks WHERE id = :id AND user_id = :userId AND is_deleted = false`. Returns HTTP `200 OK` or HTTP `404 Not Found`.

#### FR-009: Task Full Update
- **Purpose**: Update task title, description, priority, and due date.
- **Workflow**: `PUT /api/v1/tasks/{id}`. Validates task exists and belongs to user. Updates fields, updates `updated_at = UTC`, writes `TASK_UPDATED` event to outbox, returns HTTP `200 OK`.

#### FR-010: Task Soft Deletion
- **Purpose**: Remove task from active visibility while retaining audit history.
- **Workflow**: `DELETE /api/v1/tasks/{id}`. Executes `UPDATE tasks SET is_deleted = true, deleted_at = now() WHERE id = :id AND user_id = :userId`. Inserts `TASK_DELETED` into outbox. Returns HTTP `204 No Content`.

#### FR-011 & FR-012: Task Status & Priority Transitions
- **Purpose**: Atomic updates to status or priority without sending entire payload.
- **Endpoints**:
  - `PATCH /api/v1/tasks/{id}/status` with `{ "status": "IN_PROGRESS" }`. If transitioned to `COMPLETED`, outbox writes `TASK_COMPLETED`.
  - `PATCH /api/v1/tasks/{id}/priority` with `{ "priority": "URGENT" }`. Outbox writes `TASK_UPDATED`.
- **Returns**: HTTP `200 OK` with updated entity.

#### FR-013, FR-014, FR-015, FR-016: Querying, Filtering, Sorting & Pagination
- **Purpose**: Flexible, high-performance listing of tasks.
- **Endpoint**: `GET /api/v1/tasks?query=build&status=TODO,IN_PROGRESS&priority=HIGH&page=0&size=20&sort=dueDate,asc`.
- **Query Logic**:
  - Search: ILIKE matching on `title` or `description`.
  - Filter: IN clause on `status` and `priority`; range checks on `dueDate` (`dueFrom`, `dueTo`).
  - Sort: Whitelist validation (`dueDate`, `priority`, `createdAt`, `title`). Default: `createdAt,desc`.
  - Pagination: Zero-based `page`, `size` constrained between 1 and 100 (default: 20).
- **Returns**: HTTP `200 OK` with `{ content: [...], page: 0, size: 20, totalElements: 42, totalPages: 3, isLast: false }`.

---

### 2.3 Dashboard & Notification Domains

#### FR-017: Dashboard Productivity Summary
- **Purpose**: Aggregated count of tasks for productivity widgets.
- **Endpoint**: `GET /api/v1/tasks/metrics/summary`.
- **Calculations**:
  - `totalTasks`: count where `is_deleted = false`.
  - `todoCount`, `inProgressCount`, `completedCount`, `cancelledCount`: grouped by status.
  - `highPriorityCount`: count where `priority IN ('HIGH', 'URGENT') AND status NOT IN ('COMPLETED', 'CANCELLED')`.
  - `overdueCount`: count where `due_date < now(UTC) AND status NOT IN ('COMPLETED', 'CANCELLED')`.
- **Returns**: HTTP `200 OK` with summary JSON payload.

#### FR-018: Kafka Event Publishing (Transactional Outbox)
- **Purpose**: Reliable asynchronous event dispatch.
- **Specification**: Every task mutation inserts an outbox record into `outbox_events`. An asynchronous Spring `@Scheduled` poller queries unprocessed rows, publishes to Kafka topic `task.events.v1`, and marks rows `PROCESSED`.

#### FR-019 & FR-020: Notification Ingestion & Management
- **Purpose**: Consume Kafka events, store notifications, and allow user inspection.
- **Consumer**: Subscribes to `task.events.v1`. Validates `event_id` not in `consumed_events`. Formats notification message. Inserts into `notifications` table (`id`, `userId`, `message`, `eventType`, `read=false`, `createdAt`).
- **REST Endpoints**:
  - `GET /api/v1/notifications`: Paginated list of notifications for user.
  - `PATCH /api/v1/notifications/{id}/read`: Set `read = true`, `read_at = now()`.
  - `PATCH /api/v1/notifications/read-all`: Bulk mark all unread notifications as read for user.

---

## 3. Review Checklist
- [x] All 20 Functional Requirements covered with exhaustive specifications.
- [x] Preconditions, main workflows, alternative flows, validations, and responses defined.
- [x] Multi-tenancy isolation and transactional outbox mechanics enforced.

---

## 4. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Senior Business Analyst | Initial baseline Functional Requirements Document. |

