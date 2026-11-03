# Requirements Traceability Linkage: Phase 2 Baseline

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-REQ-011 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Quality Assurance Lead & Senior Business Analyst |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `REG-RTM-001`, `DOC-REQ-003`, `DOC-REQ-004`, `DOC-REQ-006` |

---

## 1. Overview
This document records the end-to-end traceability linkage established at the conclusion of Phase 2 (Requirements Gathering and Analysis), ensuring that every functional requirement, user story, and use case maps directly to business goals and downstream architectural specifications.

---

## 2. Phase 2 Comprehensive Traceability Mapping

| Business Req | Functional Req | User Story | Use Case | Business Rules | Architecture Component | Downstream API Target | Target DB Entity | Target Kafka Event | Test Case Ref |
|---|---|---|---|---|---|---|---|---|---|
| **BR-001** | FR-001, FR-002 | US-001 | UC-001 | BRULE-006, BRULE-008 | User Service | `POST /auth/register` | `users` | N/A | TC-USR-001, TC-USR-002 |
| **BR-002** | FR-003, FR-004 | US-002, US-003 | UC-002, UC-003 | BRULE-006, BRULE-008 | User Service, Gateway | `POST /auth/login`, `POST /auth/refresh` | `refresh_tokens` | N/A | TC-AUTH-001, TC-AUTH-002 |
| **BR-003** | FR-005, FR-006 | US-004 | UC-004 | BRULE-001 | User Service | `GET /users/me`, `PUT /users/me` | `users` | N/A | TC-USR-003, TC-USR-004 |
| **BR-004** | FR-007 | US-005 | UC-005 | BRULE-001, 002, 003, 004, 006, 009 | Task Service | `POST /tasks` | `tasks`, `outbox_events` | `TASK_CREATED` | TC-TSK-001 |
| **BR-004** | FR-008 | US-006 | UC-006 | BRULE-001 | Task Service | `GET /tasks/{id}` | `tasks` | N/A | TC-TSK-002 |
| **BR-004** | FR-009 | US-007 | UC-007 | BRULE-001, 004, 009 | Task Service | `PUT /tasks/{id}` | `tasks`, `outbox_events` | `TASK_UPDATED` | TC-TSK-003 |
| **BR-004** | FR-010 | US-008 | UC-009 | BRULE-001, 005, 009 | Task Service | `DELETE /tasks/{id}` | `tasks`, `outbox_events` | `TASK_DELETED` | TC-TSK-004 |
| **BR-005** | FR-011 | US-009 | UC-008 | BRULE-001, 002, 009 | Task Service | `PATCH /tasks/{id}/status` | `tasks`, `outbox_events` | `TASK_COMPLETED` / `TASK_UPDATED` | TC-TSK-005 |
| **BR-005** | FR-012 | US-010 | UC-008 | BRULE-001, 003, 009 | Task Service | `PATCH /tasks/{id}/priority` | `tasks`, `outbox_events` | `TASK_UPDATED` | TC-TSK-006 |
| **BR-006** | FR-013, FR-014 | US-011 | UC-006 | BRULE-001 | Task Service | `GET /tasks?query=..&status=..` | `tasks` | N/A | TC-TSK-007 |
| **BR-006** | FR-015, FR-016 | US-012 | UC-006 | BRULE-001 | Task Service | `GET /tasks?page=..&sort=..` | `tasks` | N/A | TC-TSK-008 |
| **BR-007** | FR-017 | US-013 | UC-010 | BRULE-001, BRULE-007 | Task Service | `GET /tasks/metrics/summary` | `tasks` | N/A | TC-DSH-001 |
| **BR-008** | FR-018 | US-016 | UC-011 | BRULE-009 | Task Service (Poller) | Kafka Poller (`task.events.v1`) | `outbox_events` | `task.events.v1` | TC-EVT-001 |
| **BR-008** | FR-019 | US-016 | UC-012 | BRULE-010 | Notification Service | Kafka Listener | `consumed_events`, `notifications` | Consumes `task.events.v1` | TC-NTF-001 |
| **BR-008** | FR-020 | US-014, US-015 | UC-012 | BRULE-001 | Notification Service | `GET /notifications`, `PATCH /read` | `notifications` | N/A | TC-NTF-002, TC-NTF-003 |
| **BR-009** | NFR-001 | US-006, US-008 | All Use Cases | BRULE-001 | Gateway & All Services | `X-User-Id` Context Filter | All DB Schemas | Envelope `userId` | TC-SEC-001 |
| **BR-010** | NFR-002 to 007 | All Stories | Ops Cases | BRULE-007 | All Microservices | `/actuator/health`, `/actuator/prometheus` | Prometheus Datastore | Kafka Broker Metrics | TC-OBS-001, TC-RES-001 |

---

## 3. Traceability Health Check
- **Orphan Requirements**: 0 (Every FR maps to at least one BR and one US).
- **Untraceable Features**: 0 (All features originate from confirmed PRD/SRS scope).
- **Test Case Allocation**: 100% of functional requirements have designated verification targets.

---

## 4. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | QA Lead | Initial baseline requirements traceability linkage. |

