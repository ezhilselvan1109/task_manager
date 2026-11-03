# Test Scenarios Document

**Document Identifier:** DOC-TEST-003  
**Traceability References:** US-001 to US-016, UC-001 to UC-012, FR-001 to FR-020  
**Target Architecture:** End-to-End Platform Flows  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the primary end-to-end business test scenarios across the Task Management System. Each scenario details the user journey, preconditions, action steps, cross-service interactions, and expected behavioral outcomes.

---

## 2. Business Test Scenarios Inventory

### `TSCN-001`: User Registration, Login, and Profile Retrieval
- **Objective**: Verify that a new user registers successfully with hashed credentials, authenticates with valid credentials to receive a signed JWT, and accesses their private profile.
- **Preconditions**: `user_db` running and healthy; username and email not previously registered.
- **Execution Flow**:
  1. POST `/api/v1/auth/register` with valid username, email, and strong password.
  2. POST `/api/v1/auth/login` with registered credentials.
  3. GET `/api/v1/users/me` using returned `Bearer <token>`.
- **Expected Outcome**:
  - Step 1: Returns `201 Created` with `UserResponse` (password omitted).
  - Step 2: Returns `200 OK` with valid 15-minute JWT.
  - Step 3: Returns `200 OK` with profile details matching caller identity.

### `TSCN-002`: Task Lifecycle Execution and Status Transitions
- **Objective**: Verify full state transition sequence: `TODO` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `COMPLETED`.
- **Preconditions**: Authenticated user session.
- **Execution Flow**:
  1. POST `/api/v1/tasks` (creates task, default status `TODO`).
  2. PATCH `/api/v1/tasks/{id}/status` with `IN_PROGRESS`.
  3. PATCH `/api/v1/tasks/{id}/status` with `COMPLETED`.
- **Expected Outcome**:
  - Task status updates cleanly in `task_db`; version incremented on each update.
  - Domain events `TASK_CREATED`, `TASK_UPDATED`, `TASK_COMPLETED` written to `outbox_events`.

### `TSCN-003`: Multi-Attribute Dynamic Search, Filtering, and Pagination
- **Objective**: Verify filtering by status, priority, title substring, and page limits.
- **Execution Flow**:
  1. Create 15 tasks with varying priorities and statuses for User A.
  2. GET `/api/v1/tasks?status=TODO&priority=HIGH&page=0&size=5&sort=dueDate,asc`.
- **Expected Outcome**:
  - Returns `PageResponse` containing only User A's tasks matching both criteria.
  - Page metadata shows `totalElements`, `totalPages`, `pageSize: 5`, `pageNumber: 0`.

### `TSCN-004`: Anti-Enumeration & Multi-Tenant Data Concealment
- **Objective**: Verify that User B cannot discover or access tasks belonging to User A.
- **Execution Flow**:
  1. User A creates a task (ID: `task-A-id`).
  2. User B logs in and issues `GET /api/v1/tasks/task-A-id`.
  3. User B issues `DELETE /api/v1/tasks/task-A-id`.
- **Expected Outcome**:
  - Both requests return `HTTP 404 Not Found` with `RESOURCE_NOT_FOUND`.
  - Zero information leakage; User B cannot determine that `task-A-id` exists.

### `TSCN-005`: Asynchronous Event Delivery & Notification Generation
- **Objective**: Verify that task mutation in `task-service` produces an in-app notification in `notification-service`.
- **Execution Flow**:
  1. User A completes a task.
  2. Outbox poller relays `TASK_COMPLETED` event to Kafka topic `task.events.v1`.
  3. Notification consumer processes record and writes to `notifications`.
  4. User A queries `GET /api/v1/notifications`.
- **Expected Outcome**:
  - Notification appears in User A's unread tray within 2 seconds.

### `TSCN-006`: Consumer Idempotency on Redelivered Kafka Events
- **Objective**: Verify that Kafka re-delivery does not create duplicate user notifications.
- **Execution Flow**:
  1. Produce identical `TASK_CREATED` event twice with identical `eventId`.
  2. Notification listener processes first record, writes to `notifications` and `consumed_events`.
  3. Notification listener receives second duplicate record.
- **Expected Outcome**:
  - Listener detects `eventId` in `consumed_events`, logs duplicate warning, commits offset without inserting second notification.

### `TSCN-007`: Poison Pill Message Isolation in Dead Letter Topic
- **Objective**: Verify corrupted messages do not halt consumer pipeline.
- **Execution Flow**:
  1. Produce invalid JSON string to `task.events.v1`.
  2. Listener error handler retries up to configured policy, then invokes recoverer.
- **Expected Outcome**:
  - Corrupted record forwarded to `task.events.v1.DLT`; consumer offset committed; consumer continues processing valid messages.

### `TSCN-008`: Circuit Breaker Tripping and Fallback
- **Objective**: Verify Gateway circuit breaker opens during `task-service` outage.
- **Execution Flow**:
  1. Simulate `task-service` failure (stop container).
  2. Issue 20 requests to `/api/v1/tasks`.
- **Expected Outcome**:
  - Circuit trips to `OPEN`; subsequent calls return `503 Service Unavailable` fallback immediately ($<5$ ms).

### `TSCN-009`: Rate Limiting Throttling on Authentication Ingress
- **Objective**: Verify Token Bucket limits brute-force login attempts.
- **Execution Flow**:
  1. Issue 12 login requests within 30 seconds from single client IP.
- **Expected Outcome**:
  - 11th and 12th requests receive `HTTP 429 Too Many Requests` with `Retry-After: 60`.

### `TSCN-010`: Soft-Deletion and Data Isolation
- **Objective**: Verify deleted tasks are excluded from lists and lookups while remaining in DB.
- **Execution Flow**:
  1. Delete task via `DELETE /api/v1/tasks/{id}`.
  2. Query `GET /api/v1/tasks/{id}`.
  3. Query `GET /api/v1/tasks`.
- **Expected Outcome**:
  - Direct GET returns `404 Not Found`; task is absent from list; DB retains row with `is_deleted = true`.

