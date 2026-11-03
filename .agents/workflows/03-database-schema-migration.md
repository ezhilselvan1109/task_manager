# Workflow 03: Database Schema and Migration

## 1. Overview & Purpose
Defines the safe, versioned procedure for modifying relational database schemas using Flyway in PostgreSQL 16.

- **Workflow ID**: `WF-003`
- **Trigger**: New table requirement, column addition, index creation, or constraint alteration.

## 2. Preconditions
- Target microservice and PostgreSQL database running or available via Testcontainers.
- Previous Flyway migration version established and intact.

## 3. Required Inputs
- Target service module path.
- Next version identifier (e.g. `V1_1`).
- DDL SQL statements.

## 4. Execution Steps
1. **Determine Version Number**:
   - Inspect `src/main/resources/db/migration/`.
   - Increment minor or patch version (e.g., `V1_1__add_due_date_index.sql`).
2. **Draft SQL Script**:
   - Write standard PostgreSQL DDL:
     - Use `snake_case` column names.
     - Use `UUID` for foreign and primary keys.
     - Use `TIMESTAMPTZ` for timestamps.
     - Include indexes on `user_id` and query filters.
3. **Update JPA Entity**:
   - Add matching fields to the `@Entity` class in `entity` package.
4. **Execute Flyway Migration**:
   - Run Maven test with Testcontainers or local PostgreSQL to verify Flyway migration executes without error.
5. **Verify Hibernate Schema Validation**:
   - Confirm Spring Boot starts with `spring.jpa.hibernate.ddl-auto=validate` with zero mapping discrepancies.

## 5. Skills to Use
- [`flyway-database-migrations`](file:///d:/learning/task_manager/.agents/skills/flyway-database-migrations/SKILL.md)
- [`postgresql-database-design`](file:///d:/learning/task_manager/.agents/skills/postgresql-database-design/SKILL.md)
- [`spring-data-jpa`](file:///d:/learning/task_manager/.agents/skills/spring-data-jpa/SKILL.md)

## 6. Rules to Follow
- [Rule 04: Database & Flyway Standards](file:///d:/learning/task_manager/.agents/rules/04-database-and-flyway.md)
- [Rule 01: Architecture & Stack](file:///d:/learning/task_manager/.agents/rules/01-architecture-and-stack.md)

## 7. Tests and Validation
- Integration test with Testcontainers executing the new migration script against real PostgreSQL.
- Verify entry created in `flyway_schema_history` with `success = true`.

## 8. Failure Handling
- If migration checksum fails, verify no past migration files were edited.
- If Flyway fails with syntax error, repair script before committing.

## 9. Review Checklist
- [ ] Script uses double underscore syntax (`V<Version>__<Description>.sql`).
- [ ] No cross-database foreign key constraints.
- [ ] All timestamps use `TIMESTAMPTZ`.
- [ ] Indexes created on foreign keys and filter fields.

## 10. Expected Outputs
- New migration file in `db/migration/`.
- Updated JPA entity.
- Passing integration test against PostgreSQL.

