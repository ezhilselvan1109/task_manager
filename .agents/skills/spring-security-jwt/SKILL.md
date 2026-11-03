---
name: spring-security-jwt
description: >-
  Use this skill when configuring Spring Security 6.x, stateless JWT token generation and validation,
  BCrypt password hashing, and role-based access control.
---

# Spring Security & JWT Skill

## Purpose
Specifies the exact procedure to implement stateless JWT-based authentication, BCrypt password hashing (cost 12), SecurityFilterChain configuration, and downstream request validation.

## When to Use
- Implementing user registration and login in `user-service`.
- Authorizing requests and securing endpoints in microservices.
- Configuring stateless session management and CORS/CSRF policies.

## Required Inputs
- Target microservice (`user-service` or downstream services).
- JWT secret configuration (`security.jwt.secret`, `security.jwt.expiration-ms`).

## Step-by-Step Procedure
1. **Configure Password Encoder**:
   Define `BCryptPasswordEncoder` bean with cost factor 12:
   ```java
   @Bean
   public PasswordEncoder passwordEncoder() {
       return new BCryptPasswordEncoder(12);
   }
   ```
2. **Build Stateless SecurityFilterChain**:
   Configure Spring Security 6.x lambda DSL:
   ```java
   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       return http
           .csrf(AbstractHttpConfigurer::disable)
           .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
           .authorizeHttpRequests(auth -> auth
               .requestMatchers("/api/v1/auth/**", "/actuator/health/**", "/v3/api-docs/**").permitAll()
               .anyRequest().authenticated()
           )
           .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
           .build();
   }
   ```
3. **Implement JWT Utility**:
   - Generate token using `io.jsonwebtoken` (jjwt 0.12.x).
   - Set subject (`userId.toString()`), issue timestamp, expiration (15 minutes), and custom claims (`role`, `email`).
   - Sign using `Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))`.
4. **Enforce Downstream Identity Propagation**:
   - In microservices behind Gateway, read `X-User-Id` header and construct `Authentication` object in Spring SecurityContext.

## Applicable Project Standards
- [Rule 05: REST API & Security Standards](file:///d:/learning/task_manager/.agents/rules/05-rest-api-and-security.md)
- [Authentication and Authorization Design](file:///d:/learning/task_manager/documentation/09-security-resilience/02-authentication-and-authorization-design.md)

## Validation Checklist
- [ ] BCrypt cost factor is explicitly configured to 12.
- [ ] Sessions are strictly `STATELESS`.
- [ ] CSRF is disabled for stateless token-authenticated APIs.
- [ ] Expired tokens produce `401 Unauthorized`.
- [ ] Passwords are never logged or returned in responses.

## Common Mistakes
- Hardcoding the JWT secret key in version control.
- Storing plaintext passwords during registration.
- Using stateful HTTP sessions with JWT tokens.

## Completion Criteria
User registration safely hashes passwords; login issues verifiable JWT; protected endpoints reject missing or invalid tokens.

