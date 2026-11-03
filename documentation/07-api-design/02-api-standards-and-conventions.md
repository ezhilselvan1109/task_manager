# API Standards and Conventions: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-API-002 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | API Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-API-001`, `DOC-LLD-008` |

---

## 1. URI Naming Standards
1. **Plural Nouns**: Resource paths use plural nouns representing collections (`/tasks`, `/notifications`, `/users`).
2. **Kebab-Case Paths**: Multi-word URI segments use lowercase kebab-case (`/api/v1/notifications/read-all`).
3. **No Verbs in URIs**: Operations are indicated by HTTP methods, never verbs in paths (e.g. use `DELETE /tasks/{id}` instead of `/tasks/deleteTask`).
4. **Hierarchical Relationships**: Sub-resources indicate child relationships (e.g., `/api/v1/tasks/{id}/status`).

---

## 2. HTTP Verb Semantics

| HTTP Verb | Idempotent? | Safe? | Usage in TMS | Success Response Code |
|---|:---:|:---:|---|:---:|
| `GET` | **Yes** | **Yes** | Read single or collection resources without side effects. | `200 OK` |
| `POST` | **No** | **No** | Create a new entity or execute non-idempotent actions (login). | `201 Created` / `200 OK` |
| `PUT` | **Yes** | **No** | Replace all mutable attributes of an existing resource. | `200 OK` |
| `PATCH` | **No** | **No** | Partial update of specific fields (status transition, priority change).| `200 OK` |
| `DELETE` | **Yes** | **No** | Soft delete a resource (`is_deleted = true`). | `204 No Content` |

---

## 3. Standard HTTP Headers

### 3.1 Request Headers
- `Content-Type`: Mandatory `application/json` for requests with bodies.
- `Accept`: `application/json`.
- `Authorization`: `Bearer <jwt_token>` on protected routes.
- `X-Correlation-Id`: Optional client UUID; if absent, Gateway generates one.

### 3.2 Response Headers
- `Content-Type`: `application/json`.
- `X-Correlation-Id`: Echoes the distributed trace UUID.
- `Location`: Included on `201 Created` responses pointing to the canonical URI of the new entity (`/api/v1/tasks/{id}`).

---

## 4. JSON Payload Standards
1. **Property Casing**: Strictly `camelCase` (e.g., `userId`, `dueDate`, `createdAt`, `totalElements`).
2. **Enumerations**: Strictly `UPPER_SNAKE_CASE` (e.g., `IN_PROGRESS`, `MEDIUM`, `ACTIVE`).
3. **Timestamps**: UTC ISO-8601 strings with trailing `Z` offset (e.g., `"2026-11-03T18:00:00Z"`).
4. **Null Handling**: Omit null fields or serialize as explicit `null`. Empty lists serialize as `[]` (never `null`).

---

## 5. Review Checklist
- [x] Clear URI naming and kebab-case standards documented.
- [x] HTTP verb semantics, idempotency, and status codes aligned.
- [x] camelCase and ISO-8601 JSON standards defined.

---

## 6. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | API Architect | Initial baseline API Standards and Conventions. |

