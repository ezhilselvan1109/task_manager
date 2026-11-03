# Workflow 07: Authentication and Authorization Implementation

## 1. Overview & Purpose
Defines the procedure for implementing user registration, BCrypt credential hashing, stateless JWT issuance, and API Gateway route authorization.

- **Workflow ID**: `WF-007`
- **Trigger**: Implementation of `user-service` auth endpoints and Gateway security filters.

## 2. Preconditions
- `users` table created in `user_db` with unique email constraint.
- JWT secret key and expiration configured in environment variables.

## 3. Required Inputs
- Registration DTO (`email`, `password`, `firstName`, `lastName`).
- Login DTO (`email`, `password`).

## 4. Execution Steps
1. **Implement User Registration**:
   - Validate email format and password strength rules.
   - Check email uniqueness; throw `ConflictException` (409) if duplicate.
   - Hash password with `BCryptPasswordEncoder(12)`.
   - Save user entity and return `UserResponse` (excluding password hash).
2. **Implement User Login & Token Generation**:
   - Authenticate credentials via `AuthenticationManager` / BCrypt comparison.
   - On success, generate signed JWT token (HMAC-SHA256, 15m expiration, claims: `sub=userId`, `role`, `email`).
   - Return `AuthResponse(accessToken, tokenType, expiresIn, user)`.
3. **Configure API Gateway Token Validation**:
   - In `api-gateway`, implement `JwtAuthenticationFilter`.
   - Verify token signature, extract `userId` and `role`, inject `X-User-Id` downstream.
4. **Implement Client-Side Logout**:
   - Discard JWT from frontend memory/storage.

## 5. Skills to Use
- [`spring-security-jwt`](file:///d:/learning/task_manager/.agents/skills/spring-security-jwt/SKILL.md)
- [`spring-cloud-api-gateway`](file:///d:/learning/task_manager/.agents/skills/spring-cloud-api-gateway/SKILL.md)
- [`rest-api-design-implementation`](file:///d:/learning/task_manager/.agents/skills/rest-api-design-implementation/SKILL.md)

## 6. Rules to Follow
- [Rule 05: REST API & Security Standards](file:///d:/learning/task_manager/.agents/rules/05-rest-api-and-security.md)
- [Rule 12: Security & Documentation Standards](file:///d:/learning/task_manager/.agents/rules/12-security-and-documentation.md)

## 7. Tests and Validation
- Unit tests verifying BCrypt hashing and invalid password rejection (`401`).
- Gateway filter tests verifying missing/expired token rejection (`401`).

## 8. Failure Handling
- If secret key is too short, verify minimum 256-bit key requirement for `HS256`.

## 9. Review Checklist
- [ ] BCrypt cost factor is 12.
- [ ] Passwords are never returned in responses or logged.
- [ ] Token is stateless with 15-minute expiration.

## 10. Expected Outputs
- Registration & login endpoints.
- JWT generation & validation components.
- Passing security test suite.

