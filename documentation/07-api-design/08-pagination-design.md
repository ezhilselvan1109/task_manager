# Pagination Design: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-API-008 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | API Architect & Database Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-API-003`, `DOC-LLD-008` |

---

## 1. Overview & Protocol Specification
Pagination prevents large data transfers from exhausting client memory and network bandwidth. The Task Management System implements standard **Zero-Based Offset Pagination** supported by Spring Data JPA `Pageable` and PostgreSQL `LIMIT / OFFSET`.

---

## 2. Query Parameters

| Parameter | Type | Required? | Default | Bounds | Description |
|---|---|:---:|:---:|:---:|---|
| `page` | `integer` | No | `0` | $\ge 0$ | Zero-based index of the target page (`0` = first page). |
| `size` | `integer` | No | `20` | $1 \le \text{size} \le 100$ | Number of records to return per page. Max capped at 100. |

---

## 3. Standard `PageResponse<T>` Envelope

```json
{
  "content": [
    {
      "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Review Microservice Security Architecture",
      "status": "TODO",
      "priority": "HIGH",
      "dueDate": "2026-11-10T18:00:00Z",
      "isOverdue": false,
      "createdAt": "2026-11-03T11:00:00Z",
      "updatedAt": "2026-11-03T11:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3,
  "isLast": false
}
```

### Field Definitions
- `content`: Array of serialized resource DTOs for the requested page.
- `page`: Current zero-based page number.
- `size`: Number of records requested per page.
- `totalElements`: Total active records matching the filter criteria across all pages.
- `totalPages`: Calculated total pages ($\lceil \text{totalElements} / \text{size} \rceil$).
- `isLast`: Boolean flag indicating if this is the final page (`page == totalPages - 1`).

---

## 4. Database Query Optimization
To prevent heavy full-table count scans, Spring Data JPA executes:
```sql
-- 1. Paginated slice query utilizing composite index
SELECT * FROM tasks 
WHERE user_id = :userId AND is_deleted = false 
ORDER BY created_at DESC 
LIMIT :size OFFSET (:page * :size);

-- 2. Scoped count query using partial index
SELECT COUNT(id) FROM tasks 
WHERE user_id = :userId AND is_deleted = false;
```
Both queries leverage `idx_tasks_user_active`, ensuring execution in $< 5\text{ ms}$.

---

## 5. Review Checklist
- [x] Zero-based pagination contract with size bound (max 100) established.
- [x] Symmetrical JSON pagination metadata defined.
- [x] Index-backed database query plan verified.

---

## 6. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | API Architect | Initial baseline Pagination Design. |

