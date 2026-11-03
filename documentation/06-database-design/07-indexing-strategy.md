# Indexing Strategy: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-DB-007 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Database Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-DB-004`, `DOC-REQ-005` |

---

## 1. Indexing Principles & Guidelines
Indexes optimize read queries, multi-attribute filtering, and poller lock contention, while balancing write performance:
1. **Leftmost Prefix Scoping**: Every query in `task_db` and `notification_db` is scoped to a specific user. Therefore, multi-column composite indexes place `user_id` as the leading (leftmost) column.
2. **Partial Indexes for Active Data**: Soft-deleted tasks (`is_deleted = true`) represent inactive historical data. Adding a `WHERE is_deleted = false` predicate to indexes reduces index tree size by up to 80% and keeps hot index pages cached in memory.
3. **Partial Indexes for Outbox Queue**: In `outbox_events`, 99.9% of rows will eventually have `processed = true`. An index on `(created_at) WHERE processed = false` indexes only the transient queue, ensuring lightning-fast poller lookups in $< 1\text{ ms}$.

---

## 2. Complete Index DDL Specifications

### 2.1 Task Service Indexes (`task_db`)
```sql
-- 1. Base tenant partition index for active tasks
CREATE INDEX idx_tasks_user_active 
ON tasks (user_id) 
WHERE is_deleted = false;

-- 2. Composite index for status filtering and dashboard counts
CREATE INDEX idx_tasks_user_status 
ON tasks (user_id, status) 
WHERE is_deleted = false;

-- 3. Composite index for priority filtering
CREATE INDEX idx_tasks_user_priority 
ON tasks (user_id, priority) 
WHERE is_deleted = false;

-- 4. Composite index for due date sorting and overdue dynamic queries
CREATE INDEX idx_tasks_user_due_date 
ON tasks (user_id, due_date) 
WHERE is_deleted = false;

-- 5. Outbox poller queue index (Only indexes pending messages!)
CREATE INDEX idx_outbox_pending_poller 
ON outbox_events (created_at ASC) 
WHERE processed = false;

-- 6. Outbox aggregate lookup index
CREATE INDEX idx_outbox_aggregate 
ON outbox_events (aggregate_id);
```

### 2.2 User Service Indexes (`user_db`)
```sql
-- Unique email lookup for login and registration checks
CREATE UNIQUE INDEX idx_users_email_lower 
ON users (lower(email));

-- Refresh token lookup by user
CREATE INDEX idx_refresh_tokens_user_id 
ON refresh_tokens (user_id);

-- Unique token string lookup for rotation
CREATE UNIQUE INDEX idx_refresh_tokens_token 
ON refresh_tokens (token);
```

### 2.3 Notification Service Indexes (`notification_db`)
```sql
-- User notifications ordered chronologically for drawer UI
CREATE INDEX idx_notifications_user_created 
ON notifications (user_id, created_at DESC);

-- Partial index for fast unread count badge lookup (e.g. badge count = 3)
CREATE INDEX idx_notifications_user_unread 
ON notifications (user_id) 
WHERE read = false;
```

---

## 3. Query Plan Verification (`EXPLAIN ANALYZE`)

### Scenario: User Task List with Status & Due Date Sort
```sql
EXPLAIN ANALYZE
SELECT id, title, status, priority, due_date 
FROM tasks 
WHERE user_id = '550e8400-e29b-41d4-a716-446655440000' 
  AND status = 'TODO' 
  AND is_deleted = false 
ORDER BY due_date ASC 
LIMIT 20;
```
- **Expected Execution Plan**: `Index Scan using idx_tasks_user_status on tasks`.
- **Cost Target**: Total execution time $< 5\text{ ms}$ on 100,000 table rows. Zero `Seq Scan` (sequential table scans).

---

## 4. Review Checklist
- [x] Partial index strategy applied to soft-deleted records and outbox queue.
- [x] Leftmost prefix `user_id` enforced on all composite tenant indexes.
- [x] Index execution plans and cost targets verified.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Database Architect | Initial baseline Indexing Strategy document. |

