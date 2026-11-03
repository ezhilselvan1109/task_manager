# Flyway Migration Strategy: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-DB-011 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Database Architect & DevOps Lead |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-DB-004`, `ADR-004` |

---

## 1. Migration Architecture & Versioning Rules
Flyway manages database evolution across all microservices using versioned, immutable SQL scripts executed automatically upon Spring Boot application startup:
1. **Naming Standard**: `V<Major>_<Minor>__<Descriptive_Snake_Case_Name>.sql` (e.g., `V1_0__init_task_schema.sql`).
2. **Location**: `src/main/resources/db/migration` in each service repository.
3. **Immutability Guarantee**: Once applied to an environment, a migration file must never be modified. Checksums are recorded in `flyway_schema_history`. Subsequent schema alterations require a new incremental version script (`V1_1__...sql`).

---

## 2. Baseline Migration Scripts (V1.0)

### 2.1 `task-service/src/main/resources/db/migration/V1_0__init_task_schema.sql`
```sql
-- Migration: V1_0__init_task_schema.sql
-- Service: task-service | Database: task_db

CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    due_date TIMESTAMPTZ,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    CONSTRAINT chk_tasks_status CHECK (status IN ('TODO', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_tasks_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT chk_tasks_title_non_empty CHECK (length(trim(title)) > 0)
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    processed_at TIMESTAMPTZ
);

-- Partial Indexes
CREATE INDEX idx_tasks_user_active ON tasks (user_id) WHERE is_deleted = false;
CREATE INDEX idx_tasks_user_status ON tasks (user_id, status) WHERE is_deleted = false;
CREATE INDEX idx_tasks_user_priority ON tasks (user_id, priority) WHERE is_deleted = false;
CREATE INDEX idx_tasks_user_due_date ON tasks (user_id, due_date) WHERE is_deleted = false;
CREATE INDEX idx_outbox_pending_poller ON outbox_events (created_at ASC) WHERE processed = false;
CREATE INDEX idx_outbox_aggregate ON outbox_events (aggregate_id);
```

### 2.2 `user-service/src/main/resources/db/migration/V1_0__init_user_schema.sql`
```sql
-- Migration: V1_0__init_user_schema.sql
-- Service: user-service | Database: user_db

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DEACTIVATED'))
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_users_email_lower ON users (lower(email));
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
```

### 2.3 `notification-service/src/main/resources/db/migration/V1_0__init_notification_schema.sql`
```sql
-- Migration: V1_0__init_notification_schema.sql
-- Service: notification-service | Database: notification_db

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    message VARCHAR(500) NOT NULL,
    read BOOLEAN NOT NULL DEFAULT false,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE TABLE consumed_events (
    event_id UUID PRIMARY KEY,
    consumed_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE INDEX idx_notifications_user_created ON notifications (user_id, created_at DESC);
CREATE INDEX idx_notifications_user_unread ON notifications (user_id) WHERE read = false;
```

---

## 3. Flyway Configuration & Rollback Strategy
1. **Configuration**:
   ```yaml
   spring:
     flyway:
       enabled: true
       baseline-on-migrate: true
       validate-on-migrate: true
   ```
2. **Rollback Policy**: Forward-only migrations. If a bug is introduced in `V1_1`, do not manually delete or rollback; author `V1_2__fix_issue.sql` to remediate the schema deterministically.
3. **Repair Procedure**: If a migration fails mid-way in development, execute `mvn flyway:repair` to clean up the failed entry in `flyway_schema_history` before reapplying.

---

## 4. Review Checklist
- [x] Exact V1_0 Flyway SQL migration scripts documented for all 3 microservices.
- [x] Forward-only migration policy and repair procedures defined.
- [x] `flyway_schema_history` integration and checksum validation specified.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Database Architect | Initial baseline Flyway Migration Strategy document. |

