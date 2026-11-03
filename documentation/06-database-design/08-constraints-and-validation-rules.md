# Database Constraints and Validation Rules: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-DB-008 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Database Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-DB-004`, `DOC-LLD-011` |

---

## 1. Overview
Database-level constraints form the final, un-bypassable tier of data protection in the Task Management System. Even if client-side validation and application service checks were compromised, the PostgreSQL database engine rejects non-conforming mutations.

---

## 2. Master Database Constraints Matrix

| Database | Table Name | Constraint Name | Type | SQL Definition | Business Rule Enforced |
|---|---|---|:---:|---|---|
| `task_db` | `tasks` | `pk_tasks` | PK | `PRIMARY KEY (id)` | `BRULE-006` (UUID Identity) |
| `task_db` | `tasks` | `chk_tasks_title_non_empty` | CHECK | `CHECK (length(trim(title)) > 0)` | `BRULE-004` (Non-Blank Title) |
| `task_db` | `tasks` | `chk_tasks_status` | CHECK | `CHECK (status IN ('TODO', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))` | `BRULE-002` (Valid States) |
| `task_db` | `tasks` | `chk_tasks_priority` | CHECK | `CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))` | `BRULE-003` (Valid Priorities) |
| `task_db` | `tasks` | `nn_tasks_user_id` | NOT NULL | `user_id UUID NOT NULL` | `BRULE-001` (Multi-Tenancy) |
| `task_db` | `outbox_events` | `pk_outbox` | PK | `PRIMARY KEY (id)` | `BRULE-009` (Outbox Identity) |
| `task_db` | `outbox_events` | `chk_outbox_event_type` | CHECK | `CHECK (event_type IN ('TASK_CREATED', 'TASK_UPDATED', 'TASK_COMPLETED', 'TASK_DELETED'))` | `EVT-001` (Allowed Events) |
| `user_db` | `users` | `pk_users` | PK | `PRIMARY KEY (id)` | `BRULE-006` (UUID Identity) |
| `user_db` | `users` | `uk_users_email` | UNIQUE | `UNIQUE (email)` | `FR-001` (Unique Email) |
| `user_db` | `users` | `chk_users_status` | CHECK | `CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DEACTIVATED'))` | Account Lifecycle |
| `user_db` | `refresh_tokens` | `fk_refresh_tokens_user` | FK | `FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE` | Referential Integrity |
| `notification_db` | `notifications` | `pk_notifications` | PK | `PRIMARY KEY (id)` | `BRULE-006` (UUID Identity) |
| `notification_db` | `consumed_events` | `pk_consumed_events` | PK | `PRIMARY KEY (event_id)` | `BRULE-010` (Idempotency) |

---

## 3. PostgreSQL SQLState Error Translation

| PostgreSQL SQLState | PostgreSQL Error Category | Cause in TMS | Translated Application Error Code | HTTP Status |
|---|---|---|---|:---:|
| `23505` | `unique_violation` | Duplicate email during registration or duplicate token insertion. | `ERR_EMAIL_ALREADY_EXISTS` | 409 Conflict |
| `23514` | `check_violation` | Invalid status string, blank title, or illegal priority. | `ERR_VALIDATION_FAILED` | 400 Bad Request |
| `23502` | `not_null_violation` | Missing mandatory field (e.g. `user_id` or `title`). | `ERR_VALIDATION_FAILED` | 400 Bad Request |
| `23503` | `foreign_key_violation` | Inserting refresh token for non-existent user. | `ERR_RESOURCE_NOT_FOUND` | 404 Not Found |
| `40001` | `serialization_failure` | Concurrent transactions conflict during outbox row update. | Retried by Spring `@Retryable` | 500 / Auto-retry |

---

## 4. Review Checklist
- [x] All 13 core database constraints across 3 databases cataloged.
- [x] Check constraints align strictly with Java domain enums.
- [x] PostgreSQL SQLState error translation mapped to RFC 7807 responses.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Database Architect | Initial baseline Database Constraints document. |

