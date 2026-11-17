-- V1_0__init_task_schema.sql
-- Task Service Database Baseline Schema & Indexes
-- Target Database: task_db (MySQL 8.4 LTS, per ADR-009)
-- Traceability: DOC-DB-004, DOC-DB-007, ADR-005, ADR-009

-- 1. Tasks Table
CREATE TABLE tasks (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    due_date DATETIME(6),
    is_deleted BIT(1) NOT NULL DEFAULT b'0',
    deleted_at DATETIME(6),
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT chk_tasks_status CHECK (status IN ('TODO', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_tasks_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT chk_tasks_title_non_empty CHECK (CHAR_LENGTH(TRIM(title)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. Transactional Outbox Events Table (supports FOR UPDATE SKIP LOCKED in MySQL 8.4 InnoDB)
CREATE TABLE outbox_events (
    id CHAR(36) PRIMARY KEY,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    processed BIT(1) NOT NULL DEFAULT b'0',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    processed_at DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. Performance Composite Indexes
CREATE INDEX idx_tasks_user_active ON tasks (user_id, is_deleted);
CREATE INDEX idx_tasks_user_status ON tasks (user_id, is_deleted, status);
CREATE INDEX idx_tasks_user_priority ON tasks (user_id, is_deleted, priority);
CREATE INDEX idx_tasks_user_due_date ON tasks (user_id, is_deleted, due_date);
CREATE INDEX idx_outbox_pending_poller ON outbox_events (processed, created_at ASC);
CREATE INDEX idx_outbox_aggregate ON outbox_events (aggregate_id);
