# Acceptance Criteria Document: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-REQ-008 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Quality Assurance Lead & Product Manager |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-REQ-004`, `DOC-REQ-006`, `REG-RTM-001` |

---

## 1. Overview & BDD Testing Standard
This document specifies the authoritative acceptance criteria for the Task Management System. Criteria are defined using the Behavior-Driven Development (BDD) **Given-When-Then (Gherkin)** standard, covering happy paths, negative paths, boundary constraints, and security isolation.

---

## 2. Acceptance Criteria Catalogue

### Domain 1: Authentication & User Management

#### AC-AUTH-01: User Registration Validation & Success
```gherkin
Scenario: Successful user registration
  Given an anonymous visitor on the registration endpoint
  When a payload is submitted with email "developer@example.com", fullName "Jane Developer", and password "P@ssw0rd2026"
  Then the response status code is 201 Created
  And the response body contains a valid UUID "id", "email", and "fullName"
  And the password field is absent from the response body
  And the database stores the password hash with cost factor 12

Scenario: Duplicate email rejection
  Given a registered user exists with email "developer@example.com"
  When another registration request is submitted with email "developer@example.com"
  Then the response status code is 409 Conflict
  And the response body contains error code "ERR_EMAIL_ALREADY_EXISTS"
  And no duplicate database record is created

Scenario: Weak password rejection
  Given an anonymous visitor
  When registration is attempted with password "weak"
  Then the response status code is 400 Bad Request
  And the response contains field error "password: Minimum 8 characters, at least one uppercase, one lowercase, one digit, and one special character"
```

#### AC-AUTH-02: User Login & JWT Token Claims
```gherkin
Scenario: Valid credentials login
  Given an active user with email "developer@example.com" and password "P@ssw0rd2026"
  When POST /api/v1/auth/login is executed with valid credentials
  Then the response status code is 200 OK
  And the response body contains "accessToken", "refreshToken", and "tokenType: Bearer"
  And decoding the accessToken reveals subject claim "sub" matching the user's UUID
  And the accessToken expiration is 900 seconds (15 minutes) from issuance

Scenario: Invalid password login failure
  Given an active user with email "developer@example.com"
  When POST /api/v1/auth/login is executed with password "WrongPassword"
  Then the response status code is 401 Unauthorized
  And the response body contains error code "ERR_INVALID_CREDENTIALS"
  And no token is issued
```

---

### Domain 2: Core Task Management & Multi-Tenancy

#### AC-TASK-01: Task Creation Defaults & Persistence
```gherkin
Scenario: Minimal task creation
  Given an authenticated user with userId "11111111-1111-1111-1111-111111111111"
  When POST /api/v1/tasks is submitted with only title "Complete System Architecture"
  Then the response status code is 201 Created
  And the created task has status "TODO"
  And the created task has priority "MEDIUM"
  And the created task has a non-null UUID "id"
  And "createdAt" and "updatedAt" are within 5 seconds of the current UTC time
  And a TASK_CREATED record is inserted into the "outbox_events" table
```

#### AC-TASK-02: Boundary Validations on Task Fields
```gherkin
Scenario: Blank title rejection
  Given an authenticated user
  When POST /api/v1/tasks is submitted with title "   "
  Then the response status code is 400 Bad Request
  And the error message specifies "title: must not be blank"

Scenario: Maximum length boundary on title
  Given an authenticated user
  When POST /api/v1/tasks is submitted with a title of 256 characters
  Then the response status code is 400 Bad Request
  And the error message specifies "title: length must be between 1 and 255 characters"
```

#### AC-TASK-03: Multi-Tenant Data Isolation (Zero Leakage)
```gherkin
Scenario: Cross-user task access rejection
  Given User A (UUID: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") owns Task 101
  And User B (UUID: "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb") is authenticated
  When User B requests GET /api/v1/tasks/101
  Then the response status code is 404 Not Found
  And no task details are exposed

Scenario: Cross-user task mutation rejection
  Given User A owns Task 101
  When User B requests PUT /api/v1/tasks/101 with title "Malicious Title"
  Then the response status code is 404 Not Found
  And Task 101 in the database remains unmodified
```

#### AC-TASK-04: Soft Deletion Verification
```gherkin
Scenario: Task soft deletion lifecycle
  Given an authenticated user owns Task 202
  When DELETE /api/v1/tasks/202 is submitted
  Then the response status code is 204 No Content
  And the database record has "is_deleted = true" and "deleted_at" set to UTC now
  And a subsequent GET /api/v1/tasks/202 by the owner returns 404 Not Found
  And Task 202 is excluded from GET /api/v1/tasks list queries
```

---

### Domain 3: Querying, Filtering, Pagination & Metrics

#### AC-QUERY-01: Pagination & Sorting Contract
```gherkin
Scenario: Paginated task query
  Given an authenticated user owns 25 active tasks
  When GET /api/v1/tasks?page=0&size=10&sort=dueDate,asc is executed
  Then the response status code is 200 OK
  And the response "content" array contains exactly 10 tasks
  And "totalElements" equals 25
  And "totalPages" equals 3
  And tasks are sorted in ascending order of "dueDate"
```

#### AC-DASH-01: Dashboard Summary Accuracy
```gherkin
Scenario: Accurate dashboard metric aggregation
  Given an authenticated user owns:
    | Status      | Priority | Due Date    | Count |
    | TODO        | MEDIUM   | Tomorrow    | 3     |
    | IN_PROGRESS | HIGH     | Yesterday   | 2     |
    | COMPLETED   | URGENT   | Yesterday   | 4     |
    | CANCELLED   | LOW      | Next Week   | 1     |
  When GET /api/v1/tasks/metrics/summary is executed
  Then "totalTasks" equals 10
  And "todoCount" equals 3
  And "inProgressCount" equals 2
  And "completedCount" equals 4
  And "cancelledCount" equals 1
  And "highPriorityCount" equals 2 (only active HIGH/URGENT)
  And "overdueCount" equals 2 (only active tasks with past due dates)
```

---

### Domain 4: Asynchronous Events & Notifications

#### AC-EVT-01: Idempotent Event Consumption
```gherkin
Scenario: Duplicate Kafka event handling
  Given an event with eventId "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee" has been consumed by Notification Service
  When Kafka delivers the identical message a second time
  Then Notification Service detects the eventId in "consumed_events"
  And skips duplicate insertion into "notifications" table
  And acknowledges the message to Kafka broker without error
```

---

## 3. Review Checklist
- [x] Comprehensive Gherkin BDD scenarios documented across all 4 domains.
- [x] Positive, negative, boundary, and multi-tenant security scenarios detailed.
- [x] Clear mapping to test cases and requirements traceability.

---

## 4. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | QA Lead | Initial baseline Acceptance Criteria document. |

