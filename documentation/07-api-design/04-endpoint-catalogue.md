# Master Endpoint Catalogue: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-API-004 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | API Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-API-003`, `REG-RTM-001` |

---

## 1. Master Endpoint Catalogue Table

| Endpoint ID | Microservice | HTTP Method | Route URI | Summary & Operation | Auth Guard | Rate Limit Tier | Bound Req ID |
|---|---|:---:|---|---|:---:|:---:|---|
| **API-USER-001** | `user-service` | `POST` | `/api/v1/auth/register` | Register new user account | Public | 10 req/min | `FR-001`, `BR-001` |
| **API-USER-002** | `user-service` | `POST` | `/api/v1/auth/login` | Authenticate credentials & return JWT | Public | 15 req/min | `FR-003`, `BR-002` |
| **API-USER-003** | `user-service` | `POST` | `/api/v1/auth/refresh` | Rotate refresh token & issue access token | Public | 30 req/min | `FR-004`, `BR-002` |
| **API-USER-004** | `user-service` | `GET` | `/api/v1/users/me` | Retrieve profile of authenticated user | Bearer JWT | 100 req/min | `FR-005`, `BR-003` |
| **API-USER-005** | `user-service` | `PUT` | `/api/v1/users/me` | Update profile attributes (full name) | Bearer JWT | 30 req/min | `FR-006`, `BR-003` |
| **API-TASK-001** | `task-service` | `POST` | `/api/v1/tasks` | Create personal task & queue outbox event | Bearer JWT | 60 req/min | `FR-007`, `BR-004` |
| **API-TASK-002** | `task-service` | `GET` | `/api/v1/tasks/{id}` | Retrieve task detail by UUID | Bearer JWT | 200 req/min | `FR-008`, `BR-004` |
| **API-TASK-003** | `task-service` | `PUT` | `/api/v1/tasks/{id}` | Replace task title, desc, priority, dueDate | Bearer JWT | 60 req/min | `FR-009`, `BR-004` |
| **API-TASK-004** | `task-service` | `DELETE`| `/api/v1/tasks/{id}` | Soft delete task & queue outbox event | Bearer JWT | 30 req/min | `FR-010`, `BR-004` |
| **API-TASK-005** | `task-service` | `PATCH` | `/api/v1/tasks/{id}/status` | Transition state (TODO, IN_PROGRESS, etc.) | Bearer JWT | 60 req/min | `FR-011`, `BR-005` |
| **API-TASK-006** | `task-service` | `PATCH` | `/api/v1/tasks/{id}/priority`| Update priority (LOW, MEDIUM, HIGH, URGENT)| Bearer JWT | 60 req/min | `FR-012`, `BR-005` |
| **API-TASK-007** | `task-service` | `GET` | `/api/v1/tasks` | Search, filter, sort & paginate tasks | Bearer JWT | 150 req/min | `FR-013` to `FR-016` |
| **API-TASK-008** | `task-service` | `GET` | `/api/v1/tasks/metrics/summary`| Calculate productivity counts & overdue | Bearer JWT | 100 req/min | `FR-017`, `BR-007` |
| **API-NOTIF-001**| `notification-service` | `GET` | `/api/v1/notifications` | List user in-app notifications | Bearer JWT | 100 req/min | `FR-020`, `BR-008` |
| **API-NOTIF-002**| `notification-service` | `PATCH` | `/api/v1/notifications/{id}/read`| Mark single notification as read | Bearer JWT | 60 req/min | `FR-020`, `BR-008` |
| **API-NOTIF-003**| `notification-service` | `PATCH` | `/api/v1/notifications/read-all` | Bulk mark all user notifications as read | Bearer JWT | 20 req/min | `FR-020`, `BR-008` |

---

## 2. Review Checklist
- [x] All 16 system endpoints uniquely cataloged with `API-xxx` IDs.
- [x] Target microservice, HTTP verb, path, auth requirement, and rate limits defined.
- [x] Traceability mapped to Functional and Business requirements.

---

## 3. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | API Architect | Initial baseline Endpoint Catalogue. |

