---
name: flyway-database-migrations
description: >-
  Use this skill when authoring, versioning, executing, or troubleshooting Flyway SQL database
  migrations in microservice modules.
---

# Flyway Database Migrations Skill

## Purpose
Governs the creation, versioning, execution, and validation of Flyway database migration scripts across all microservices.

## When to Use
- Adding a new table, column, index, or constraint to any microservice database.
- Initializing the baseline schema for `user-service`, `task-service`, or `notification-service`.
- Troubleshooting migration checksum errors or failed migrations.

## Required Inputs
- Target microservice migration path (`<service>/src/main/resources/db/migration/`).
- Next migration version number (e.g., `V1_0`, `V1_1`).
- DDL SQL statements.

## Step-by-Step Procedure
1. **Identify Current Migration Version**:
   - Inspect files in `src/main/resources/db/migration/`.
   - Determine next sequential semantic version (e.g., `V1_0__create_tasks_table.sql` $\rightarrow$ `V1_1__add_task_tags.sql`).
2. **Author the Migration File**:
   - File name pattern: `V<Major>_<Minor>__<description_in_snake_case>.sql` (note double underscore).
   - Ensure script is strictly idempotent or fail-safe (e.g., `CREATE TABLE IF NOT EXISTS`, `CREATE INDEX IF NOT EXISTS`).
3. **Set JPA Validation Mode**:
   In `application.yml`:
   ```yaml
   spring:
     jpa:
       hibernate:
         ddl-auto: validate
     flyway:
       enabled: true
       baseline-on-migrate: true
       locations: classpath:db/migration
   ```
4. **Execute and Validate Migration**:
   - Run Maven test or local startup to trigger Flyway migration:
     `mvn test-compile flyway:migrate` or start container.
   - Verify `flyway_schema_history` table in the target database.
5. **Never Edit Past Migrations**:
   - Once a migration script has been applied, its checksum is locked. Any changes must be written as a subsequent version.

## Applicable Project Standards
- [Rule 04: Database & Flyway Standards](file:///d:/learning/task_manager/.agents/rules/04-database-and-flyway.md)
- [Database Migration Strategy](file:///d:/learning/task_manager/documentation/06-database-design/08-database-migration-strategy.md)

## Validation Checklist
- [ ] File naming matches `V<Version>__<Description>.sql` with double underscore.
- [ ] Never modify an existing, committed migration file.
- [ ] `ddl-auto` is set to `validate`, never `create` or `update`.
- [ ] Migration runs cleanly on an empty database.

## Common Mistakes
- Using a single underscore between version and description (`V1_0_description.sql` will be ignored by Flyway).
- Modifying a past migration file, causing checksum validation failure on boot.

## Completion Criteria
Migration executes without error, records in `flyway_schema_history`, and JPA entity mapping validates cleanly against the resulting schema.

