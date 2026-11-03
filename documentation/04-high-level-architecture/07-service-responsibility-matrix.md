# Service Responsibility Matrix: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-ARCH-007 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Solution Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-ARCH-001`, `DOC-ARCH-002`, `REG-RTM-001` |

---

## 1. Overview
The Service Responsibility Matrix defines unambiguous operational boundaries, exclusive data ownership, published/consumed event contracts, and functional mandates for each service in the microservices architecture.

---

## 2. Master Responsibility Matrix

| Microservice | Bounded Context | Exclusive DB Ownership | Owned DB Tables | Exposed REST API Routes | Kafka Events Produced | Kafka Events Consumed | External Dependencies |
|---|---|---|---|---|---|---|---|
| **API Gateway** (`api-gateway`) | Ingress & Security Edge | Stateless (No DB) | None | `/api/v1/**` (Reverse proxy routes) | None | None | Eureka Server |
| **Service Discovery** (`service-discovery`) | Platform Infrastructure | In-Memory Registry | None | `/eureka/**` (Registration endpoints) | None | None | None |
| **User Service** (`user-service`) | Identity & Credentials | `user_db` (PostgreSQL) | • `users`<br>• `refresh_tokens` | • `POST /api/v1/auth/register`<br>• `POST /api/v1/auth/login`<br>• `POST /api/v1/auth/refresh`<br>• `GET /api/v1/users/me`<br>• `PUT /api/v1/users/me` | None (MVP) | None | PostgreSQL `user_db`, Eureka Server |
| **Task Service** (`task-service`) | Task Lifecycle & Operations | `task_db` (PostgreSQL) | • `tasks`<br>• `outbox_events` | • `POST /api/v1/tasks`<br>• `GET /api/v1/tasks/{id}`<br>• `PUT /api/v1/tasks/{id}`<br>• `DELETE /api/v1/tasks/{id}`<br>• `PATCH /api/v1/tasks/{id}/status`<br>• `PATCH /api/v1/tasks/{id}/priority`<br>• `GET /api/v1/tasks`<br>• `GET /api/v1/tasks/metrics/summary` | Topic: `task.events.v1`<br>• `TASK_CREATED`<br>• `TASK_UPDATED`<br>• `TASK_COMPLETED`<br>• `TASK_DELETED` | None | PostgreSQL `task_db`, Kafka Broker, Eureka Server |
| **Notification Service** (`notification-service`) | In-App Notifications | `notification_db` (PostgreSQL) | • `notifications`<br>• `consumed_events` | • `GET /api/v1/notifications`<br>• `PATCH /api/v1/notifications/{id}/read`<br>• `PATCH /api/v1/notifications/read-all` | Dead Letter Topic (on failure):<br>• `task.events.v1.DLT` | Topic: `task.events.v1`<br>Group: `notification-service-group` | PostgreSQL `notification_db`, Kafka Broker, Eureka Server |

---

## 3. Strict Boundary Rules
1. **No Shared Tables**: `user-service` cannot access `tasks` table; `task-service` cannot access `users` table; `notification-service` cannot access `outbox_events`.
2. **No Synchronous Database Links (DB Links)**: Cross-database joins via SQL or PostgreSQL foreign data wrappers (FDW) are strictly prohibited.
3. **No Direct Inter-Service Synchronous HTTP Calls in Critical Mutation Paths**: When a user creates or completes a task, `task-service` does NOT invoke `notification-service` via HTTP. It solely writes to its local outbox.

---

## 4. Review Checklist
- [x] Clear ownership matrix covering all 5 services.
- [x] Exclusive database and table ownership designated.
- [x] REST routes, Kafka production, and Kafka consumption cataloged.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Solution Architect | Initial baseline Service Responsibility Matrix. |

