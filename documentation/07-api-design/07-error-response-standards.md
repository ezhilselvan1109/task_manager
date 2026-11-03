# Error Response Standards: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-API-007 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | API Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-API-002`, `DOC-LLD-010` |

---

## 1. Overview & RFC 7807 Problem Details
The Task Management System standardizes all error payloads using RFC 7807 Problem Details. Clients can parse error codes and field validation issues deterministically across all microservices and the API Gateway.

```json
{
  "timestamp": "2026-11-03T15:10:00Z",
  "status": 400,
  "error": "Bad Request",
  "errorCode": "ERR_VALIDATION_FAILED",
  "message": "Input validation failed for 2 field(s)",
  "path": "/api/v1/tasks",
  "correlationId": "8f3b2a1c-9d4e-4f7a-8b1e-6c5d4e3f2a1b",
  "fieldErrors": [
    {
      "field": "title",
      "rejectedValue": "",
      "message": "Task title must not be blank"
    },
    {
      "field": "priority",
      "rejectedValue": "SUPER_HIGH",
      "message": "Invalid priority. Allowed values: LOW, MEDIUM, HIGH, URGENT"
    }
  ]
}
```

---

## 2. Standard Error Payload Field Specifications

| Field Name | Type | Mandatory? | Description | Sample Value |
|---|---|:---:|---|---|
| `timestamp` | `string (date-time)` | Yes | UTC timestamp when error occurred. | `2026-11-03T15:10:00Z` |
| `status` | `integer` | Yes | HTTP status code matching header. | `400` |
| `error` | `string` | Yes | Standard HTTP reason phrase. | `Bad Request` |
| `errorCode` | `string` | Yes | Machine-readable application error identifier. | `ERR_VALIDATION_FAILED` |
| `message` | `string` | Yes | Human-readable error summary. | `Input validation failed` |
| `path` | `string` | Yes | Request URI where error occurred. | `/api/v1/tasks` |
| `correlationId` | `string (uuid)` | Yes | Distributed tracing UUID for support triage. | `8f3b2a1c-9d4e...` |
| `fieldErrors` | `array` | No | List of field-level constraint violations (400 only). | `[{ "field": "...", ... }]` |

---

## 3. Concrete Error Payload Examples

### 3.1 404 Resource Not Found (Multi-Tenant Protection)
```json
{
  "timestamp": "2026-11-03T15:12:00Z",
  "status": 404,
  "error": "Not Found",
  "errorCode": "ERR_RESOURCE_NOT_FOUND",
  "message": "Task with ID '7c9e6679-7425-40de-944b-e07fc1f90ae7' not found",
  "path": "/api/v1/tasks/7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "correlationId": "8f3b2a1c-9d4e-4f7a-8b1e-6c5d4e3f2a1b",
  "fieldErrors": null
}
```

### 3.2 409 Conflict (Duplicate Registration)
```json
{
  "timestamp": "2026-11-03T15:15:00Z",
  "status": 409,
  "error": "Conflict",
  "errorCode": "ERR_EMAIL_ALREADY_EXISTS",
  "message": "An account with email 'sarah.connor@example.com' already exists",
  "path": "/api/v1/auth/register",
  "correlationId": "8f3b2a1c-9d4e-4f7a-8b1e-6c5d4e3f2a1b",
  "fieldErrors": null
}
```

---

## 4. Review Checklist
- [x] RFC 7807 compliance verified across all error responses.
- [x] Distributed `correlationId` present in all error payloads.
- [x] Sensitive stack traces and raw SQL exceptions completely suppressed.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | API Architect | Initial baseline Error Response Standards. |

