-- =============================================================================
-- Migration: V1_0__init_notification_schema.sql
-- Service: notification-service
-- Database: notification_db
-- Description: Baseline schema initialization for notifications and consumed_events.
-- Conforms to ADR-002, ADR-006, DOC-DB-004, DOC-DEV-006, and RULE-07.
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- -----------------------------------------------------------------------------
-- 1. Notifications Table
-- -----------------------------------------------------------------------------
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    task_id UUID,
    event_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    CONSTRAINT chk_notifications_title_not_empty CHECK (length(trim(title)) > 0),
    CONSTRAINT chk_notifications_message_not_empty CHECK (length(trim(message)) > 0),
    CONSTRAINT chk_notifications_event_type_not_empty CHECK (length(trim(event_type)) > 0)
);

-- Partial and compound indexes for notifications query optimization
CREATE INDEX idx_notifications_user_created 
    ON notifications (user_id, created_at DESC);

CREATE INDEX idx_notifications_user_unread 
    ON notifications (user_id) 
    WHERE read = FALSE;

CREATE INDEX idx_notifications_task_id 
    ON notifications (task_id);

-- -----------------------------------------------------------------------------
-- 2. Consumed Events Table (Consumer Idempotency Gate)
-- -----------------------------------------------------------------------------
CREATE TABLE consumed_events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(50),
    consumer_group VARCHAR(100),
    consumed_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

