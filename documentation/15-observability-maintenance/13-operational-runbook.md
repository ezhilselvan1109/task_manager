# Operational Runbook

**Document Identifier:** DOC-OBS-013  
**Traceability References:** NFR-007, DOC-INF-013, DOC-DB-013  
**Scope:** Day-2 Operational Maintenance & Administrative Procedures  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This runbook defines the recurring administrative maintenance procedures, database vacuum schedules, outbox table archival routines, and container hygiene tasks required to sustain long-term operational health across the Task Management platform.

---

## 2. Maintenance Cadence Schedule

| Task Name | Target Subsystem | Execution Frequency | Maintenance Command / Script |
|---|---|---|---|
| **Outbox Table Archival** | `task_db` | Daily (Off-Peak) | Purge processed outbox rows older than 30 days. |
| **Consumed Events Cleanup**| `notification_db` | Weekly | Purge deduplication keys older than 30 days. |
| **PostgreSQL VACUUM** | All Databases | Weekly (Sunday 02:00 UTC) | Reclaim dead row space & refresh query planner stats. |
| **Docker Engine Prune** | Host Workstation | Bi-Weekly | Remove unused container layers and dangling volumes. |
| **Kafka Retention Audit** | Apache Kafka | Monthly | Verify topic segment retention complies with 7-day policy. |

---

## 3. Detailed Maintenance Execution Steps

### 3.1 Outbox Table Archival & Purging (`task_db`)
To prevent `outbox_events` from growing indefinitely and slowing down index scans:
```sql
-- Purge processed events older than 30 days in batches of 5000
DELETE FROM outbox_events 
WHERE id IN (
    SELECT id FROM outbox_events 
    WHERE processed = TRUE 
      AND processed_at < NOW() - INTERVAL '30 days' 
    LIMIT 5000
);
```

### 3.2 Consumed Events Archival (`notification_db`)
```sql
DELETE FROM consumed_events 
WHERE processed_at < NOW() - INTERVAL '30 days';
```

### 3.3 Database Optimization (`VACUUM ANALYZE`)
Execute weekly against all 3 logical databases:
```powershell
docker exec -i taskmanager-postgresql psql -U postgres -d user_db -c "VACUUM ANALYZE;"
docker exec -i taskmanager-postgresql psql -U postgres -d task_db -c "VACUUM ANALYZE;"
docker exec -i taskmanager-postgresql psql -U postgres -d notification_db -c "VACUUM ANALYZE;"
```

### 3.4 Docker Disk Hygiene
Reclaim unreferenced image layers on developer host:
```powershell
docker system prune -f --volumes=false
```
*(Volumes flag set to false guarantees persistent database records are never accidentally deleted)*.

