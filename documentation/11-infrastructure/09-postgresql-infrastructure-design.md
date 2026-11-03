# PostgreSQL Infrastructure Design

**Document Identifier:** DOC-INF-009  
**Traceability References:** ADR-002, NFR-007, DOC-INF-005  
**Database Engine:** PostgreSQL 16.2 on Alpine Linux  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the PostgreSQL container infrastructure supporting the Database-per-Service architecture. A single PostgreSQL container hosts three logically isolated databases (`user_db`, `task_db`, `notification_db`), each owned by dedicated database user roles with zero cross-database query permissions.

---

## 2. Multi-Database Initialization Script

Mounted in `docker-compose.yml` to `/docker-entrypoint-initdb.d/init-multi-db.sh`:

```bash
#!/bin/bash
set -e

# Creates dedicated roles and databases for each microservice
function create_user_and_database() {
    local database=$1
    local user=$2
    local password=$3
    echo "Creating database '$database' and user '$user'..."
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        CREATE USER $user WITH PASSWORD '$password';
        CREATE DATABASE $database OWNER $user;
        GRANT ALL PRIVILEGES ON DATABASE $database TO $user;
EOSQL
}

# Execute creation for all 3 microservices
create_user_and_database "user_db" "user_app" "${DB_USER_PASSWORD:-user_password_secret}"
create_user_and_database "task_db" "task_app" "${DB_TASK_PASSWORD:-task_password_secret}"
create_user_and_database "notification_db" "notification_app" "${DB_NOTIF_PASSWORD:-notification_password_secret}"

echo "Multi-database provisioning completed successfully."
```

---

## 3. PostgreSQL Performance & Connection Tuning

Configured in `infrastructure/postgres/postgresql.conf`:

| Parameter | Configured Value | Rationale |
|---|---|---|
| `shared_buffers` | `128MB` | Fits within the 512MB container limit while caching active table indexes. |
| `work_mem` | `4MB` | Sufficient memory for sorting and hashing without triggering disk spill. |
| `maintenance_work_mem` | `32MB` | Speeds up Flyway DDL index creation and VACUUM operations. |
| `max_connections` | `60` | Accommodates 3 HikariCP pools ($10 + 15 + 10 = 35$) plus administrative connections. |
| `statement_timeout` | `6000ms` | Automatically aborts runaway queries exceeding 6 seconds. |

---

## 4. Volume Persistence & Backup Mounts

- **Named Volume**: `postgres_data:/var/lib/postgresql/data`
- **Persistence Guarantee**: Stopping or removing containers (`docker compose down`) preserves all database records. Data is only wiped if the user explicitly supplies the `-v` (volumes) flag.
- **Backup Script Mount**: `infrastructure/backup:/backup` allows automated `pg_dump` snapshots to be written directly to host storage.

