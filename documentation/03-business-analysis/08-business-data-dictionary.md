# Business Data Dictionary: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-BA-008 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Database Architect & Senior Business Analyst |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-REQ-004`, `DOC-REQ-009` |

---

## 1. Overview
The Business Data Dictionary defines the business terms, entity definitions, attributes, data types, constraints, and valid domain values across all three autonomous microservice databases.

---

## 2. User Service Domain (`user_db`)

### 2.1 Entity: `User` (Table: `users`)
Represents an individual who owns an account within the system.

| Field Name | Business Definition | Technical Type | Nullable? | Constraints / Allowed Values | Sample Value |
|---|---|---|:---:|---|---|
| `id` | Unique identifier of the user account. | `UUID` | No | Primary Key, UUID v4 | `550e8400-e29b-41d4-a716-446655440000` |
| `email` | Registered email address used for login. | `VARCHAR(255)` | No | Unique, valid RFC 5322 format | `alex.turner@example.com` |
| `password_hash` | Cryptographic hash of the user's password. | `VARCHAR(255)` | No | BCrypt hash string ($2a$ / $2b$, 60 chars) | `$2a$12$e8YQJ1rLg3qE...` |
| `full_name` | User's display name. | `VARCHAR(100)` | No | 1..100 characters | `Alex Turner` |
| `status` | Operational state of the user account. | `VARCHAR(20)` | No | `ACTIVE`, `SUSPENDED`, `DEACTIVATED` | `ACTIVE` |
| `created_at` | Timestamp when the user registered. | `TIMESTAMPTZ` | No | UTC ISO-8601 | `2026-11-03T10:15:30Z` |
| `updated_at` | Timestamp of last profile update. | `TIMESTAMPTZ` | No | UTC ISO-8601 | `2026-11-03T10:15:30Z` |

### 2.2 Entity: `RefreshToken` (Table: `refresh_tokens`)
Tracks active and rotated session refresh tokens.

| Field Name | Business Definition | Technical Type | Nullable? | Constraints / Allowed Values | Sample Value |
|---|---|---|:---:|---|---|
| `id` | Identifier of token record. | `UUID` | No | Primary Key | `a1b2c3d4-e5f6-7890-abcd-ef1234567890` |
| `user_id` | Foreign identifier referencing user. | `UUID` | No | Indexed | `550e8400-e29b-41d4-a716-446655440000` |
| `token` | Opaque cryptographically secure random string. | `VARCHAR(255)` | No | Unique | `d8a9f3b7c2e140...` |
| `expires_at` | UTC timestamp when token becomes invalid. | `TIMESTAMPTZ` | No | Expiration $> \text{now()}$ | `2026-11-10T10:15:30Z` |
| `revoked` | Boolean flag indicating if token has been invalidated. | `BOOLEAN` | No | Default `false` | `false` |

---

## 3. Task Service Domain (`task_db`)

### 3.1 Entity: `Task` (Table: `tasks`)
Represents an individual work item owned by a user.

