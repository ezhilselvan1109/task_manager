# End-to-End (E2E) Testing Strategy

**Document Identifier:** DOC-TEST-011  
**Traceability References:** US-001 to US-016, DOC-TEST-003, DOC-TEST-001  
**Harnesses:** Cypress / Playwright (Web UI), Postman Newman (API Mesh)  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the End-to-End (E2E) testing strategy for validating complete user journeys across the Task Management platform. E2E tests execute against the fully orchestrated Docker Compose environment, verifying real browser interactions and end-to-end API mesh communications.

---

## 2. Core E2E User Journey Flow

```mermaid
sequenceDiagram
    autonumber
    actor User as E2E Automated Test Runner
    participant Web as React Web SPA (Port 3000)
    participant GW as API Gateway (Port 8080)
    participant US as user-service
    participant TS as task-service
    participant KF as Apache Kafka
    participant NS as notification-service

    User->>Web: 1. Fill Registration Form & Submit
    Web->>GW: POST /api/v1/auth/register
    GW->>US: Register User
    US-->>Web: 201 Created

    User->>Web: 2. Submit Login Credentials
    Web->>GW: POST /api/v1/auth/login
    GW->>US: Authenticate & Mint JWT
    US-->>Web: 200 OK (JWT Token)

    User->>Web: 3. Create New Task "Sprint Backlog"
    Web->>GW: POST /api/v1/tasks (Bearer JWT)
    GW->>TS: Create Task & Outbox Record
    TS-->>Web: 201 Created (Task rendered in UI)

    TS->>KF: Outbox Poller publishes TASK_CREATED
    KF->>NS: Consumer creates Notification
    
    User->>Web: 4. Click Notification Bell Icon
    Web->>GW: GET /api/v1/notifications
    GW->>NS: Fetch User Notifications
    NS-->>Web: 200 OK (Displays "Task Update: Sprint Backlog")
```

---

## 3. Postman Newman Automated Mesh Suite

The API E2E regression suite executes during Jenkins staging verification via Newman:

```bash
newman run tests/e2e/task-manager-mesh.postman_collection.json \
  --environment tests/e2e/local.postman_environment.json \
  --reporters cli,junit \
  --reporter-junit-export target/newman-results.xml
```

- **Validation Checks**:
  1. Complete auth handshake and token extraction.
  2. Sequential task mutations (`TODO` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `COMPLETED`).
  3. Dashboard metric counters reflect updated tallies.
  4. Cross-tenant request receives `HTTP 404 Not Found`.

