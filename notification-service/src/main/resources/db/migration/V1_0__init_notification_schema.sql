-- =============================================================================
-- Migration: V1_0__init_notification_schema.sql
-- Service: notification-service
-- Database: notification_db (MySQL 8.4 LTS, per ADR-009)
-- Description: Baseline schema initialization for notifications and consumed_events.
-- Conforms to ADR-002, ADR-006, ADR-009, DOC-DB-004, DOC-DEV-006, and RULE-07.
-- =============================================================================

-- 1. Notifications Table
CREATE TABLE notifications (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    task_id CHAR(36),
    event_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    is_read BIT(1) NOT NULL DEFAULT b'0',
    read_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT chk_notifications_title_not_empty CHECK (CHAR_LENGTH(TRIM(title)) > 0),
    CONSTRAINT chk_notifications_message_not_empty CHECK (CHAR_LENGTH(TRIM(message)) > 0),
    CONSTRAINT chk_notifications_event_type_not_empty CHECK (CHAR_LENGTH(TRIM(event_type)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Performance Indexes
CREATE INDEX idx_notifications_user_created ON notifications (user_id, created_at DESC);
CREATE INDEX idx_notifications_user_unread ON notifications (user_id, is_read);
CREATE INDEX idx_notifications_task_id ON notifications (task_id);

-- 2. Consumed Events Table (Consumer Idempotency Gate)
CREATE TABLE consumed_events (
    event_id CHAR(36) PRIMARY KEY,
    event_type VARCHAR(50),
    consumer_group VARCHAR(100),
    consumed_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
