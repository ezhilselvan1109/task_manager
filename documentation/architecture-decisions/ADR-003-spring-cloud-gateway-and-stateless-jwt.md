# ADR-003: Spring Cloud Gateway with Stateless JWT Authentication

| Metadata Item | Details |
|---|---|
| **ADR ID** | ADR-003 |
| **Status** | Approved |
| **Date** | 2026-11-03 |
| **Deciders** | Security Architect, Solution Architect |
| **Related Documents** | `DOC-ARCH-001`, `DOC-ARCH-009` |

---

## 1. Context & Problem Statement
Microservices require authentication and authorization enforcement without introducing stateful session replication or forcing every downstream service to independently connect to the user database to verify credentials.

## 2. Decision
Deploy **Spring Cloud Gateway** as the single edge ingress reverse proxy, coupled with **Stateless JSON Web Tokens (JWT)**:
1. The Gateway intercepts all incoming requests to protected routes (`/api/v1/tasks/**`, `/api/v1/notifications/**`, `/api/v1/users/**`).
2. Gateway verifies the JWT cryptographic signature (HMAC-SHA256 with 256-bit secret or RSA public key) and expiration timestamp.
3. Upon successful validation, the Gateway extracts the user identifier (`sub` claim) and injects an `X-User-Id: <uuid>` HTTP header before routing upstream.
4. Downstream services trust the `X-User-Id` header provided by the gateway, eliminating redundant user database lookups.

## 3. Consequences
- **Positive**:
  - Horizontal scalability: zero session state stored in gateway or microservices.
  - Performance: microservices avoid remote authentication calls on every request.
  - Centralized security perimeter: unauthenticated or expired tokens are rejected at the edge (HTTP 401).
- **Negative / Trade-offs**:
  - Immediate token revocation challenge: mitigated by configuring short Access Token lifetimes (15 minutes) and rotating Refresh Tokens persisted in `user_db`.

