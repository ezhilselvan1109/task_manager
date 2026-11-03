# HTTP Status Code Catalogue: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-API-006 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | API Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-API-002`, `DOC-API-003` |

---

## 1. Overview
The Task Management System adheres to RFC 9110 HTTP semantics. HTTP status codes communicate outcome status deterministically to client applications.

---

## 2. HTTP Status Codes Catalogue

| Status Code | Meaning | Usage in TMS APIs | Example Scenario | Client Action |
|:---:|---|---|---|---|
| **200 OK** | Success | Standard response for successful `GET`, `PUT`, `PATCH`. | Fetching task list, toggling task status to `COMPLETED`. | Render returned payload. |
| **201 Created** | Created | Resource successfully created via `POST`. | Successful task creation, user registration. | Extract `Location` header or payload `id`. |
| **204 No Content** | Success (No Body) | Operation succeeded with empty response body. | Soft deleting a task via `DELETE /tasks/{id}`. | Remove item from local state. |
| **400 Bad Request** | Client Error | Payload failed Bean Validation, malformed JSON, or illegal status enum. | Submitting blank `title` or invalid `status` string. | Inspect `fieldErrors` array; display validation errors to user. |
| **401 Unauthorized** | Auth Failure | Missing, invalid, or expired Bearer JWT access token. | Accessing `/tasks` without Authorization header or with expired token. | Execute `/auth/refresh` or redirect user to `/login`. |
| **403 Forbidden** | Authorization Denied | Authenticated user lacks permission or account is deactivated. | Deactivated user attempts API interaction. | Display account suspended notice. |
| **404 Not Found** | Resource Missing | Requested UUID does not exist or belongs to another user. | User B attempts `GET /tasks/101` where 101 is owned by User A. | Display "Resource not found" (prevents ID enumeration). |
| **409 Conflict** | State Conflict | Uniqueness constraint violation. | Registering with an email that already exists. | Inform user email is registered; suggest login. |
| **429 Too Many Requests** | Rate Limited | Client exceeded Gateway rate limiter threshold. | Submitting $> 10$ registrations per minute. | Wait duration indicated in `Retry-After` header. |
| **500 Internal Server Error** | Server Error | Unhandled runtime exception or DB connection failure. | Outbox database down during transaction. | Log error with `correlationId`; prompt user to retry. |
| **503 Service Unavailable** | Edge Service Down | Gateway cannot resolve target service in Eureka registry. | All instances of `task-service` stopped. | Display maintenance / retry banner. |
| **504 Gateway Timeout** | Edge Timeout | Upstream microservice took $> 5000\text{ms}$ to respond. | Database query stall during heavy load. | Gateway circuit breaker triggers fallback response. |

---

## 3. Review Checklist
- [x] All 12 system HTTP status codes cataloged with standard semantics.
- [x] Explicit guidance for 404 security information hiding (cross-tenant rejection).
- [x] Client recovery actions documented for every status code.

---

## 4. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | API Architect | Initial baseline HTTP Status Code Catalogue. |

