# Requirements Traceability Matrix (RTM)

| Field | Value |
|---|---|
| **Document ID** | REG-RTM-001 |
| **Project** | Task Management System – Microservices Architecture |
| **Current Status** | Active Baseline (Maintained Incrementally) |
| **Last Updated** | 2026-11-03 |

---

## 1. Traceability Framework & Identifier Conventions

Traceability ensures that every line of system architecture, code, schema, API endpoint, Kafka event, and test case stems from a validated business requirement.

```
Business Requirement (BR-xxx)
  └── Functional Requirement (FR-xxx) / Non-Functional Requirement (NFR-xxx)
        ├── Business Rule (BRULE-xxx)
        ├── User Story (US-xxx)
        └── Use Case (UC-xxx)
              ├── Architecture Component (ARCH-xxx)
              ├── API Specification (API-xxx)
              ├── Database Schema (DBR-xxx)
              ├── Event Specification (EVT-xxx)
              └── Test Case (TC-xxx)
```

### Identifier Prefixes
- `BR-xxx`: Business Requirements
- `FR-xxx`: Functional Requirements
- `NFR-xxx`: Non-Functional Requirements
- `BRULE-xxx`: Business Rules
- `US-xxx`: User Stories
- `UC-xxx`: Use Cases
- `ARCH-xxx`: Architecture Components
- `API-xxx`: API Endpoints / Contracts
- `DBR-xxx`: Database Requirements / Tables
- `EVT-xxx`: Kafka Event Schemas / Topics
- `SEC-xxx`: Security Requirements
- `TC-xxx`: Test Cases & Acceptance Scenarios
- `ADR-xxx`: Architecture Decision Records

---

## 2. Master Traceability Matrix

| BR ID | Business Requirement Description | FR / NFR ID | User Story ID | Use Case ID | Architecture Component | API Endpoint ID | Database Entity | Kafka Event | Test Case ID | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| **BR-001** | Secure User Registration & Lifecycle | FR-001, FR-002, SEC-001 | US-001 | UC-001 | User Service, API Gateway | API-USER-001 (`POST /auth/register`) | `users` | N/A (MVP) | TC-USR-001, TC-USR-002 | Baseline |
| **BR-002** | Secure User Authentication & Token Issuance | FR-003, FR-004, SEC-002 | US-002, US-003 | UC-002, UC-003 | User Service, API Gateway | API-USER-002 (`POST /auth/login`), API-USER-003 (`POST /auth/refresh`) | `refresh_tokens` | N/A | TC-AUTH-001, TC-AUTH-002 | Baseline |
| **BR-003** | User Profile Management | FR-005, FR-006, SEC-003 | US-004 | UC-004 | User Service, API Gateway | API-USER-004 (`GET /users/me`), API-USER-005 (`PUT /users/me`) | `users` | N/A | TC-USR-003, TC-USR-004 | Baseline |
| **BR-004** | Core Task Lifecycle Management | FR-007, FR-008, FR-009, FR-010 | US-005, US-006, US-007 | UC-005, UC-006, UC-007 | Task Service, API Gateway | API-TASK-001 (`POST /tasks`), API-TASK-002 (`GET /tasks/{id}`), API-TASK-003 (`PUT /tasks/{id}`), API-TASK-004 (`DELETE /tasks/{id}`) | `tasks`, `outbox_events` | EVT-TASK-001 (`TASK_CREATED`), EVT-TASK-002 (`TASK_UPDATED`), EVT-TASK-004 (`TASK_DELETED`) | TC-TSK-001, TC-TSK-002, TC-TSK-003 | Baseline |
| **BR-005** | Task State & Priority Transitions | FR-011, FR-012, BRULE-002, BRULE-003 | US-008, US-009 | UC-008 | Task Service | API-TASK-005 (`PATCH /tasks/{id}/status`), API-TASK-006 (`PATCH /tasks/{id}/priority`) | `tasks`, `outbox_events` | EVT-TASK-003 (`TASK_COMPLETED`), EVT-TASK-002 (`TASK_UPDATED`) | TC-TSK-004, TC-TSK-005 | Baseline |
| **BR-006** | Task Querying, Search, Filtering & Pagination | FR-013, FR-014, FR-015, FR-016 | US-010, US-011 | UC-009 | Task Service | API-TASK-007 (`GET /tasks`) | `tasks` | N/A | TC-TSK-006, TC-TSK-007 | Baseline |
| **BR-007** | Dashboard Metrics & Productivity Summary | FR-017, BRULE-007 | US-012 | UC-010 | Task Service | API-TASK-008 (`GET /tasks/metrics/summary`) | `tasks` | N/A | TC-DSH-001, TC-DSH-002 | Baseline |
| **BR-008** | Asynchronous In-App Notifications | FR-018, FR-019, FR-020, EVT-001 | US-013, US-014 | UC-011, UC-012 | Notification Service, Kafka Cluster | API-NOTIF-001 (`GET /notifications`), API-NOTIF-002 (`PATCH /notifications/{id}/read`), API-NOTIF-003 (`PATCH /notifications/read-all`) | `notifications`, `consumed_events` | EVT-TASK-001, EVT-TASK-002, EVT-TASK-003, EVT-TASK-004 | TC-NTF-001, TC-NTF-002, TC-NTF-003 | Baseline |
| **BR-009** | Strict Multi-Tenant Data Isolation | NFR-001, SEC-004, BRULE-001 | US-015 | UC-ALL | API Gateway, Task Service, Notification Service | Filter Chain (`UserContextHolder`) | All Service DBs (`user_id` partition column) | Event Envelope (`userId`) | TC-SEC-001, TC-SEC-002 | Baseline |
| **BR-010** | High Availability, Resilience & Observability | NFR-002 to NFR-007 | US-016 | UC-OPS | Eureka, Gateway, Prometheus, Grafana, Micrometer | Actuator (`/actuator/health`, `/actuator/prometheus`) | All Services | Kafka Health Check | TC-RES-001, TC-RES-002, TC-OBS-001 | Baseline |

---

## 3. Maintenance Process
As each Phase is authored:
1. New granular items (e.g. detailed Database constraints `DBR-xxx` in Phase 6, API paths `API-xxx` in Phase 7) are hyperlinked back to their parent `FR-xxx` and `BR-xxx`.
2. Traceability gaps are flagged during phase validation checks and documented in the respective Phase Review Report.

