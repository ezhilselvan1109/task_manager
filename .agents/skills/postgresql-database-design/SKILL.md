---
name: postgresql-database-design
description: >-
  Use this skill when designing or modifying PostgreSQL 16 database schemas, constraints,
  data types, indexes, and database-per-service isolation boundaries.
---

# PostgreSQL Database Design Skill

## Purpose
Establishes relational modeling rules, naming conventions, data types, indexing strategies, and database-per-service isolation for PostgreSQL 16.

## When to Use
- Designing new tables, columns, indexes, or constraints.
- Reviewing schema normalization (3NF) and foreign key relationships within a single service.
- Ensuring compliance with tenant isolation standards.

## Required Inputs
- Target database (`user_db`, `task_db`, or `notification_db`).
- Entity attributes and relationship cardinalities.

## Step-by-Step Procedure
1. **Enforce Database-per-Service Boundaries**:
   - Verify that tables in `user_db` (`users`), `task_db` (`tasks`, `outbox_events`), and `notification_db` (`notifications`, `consumed_events`) never cross-reference each other with database foreign keys.
2. **Apply Column Naming & Data Type Standards**:
   - Identifiers: `UUID` primary keys defaulting to `gen_random_uuid()`.
   - Timestamps: `TIMESTAMPTZ` defaulting to `CURRENT_TIMESTAMP AT TIME ZONE 'UTC'`.
   - Strings: Specific `VARCHAR(N)` or `TEXT`.
   - Booleans: `BOOLEAN NOT NULL DEFAULT FALSE`.
   - Enums: `VARCHAR(30)` backed by `CHECK` constraints.
3. **Design Essential Indexes**:
   - Primary key index on `id`.
   - B-Tree index on tenant identity: `CREATE INDEX idx_<table_name>_user_id ON <table_name>(user_id);`
   - Composite indexes for common query filters:
     ```sql
     CREATE INDEX idx_tasks_user_status ON tasks(user_id, status) WHERE is_deleted = FALSE;
     CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
     ```
4. **Enforce Soft Deletion on Core Entities**:
   - Include `is_deleted BOOLEAN NOT NULL DEFAULT FALSE` and `deleted_at TIMESTAMPTZ`.

## Applicable Project Standards
- [Rule 04: Database & Flyway Standards](file:///d:/learning/task_manager/.agents/rules/04-database-and-flyway.md)
- [Database Architecture Document](file:///d:/learning/task_manager/documentation/06-database-design/01-database-architecture-document.md)

## Validation Checklist
- [ ] Primary keys use PostgreSQL `UUID`.
- [ ] All timestamps use `TIMESTAMPTZ`.
- [ ] No cross-database foreign keys.
- [ ] Tenant `user_id` column is indexed on every tenant-owned table.

## Common Mistakes
- Using `TIMESTAMP` instead of `TIMESTAMPTZ`.
- Using auto-incrementing integers (`BIGSERIAL`) for IDs instead of UUIDs.
- Forgetting indexes on foreign keys or status filter columns.

## Completion Criteria
Schema designs conform to 3NF, enforce strict data integrity constraints, and optimize query access paths with indexes.