| Field Name | Business Definition | Technical Type | Nullable? | Constraints / Allowed Values | Sample Value |
|---|---|---|:---:|---|---|
| `id` | Unique task identifier. | `UUID` | No | Primary Key | `7c9e6679-7425-40de-944b-e07fc1f90ae7` |
| `user_id` | Identifier of the task owner. | `UUID` | No | Partition/Filter key (B-Tree indexed) | `550e8400-e29b-41d4-a716-446655440000` |
| `title` | Brief summary of the work item. | `VARCHAR(255)` | No | 1..255 chars, not blank | `Finalize Database Per Service Spec` |
| `description` | Detailed notes or instructions. | `TEXT` | Yes | Max 2000 characters | `Document Flyway migration scripts...` |
| `status` | Current lifecycle state of the task. | `VARCHAR(20)` | No | `TODO`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` | `TODO` |
| `priority` | Urgency classification. | `VARCHAR(20)` | No | `LOW`, `MEDIUM`, `HIGH`, `URGENT` | `HIGH` |
| `due_date` | Target deadline for task completion. | `TIMESTAMPTZ` | Yes | UTC ISO-8601 | `2026-11-08T18:00:00Z` |
| `is_deleted` | Soft deletion flag. | `BOOLEAN` | No | Default `false` | `false` |
| `deleted_at` | Timestamp when task was soft deleted. | `TIMESTAMPTZ` | Yes | UTC ISO-8601 | `null` |
| `created_at` | Timestamp when task was created. | `TIMESTAMPTZ` | No | UTC ISO-8601 | `2026-11-03T11:00:00Z` |
| `updated_at` | Timestamp when task was last modified. | `TIMESTAMPTZ` | No | UTC ISO-8601 | `2026-11-03T11:00:00Z` |

### 3.2 Entity: `OutboxEvent` (Table: `outbox_events`)
Captures domain events produced by task state mutations for reliable Kafka dispatch.

| Field Name | Business Definition | Technical Type | Nullable? | Constraints / Allowed Values | Sample Value |
|---|---|---|:---:|---|---|
| `id` | Unique event identifier. | `UUID` | No | Primary Key | `8d0e778a-8536-41ef-a55c-f18fd2g01bf8` |
| `aggregate_id` | Identifier of the affected task entity. | `VARCHAR(64)` | No | B-Tree indexed, used as Kafka key | `7c9e6679-7425-40de-944b-e07fc1f90ae7` |
| `event_type` | Name of the domain event. | `VARCHAR(50)` | No | `TASK_CREATED`, `TASK_UPDATED`, `TASK_COMPLETED`, `TASK_DELETED` | `TASK_CREATED` |
| `payload` | Serialized JSON representation of event. | `TEXT` / `JSONB` | No | Well-formed JSON envelope | `{"eventId":"...","title":"..."}` |
| `processed` | Flag indicating if Kafka acknowledged receipt. | `BOOLEAN` | No | Default `false`, indexed | `false` |
| `created_at` | Timestamp when outbox row was written. | `TIMESTAMPTZ` | No | UTC ISO-8601 | `2026-11-03T11:00:00Z` |
| `processed_at` | Timestamp when broker acknowledged publish. | `TIMESTAMPTZ` | Yes | UTC ISO-8601 | `2026-11-03T11:00:01Z` |

---

## 4. Notification Service Domain (`notification_db`)

### 4.1 Entity: `Notification` (Table: `notifications`)
Represents an in-app notice presented to a user.

| Field Name | Business Definition | Technical Type | Nullable? | Constraints / Allowed Values | Sample Value |
|---|---|---|:---:|---|---|
| `id` | Unique notification identifier. | `UUID` | No | Primary Key | `9e1f889b-9647-52fa-b66d-g29ge3h12cg9` |
| `user_id` | Identifier of the recipient user. | `UUID` | No | B-Tree indexed | `550e8400-e29b-41d4-a716-446655440000` |
| `event_type` | Type of event that triggered notification. | `VARCHAR(50)` | No | Corresponds to Kafka `eventType` | `TASK_COMPLETED` |
| `message` | Human-readable notification summary text. | `VARCHAR(500)` | No | 1..500 characters | `Task 'Database Design' was marked COMPLETED` |
| `read` | Read status indicator. | `BOOLEAN` | No | Default `false` | `false` |
| `read_at` | Timestamp when notification was viewed. | `TIMESTAMPTZ` | Yes | UTC ISO-8601 | `null` |
| `created_at` | Timestamp when notification was generated. | `TIMESTAMPTZ` | No | UTC ISO-8601 | `2026-11-03T11:00:01Z` |

### 4.2 Entity: `ConsumedEvent` (Table: `consumed_events`)
Enforces idempotent consumer deduplication.

| Field Name | Business Definition | Technical Type | Nullable? | Constraints / Allowed Values | Sample Value |
|---|---|---|:---:|---|---|
| `event_id` | Identifier of the received Kafka event. | `UUID` | No | Primary Key | `8d0e778a-8536-41ef-a55c-f18fd2g01bf8` |
| `consumed_at` | Timestamp when event was processed. | `TIMESTAMPTZ` | No | UTC ISO-8601 | `2026-11-03T11:00:01Z` |

---

## 5. Review Checklist
- [x] All 6 core tables across 3 independent databases exhaustively documented.
- [x] Field definitions, technical types, nullability, constraints, and samples provided.
- [x] Database-per-service isolation strictly maintained with zero cross-database foreign keys.

---

## 6. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Database Architect | Initial baseline Business Data Dictionary. |

