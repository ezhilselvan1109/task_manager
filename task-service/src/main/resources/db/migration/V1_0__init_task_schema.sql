-- V1_0__init_task_schema.sql
-- Task Service Database Baseline Schema & Indexes
-- Target Database: task_db (PostgreSQL 16)
-- Traceability: DOC-DB-004, DOC-DB-007, ADR-005

-- 1. Tasks Table
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

-- 2. Transactional Outbox Events Table
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    processed_at TIMESTAMPTZ
);

-- 3. Performance Indexes
-- Base tenant partition index for active tasks
CREATE INDEX idx_tasks_user_active 
ON tasks (user_id) 
WHERE is_deleted = false;

-- Composite index for status filtering and dashboard counts
CREATE INDEX idx_tasks_user_status 
ON tasks (user_id, status) 
WHERE is_deleted = false;

-- Composite index for priority filtering
CREATE INDEX idx_tasks_user_priority 
ON tasks (user_id, priority) 
WHERE is_deleted = false;

-- Composite index for due date sorting and overdue dynamic queries
CREATE INDEX idx_tasks_user_due_date 
ON tasks (user_id, due_date) 
WHERE is_deleted = false;

-- Outbox poller queue index for efficient SKIP LOCKED polling
CREATE INDEX idx_outbox_pending_poller 
ON outbox_events (created_at ASC) 
WHERE processed = false;

-- Outbox aggregate lookup index
CREATE INDEX idx_outbox_aggregate 
ON outbox_events (aggregate_id);

