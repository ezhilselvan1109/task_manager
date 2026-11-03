# Rule 04: Database & Flyway Migration Standards

## PostgreSQL 16 Principles
1. **Physical Isolation**:
   - Independent databases: `user_db`, `task_db`, `notification_db`.
   - Separate database user credentials per service (`user_svc`, `task_svc`, `notif_svc`).
2. **Schema & Table Conventions**:
   - Tables and columns must use `snake_case` in lowercase (e.g. `due_date`, `user_id`, `created_at`).
   - Primary keys must be UUID v4 stored as native PostgreSQL `UUID` (`DEFAULT gen_random_uuid()`).
   - Timestamps must use `TIMESTAMP WITH TIME ZONE` (`TIMESTAMPTZ`) and default to `CURRENT_TIMESTAMP AT TIME ZONE 'UTC'`.
   - Enums must be stored as `VARCHAR(30)` with `CHECK` constraints (e.g. `CHECK (status IN ('TODO', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))`).

## Flyway Migration Standards
1. **Migration Location**:
   - Placed in `src/main/resources/db/migration/` within each microservice module.
2. **Naming Convention**:
   - Versioned migrations: `V<Version>__<Description>.sql` (two underscores, e.g. `V1_0__create_tasks_table.sql`).
   - Never alter a previously executed migration file. All schema evolutions must be new versioned files (e.g. `V1_1__add_tags_to_tasks.sql`).
3. **Hibernate DDL Configuration**:
   - `spring.jpa.hibernate.ddl-auto` must be set to `validate` in production and test environments.
   - Never set `ddl-auto` to `create`, `create-drop`, or `update`. Flyway is the sole owner of schema mutations.
4. **Performance & Indexes**:
   - Every foreign tenant key (`user_id`) must have a B-tree index.
   - Composite indexes must be created for high-frequency queries (e.g. `idx_tasks_user_status` on `tasks(user_id, status)`).

