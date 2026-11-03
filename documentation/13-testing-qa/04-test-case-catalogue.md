# Test Case Catalogue

**Document Identifier:** DOC-TEST-004  
**Traceability References:** FR-001 to FR-020, SEC-001 to SEC-010, EVT-001 to EVT-004  
**Total Test Cases:** 40 Formally Specified Test Cases (`TC-001` through `TC-040`)  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document provides the master catalogue of all 40 individual, numbered test cases covering the entire Task Management Microservices platform. Every test case links directly to functional requirements, security policies, and user stories.

---

## 2. Test Case Inventory Table

| Test Case ID | Test Case Title | Layer | Target Component | Input / Action | Expected Result | Traceability |
|---|---|---|---|---|---|---|
| `TC-001` | Successful User Registration | Web Slice | `user-service` | Valid `RegisterRequest` | Returns 201 Created; passwords omitted; `id` generated | `FR-001`, `US-001` |
| `TC-002` | Reject Duplicate Username Registration | Web Slice | `user-service` | Existing `username` | Returns 409 Conflict with RFC 7807 ProblemDetail | `FR-001`, `BRULE-008` |
| `TC-003` | Reject Duplicate Email Registration | Web Slice | `user-service` | Existing `email` | Returns 409 Conflict with RFC 7807 ProblemDetail | `FR-001`, `BRULE-008` |
| `TC-004` | Reject Weak Password During Registration | Unit | `user-service` | Password without special char | Returns 400 Bad Request with validation error message | `FR-001`, `SEC-003` |
| `TC-005` | Successful Login with Valid Credentials | Web Slice | `user-service` | Valid username & password | Returns 200 OK with valid signed HMAC-SHA256 JWT | `FR-002`, `US-002` |
| `TC-006` | Reject Login with Incorrect Password | Web Slice | `user-service` | Valid username, bad password | Returns 401 Unauthorized with generic error code | `FR-002`, `SEC-001` |
| `TC-007` | Retrieve Own Profile | Integration | `user-service` | Valid `Bearer <token>` | Returns 200 OK with user profile details | `FR-003`, `US-003` |
| `TC-008` | Reject Unauthenticated Profile Access | Web Slice | `api-gateway` | Missing Authorization header | Returns 401 Unauthorized (`MISSING_AUTH_TOKEN`) | `FR-003`, `SEC-001` |
| `TC-009` | Create Task Happy Path | Integration | `task-service` | Valid `CreateTaskRequest` | Returns 201 Created; default status TODO, priority MEDIUM | `FR-005`, `US-004` |
| `TC-010` | Reject Task Creation with Blank Title | Web Slice | `task-service` | `title: ""` | Returns 400 Bad Request with field validation error | `FR-005`, `BRULE-003` |
| `TC-011` | Retrieve Task by ID (Own Task) | Integration | `task-service` | Valid `taskId` for owner | Returns 200 OK with task details | `FR-006`, `US-005` |
| `TC-012` | Anti-Enumeration on Foreign Task Lookup | Integration | `task-service` | Query task owned by other user | Returns 404 Not Found (resource concealed) | `FR-006`, `SEC-002` |
| `TC-013` | Update Task Title and Description | Integration | `task-service` | Valid `UpdateTaskRequest` | Returns 200 OK with updated attributes; version increments | `FR-007`, `US-006` |
| `TC-014` | Reject Update on Non-Existent Task | Web Slice | `task-service` | Random non-existent UUID | Returns 404 Not Found | `FR-007`, `BRULE-002` |
| `TC-015` | Transition Task Status to IN_PROGRESS | Integration | `task-service` | Status `IN_PROGRESS` | Returns 200 OK; status updated; outbox event saved | `FR-008`, `US-007` |
| `TC-016` | Transition Task Status to COMPLETED | Integration | `task-service` | Status `COMPLETED` | Returns 200 OK; status updated; outbox event saved | `FR-008`, `US-007` |
| `TC-017` | Change Task Priority to URGENT | Integration | `task-service` | Priority `URGENT` | Returns 200 OK; priority updated; outbox event saved | `FR-008`, `US-008` |
| `TC-018` | Soft-Delete Task | Integration | `task-service` | Valid `taskId` | Returns 204 No Content; task marked `is_deleted = true` | `FR-007`, `US-006` |
| `TC-019` | Filter Tasks by Status TODO | Integration | `task-service` | `?status=TODO` | Returns only tasks with status TODO | `FR-009`, `US-009` |
| `TC-020` | Filter Tasks by Priority HIGH | Integration | `task-service` | `?priority=HIGH` | Returns only tasks with priority HIGH | `FR-009`, `US-009` |
| `TC-021` | Filter Tasks by Combined Status and Priority | Integration | `task-service` | `?status=TODO&priority=HIGH` | Returns tasks satisfying both predicates | `FR-009`, `US-009` |
| `TC-022` | Search Tasks by Title Keyword | Integration | `task-service` | `?search=report` | Returns tasks containing 'report' (case-insensitive) | `FR-009`, `US-009` |
| `TC-023` | Paginate Task List (Page 0, Size 10) | Integration | `task-service` | `?page=0&size=10` | Returns 10 items with complete `PageResponse` metadata | `FR-010`, `US-010` |
| `TC-024` | Sort Tasks by Due Date Ascending | Integration | `task-service` | `?sort=dueDate,asc` | Returns tasks ordered chronologically by due date | `FR-010`, `US-010` |
| `TC-025` | Atomic Outbox Event Persistence on Task Create | Integration | `task-service` | Task creation request | Verifies task row and `outbox_events` row committed together | `EVT-001`, `BRULE-009`|
| `TC-026` | Outbox Poller Emits Event to Kafka | Integration | `task-service` | Pending outbox records | Poller publishes to Kafka; marks `processed = true` | `EVT-001`, `DOC-EVT-014`|
| `TC-027` | Consumer Processes TASK_CREATED Event | Integration | `notification-service`| Valid Kafka record | Notification persisted; `consumed_events` recorded | `EVT-001`, `FR-011` |
| `TC-028` | Consumer Idempotency on Duplicate Event | Integration | `notification-service`| Duplicate event ID | Offset committed; zero duplicate notification created | `EVT-013`, `BRULE-010`|
| `TC-029` | Transient Consumer Retry with Backoff | Integration | `notification-service`| Simulated transient DB lock | Retried 3 times with exponential delay before succeeding | `EVT-010`, `DOC-RES-003`|
| `TC-030` | Poison Pill Routing to Dead Letter Topic | Integration | `notification-service`| Corrupted JSON record | Forwarded to `task.events.v1.DLT`; consumer offset commits | `EVT-011`, `DOC-EVT-011`|
| `TC-031` | Retrieve Unread Notifications for User | Integration | `notification-service`| Valid `Bearer <token>` | Returns list of unread notifications for caller | `FR-012`, `US-012` |
| `TC-032` | Mark Single Notification as Read | Integration | `notification-service`| Valid `notificationId` | Returns 200 OK; notification marked `is_read = true` | `FR-013`, `US-013` |
| `TC-033` | Mark All Notifications as Read | Integration | `notification-service`| Authenticated user | Returns 200 OK; all notifications marked read for caller | `FR-013`, `US-013` |
| `TC-034` | Anti-Enumeration on Foreign Notification | Integration | `notification-service`| Foreign notificationId | Returns 404 Not Found (resource concealed) | `FR-013`, `SEC-002` |
| `TC-035` | Gateway Strips Inbound X-User-Id Headers | Integration | `api-gateway` | Spoofed `X-User-Id: 999` | Downstream receives only JWT-derived identity header | `SEC-006`, `SRISK-002` |
| `TC-036` | Reject Expired JWT Token | Web Slice | `api-gateway` | JWT older than 15 minutes | Returns 401 Unauthorized (`TOKEN_EXPIRED`) | `SEC-001`, `DOC-SEC-004`|
| `TC-037` | Rate Limiter Throttles Flooding Requests | Integration | `api-gateway` | 15 req/sec on login | Returns 429 Too Many Requests (`RATE_LIMIT_EXCEEDED`) | `SEC-009`, `DOC-RES-005`|
| `TC-038` | Circuit Breaker Opens on Downstream Outage | Integration | `api-gateway` | Task service down | Fails fast after 10 calls; returns 503 fallback | `NFR-002`, `DOC-RES-004`|
| `TC-039` | Retrieve Dashboard Metrics Aggregation | Integration | `task-service` | Valid caller token | Returns counts for total, TODO, COMPLETED, overdue | `FR-015`, `US-014` |
| `TC-040` | Clean Actuator Health Probes | Slice | All Services | `GET /actuator/health` | Returns 200 OK `{"status": "UP"}` | `NFR-001`, `DOC-INF-006`|

