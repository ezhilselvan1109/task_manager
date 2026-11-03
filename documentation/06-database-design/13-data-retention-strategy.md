# Data Retention Strategy: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-DB-013 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Database Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-DB-001`, `DOC-DB-005` |

---

## 1. Overview
The Data Retention Strategy balances compliance, data recovery requirements, query performance, and disk storage efficiency by formalizing lifecycle retention limits for active, transient, and soft-deleted records.

---

## 2. Retention Policy Matrix

| Entity / Table | Active Retention Period | Purge / Archival Trigger | Pruning Action | Frequency |
|---|---|---|---|---|
| **Active Tasks** (`tasks`) | Indefinite | User initiates soft deletion (`is_deleted = true`). | None while active. | N/A |
| **Soft-Deleted Tasks** (`tasks`) | 90 days from `deleted_at` | `deleted_at < now() - INTERVAL '90 days'` | Hard delete or export to cold storage archive. | Monthly batch job |
| **Processed Outbox Events** (`outbox_events`)| 7 days from `processed_at` | `processed = true AND processed_at < now() - INTERVAL '7 days'` | `DELETE FROM outbox_events WHERE ...` | Daily scheduled worker |
| **Consumed Event IDs** (`consumed_events`) | 30 days from `consumed_at` | `consumed_at < now() - INTERVAL '30 days'` | `DELETE FROM consumed_events WHERE ...` | Weekly scheduled worker |
| **In-App Notifications** (`notifications`) | 30 days from `created_at` | `created_at < now() - INTERVAL '30 days'` | `DELETE FROM notifications WHERE ...` | Weekly scheduled worker |
| **Expired Refresh Tokens** (`refresh_tokens`)| Purged on expiration | `expires_at < now() OR revoked = true` | `DELETE FROM refresh_tokens WHERE ...` | Daily scheduled worker |

---

## 3. Automated Maintenance Pruning Script
```sql
-- Daily Maintenance Cleanup Script (cron / Spring Scheduled)

-- 1. Purge acknowledged outbox events older than 7 days
DELETE FROM outbox_events 
WHERE processed = true 
  AND processed_at < now() - INTERVAL '7 days';

-- 2. Purge stale notification idempotency records older than 30 days
DELETE FROM consumed_events 
WHERE consumed_at < now() - INTERVAL '30 days';

-- 3. Purge revoked or expired refresh tokens
DELETE FROM refresh_tokens 
WHERE revoked = true 
   OR expires_at < now();
```

---

## 4. Review Checklist
- [x] Retention windows cataloged across all 6 database tables.
- [x] Soft-delete grace period (90 days) permits accidental deletion recovery.
- [x] Outbox and idempotency pruning scripts prevent table bloat.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Database Architect | Initial baseline Data Retention Strategy document. |

