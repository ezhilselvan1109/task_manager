# Rule 05: REST API & Security Standards

## REST API Design Rules
1. **URI Conventions**:
   - Resource URIs must be plural lowercase nouns: `/api/v1/tasks`, `/api/v1/users`, `/api/v1/notifications`.
   - Use standard HTTP methods:
     - `POST` for creation (`201 Created` with `Location` header).
     - `GET` for retrieval (`200 OK`).
     - `PUT` for complete resource replacement (`200 OK`).
     - `PATCH` for partial resource updates (`200 OK`).
     - `DELETE` for removal (`204 No Content`).
2. **Pagination & Sorting**:
   - All collection queries (`GET /api/v1/tasks`, `GET /api/v1/notifications`) must support `page` (0-indexed, default 0), `size` (default 20, max 100), `sort` (e.g. `createdAt,desc`).
   - Paginated responses must return standard envelopes containing `content`, `page`, `size`, `totalElements`, and `totalPages`.

## Security & Anti-Enumeration Rules
1. **Anti-Enumeration Mandate**:
   - Accessing, modifying, or deleting a resource ID that belongs to another tenant MUST return `HTTP 404 Not Found`.
   - Never return `HTTP 403 Forbidden` for ID lookups. Returning `403` reveals that the resource exists, enabling IDOR harvesting.
   - Always formulate SQL/JPQL queries with tenant scoping: `WHERE id = :taskId AND user_id = :userId`.
2. **Stateless JWT Authentication**:
   - Algorithm: HMAC-SHA256 (`HS256`) with a 256-bit secret key.
   - Access token lifetime: 15 minutes.
   - Microservices trust downstream `X-User-Id` and `X-User-Role` headers injected by the verified Gateway, or validate the Bearer token locally.
3. **Password Security**:
   - Passwords must be hashed using BCrypt with work factor (cost) 12.
   - Passwords must never be logged, printed, or returned in any API response or DTO.
   - Enforce password policy: minimum 8 characters, at least 1 uppercase, 1 lowercase, 1 number, 1 special character.

