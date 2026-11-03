# Rule 03: Spring Cloud & API Gateway Standards

## Spring Cloud Gateway Directives
1. **Edge Entry Point**:
   - `api-gateway` (Port 8080) is the sole public-facing ingress point for frontend and client HTTP requests.
   - Microservices (`user-service`, `task-service`, `notification-service`) must not expose their ports to external untrusted networks in production.
2. **Gateway Routing**:
   - Dynamic service discovery routing using Eureka IDs (`lb://user-service`, `lb://task-service`, `lb://notification-service`).
   - Route path mappings:
     - `/api/v1/auth/**`, `/api/v1/users/**` $\rightarrow$ `lb://user-service`
     - `/api/v1/tasks/**` $\rightarrow$ `lb://task-service`
     - `/api/v1/notifications/**` $\rightarrow$ `lb://notification-service`
3. **Gateway Authentication Filter**:
   - The Gateway executes a custom reactive filter (`JwtAuthenticationFilter`) on all protected routes.
   - Public paths (e.g. `/api/v1/auth/register`, `/api/v1/auth/login`, `/actuator/health`) bypass token validation.
   - On valid JWT validation, the filter extracts `sub` (User UUID) and `role`, mutating the downstream request headers with:
     - `X-User-Id: <UUID>`
     - `X-User-Role: <ROLE>`
4. **Correlation ID Filter**:
   - Generate a UUID for `X-Correlation-ID` if absent in the incoming request header.
   - Forward `X-Correlation-ID` to the downstream service and reflect it in the HTTP response header.
5. **Rate Limiting**:
   - Enforce Token Bucket rate limiting on `/api/v1/auth/**` (e.g. 10 requests/minute per IP) to mitigate brute-force attacks.

