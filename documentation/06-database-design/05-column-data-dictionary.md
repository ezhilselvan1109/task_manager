# Column Data Dictionary: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-DB-005 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Database Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-DB-004`, `DOC-BA-008` |

---

## 1. Overview
This document specifies the storage layout, technical data types, byte allocations, nullability, defaults, constraints, and descriptions for every database column across all microservices.

---

## 2. Table: `tasks` (`task_db`)

| Column Name | PostgreSQL Type | Storage Size | Nullable? | Default | Key / Index | Constraint / Allowed Values | Description |
|---|---|---|:---:|---|---|---|---|
| `id` | `UUID` | 16 bytes | No | `gen_random_uuid()` | PK | UUID v4 | Immutable primary identifier. |
| `user_id` | `UUID` | 16 bytes | No | None | Index (B-Tree) | Valid UUID | Tenant partition key; references user. |
| `title` | `VARCHAR(255)` | 1..256 bytes | No | None | None | `length(trim(title)) > 0` | Mandatory task title. |
| `description` | `TEXT` | Variable (max ~2KB) | Yes | None | None | Max 2000 chars | Optional detailed notes. |
| `status` | `VARCHAR(20)` | ~15 bytes | No | `'TODO'` | Composite Index | `TODO, IN_PROGRESS, COMPLETED, CANCELLED` | Lifecycle state enum. |
| `priority` | `VARCHAR(20)` | ~10 bytes | No | `'MEDIUM'` | Composite Index | `LOW, MEDIUM, HIGH, URGENT` | Task urgency level. |
| `due_date` | `TIMESTAMPTZ` | 8 bytes | Yes | None | Index (B-Tree) | UTC ISO-8601 | Target deadline. |
| `is_deleted` | `BOOLEAN` | 1 byte | No | `false` | Partial Index | `true, false` | Soft deletion flag. |
| `deleted_at` | `TIMESTAMPTZ` | 8 bytes | Yes | None | None | UTC ISO-8601 | Soft deletion timestamp. |
| `version` | `BIGINT` | 8 bytes | No | `0` | None | Non-negative integer | Optimistic locking counter. |
| `created_at` | `TIMESTAMPTZ` | 8 bytes | No | `now() AT TIME ZONE 'utc'` | None | UTC ISO-8601 | Creation audit timestamp. |
| `updated_at` | `TIMESTAMPTZ` | 8 bytes | No | `now() AT TIME ZONE 'utc'` | None | UTC ISO-8601 | Last modification timestamp. |

*Estimated Average Row Size*: $\approx 180\text{ bytes}$ (uncompressed). 100,000 tasks $\approx 18\text{ MB}$ raw table storage.

---

## 3. Table: `outbox_events` (`task_db`)

| Column Name | PostgreSQL Type | Storage Size | Nullable? | Default | Key / Index | Description |
|---|---|---|:---:|---|---|---|
| `id` | `UUID` | 16 bytes | No | None | PK | Unique event identifier. |
| `aggregate_id` | `VARCHAR(64)` | ~40 bytes | No | None | Index | Task UUID (Kafka partition key). |
| `event_type` | `VARCHAR(50)` | ~20 bytes | No | None | None | Domain event name (`TASK_CREATED`, etc.). |
| `payload` | `TEXT` | ~500 bytes | No | None | None | Full JSON serialized event envelope. |
| `processed` | `BOOLEAN` | 1 byte | No | `false` | Composite Index | Poller publication flag (`true/false`). |
| `created_at` | `TIMESTAMPTZ` | 8 bytes | No | `now() AT TIME ZONE 'utc'` | Composite Index | Outbox insertion timestamp. |
| `processed_at` | `TIMESTAMPTZ` | 8 bytes | Yes | None | None | Kafka broker ACK timestamp. |

---

## 4. Tables: `users` & `refresh_tokens` (`user_db`)

### `users`
- `id` (`UUID`, 16 bytes, PK)
- `email` (`VARCHAR(255)`, ~30 bytes, UNIQUE NOT NULL)
- `password_hash` (`VARCHAR(255)`, 60 bytes, NOT NULL)
- `full_name` (`VARCHAR(100)`, ~20 bytes, NOT NULL)
- `status` (`VARCHAR(20)`, 10 bytes, NOT NULL, DEFAULT `'ACTIVE'`)
- `created_at`, `updated_at` (`TIMESTAMPTZ`, 8 bytes each, NOT NULL)

### `refresh_tokens`
- `id` (`UUID`, 16 bytes, PK)
- `user_id` (`UUID`, 16 bytes, FK to `users(id) ON DELETE CASCADE`)
- `token` (`VARCHAR(255)`, ~40 bytes, UNIQUE NOT NULL)
- `expires_at` (`TIMESTAMPTZ`, 8 bytes, NOT NULL)
- `revoked` (`BOOLEAN`, 1 byte, NOT NULL, DEFAULT `false`)
- `created_at` (`TIMESTAMPTZ`, 8 bytes, NOT NULL)

---

## 5. Tables: `notifications` & `consumed_events` (`notification_db`)

### `notifications`
- `id` (`UUID`, 16 bytes, PK)
- `user_id` (`UUID`, 16 bytes, Index, NOT NULL)
- `event_type` (`VARCHAR(50)`, ~20 bytes, NOT NULL)
- `message` (`VARCHAR(500)`, ~80 bytes, NOT NULL)
- `read` (`BOOLEAN`, 1 byte, NOT NULL, DEFAULT `false`)
- `read_at` (`TIMESTAMPTZ`, 8 bytes, Nullable)
- `created_at` (`TIMESTAMPTZ`, 8 bytes, NOT NULL)

### `consumed_events`
- `event_id` (`UUID`, 16 bytes, PK)
- `consumed_at` (`TIMESTAMPTZ`, 8 bytes, NOT NULL)

---

## 6. Review Checklist
- [x] All columns across 6 tables cataloged with physical types, byte sizes, nullability, and defaults.
- [x] Row size and storage footprint estimations calculated.
- [x] UTC timestamp standards verified for all temporal columns.

---

## 7. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Database Architect | Initial baseline Column Data Dictionary. |

