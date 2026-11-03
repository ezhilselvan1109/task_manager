# Backup and Disaster Recovery Runbook

**Document Identifier:** DOC-OBS-014  
**Traceability References:** NFR-002, DOC-DB-012, DOC-INF-009  
**Objectives:** RPO $\le 1$ hour, RTO $\le 15$ minutes  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This runbook provides the definitive operational procedures for executing automated database backups, verifying snapshot integrity, and restoring datastores in disaster recovery scenarios.

---

## 2. Backup and Recovery Objectives

- **Recovery Point Objective (RPO)**: $\le \mathbf{1\text{ hour}}$ (Maximum acceptable data loss window).
- **Recovery Time Objective (RTO)**: $\le \mathbf{15\text{ minutes}}$ (Time required to restore database state and resume operations).

---

## 3. Automated Backup Script (`backup-all-db.sh`)

```bash
#!/bin/bash
set -e

BACKUP_DIR="/backup/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

echo "Initiating backup for all Task Management databases..."

for DB in user_db task_db notification_db; do
    echo "Dumping database: $DB ..."
    pg_dump -U postgres -d "$DB" --format=custom --compress=9 --file="$BACKUP_DIR/${DB}.dump"
done

echo "Backup completed successfully. Stored in $BACKUP_DIR"
```

---

## 4. Disaster Recovery Restoration Procedure

### Step 1: Stop Application Traffic
Stop write traffic to prevent data corruption while database is recovering:
```powershell
docker compose stop user-service task-service notification-service
```

### Step 2: Drop and Recreate Target Database
```powershell
# Example: Restoring task_db
docker exec -i taskmanager-postgresql psql -U postgres -c "DROP DATABASE task_db WITH (FORCE);"
docker exec -i taskmanager-postgresql psql -U postgres -c "CREATE DATABASE task_db OWNER task_app;"
```

### Step 3: Restore Database Snapshot via `pg_restore`
```powershell
docker exec -i taskmanager-postgresql pg_restore -U postgres -d task_db /backup/20261103_230000/task_db.dump
```

### Step 4: Restart Application Microservices & Verify
```powershell
docker compose start user-service task-service notification-service
curl http://localhost:8080/actuator/health
```
Verify Actuator status returns `{"status":"UP"}` with database connection confirmed.

