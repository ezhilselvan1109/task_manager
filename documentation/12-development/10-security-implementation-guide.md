# Security Implementation Guide

**Document Identifier:** DOC-DEV-010  
**Traceability References:** SEC-001 to SEC-010, DOC-SEC-002, DOC-SEC-003  
**Target Services:** All Platform Microservices  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the concrete Spring Security implementation classes, filter chains, JWT cryptographic utilities, and internal header verification adapters across the platform.

---

## 2. JWT Cryptographic Service (`user-service`)

```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final long EXPIRATION_TIME_MS = 15 * 60 * 1000; // 15 minutes

    public String generateToken(UUID userId, String username, String role) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();

        return Jwts.builder()
                .issuer("task-manager-auth")
                .subject(username)
                .claim("userId", userId.toString())
                .claim("role", role)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(EXPIRATION_TIME_MS)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
```

---

## 3. Downstream Header Authentication Filter (`task-service`, `notification-service`)

Downstream microservices verify trusted identity context injected by the Gateway:

```java
@Component
public class InternalHeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String userIdHeader = request.getHeader("X-User-Id");
        String roleHeader = request.getHeader("X-User-Role");

        if (userIdHeader != null && !userIdHeader.isBlank()) {
            UUID userId = UUID.fromString(userIdHeader);
            String role = roleHeader != null ? roleHeader : "ROLE_USER";

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
            PreAuthenticatedAuthenticationToken authentication = 
                    new PreAuthenticatedAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            MDC.put("userId", userId.toString());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("userId");
        }
    }
}
```

---

## 4. Downstream Security Configuration

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class DownstreamSecurityConfig {

    private final InternalHeaderAuthenticationFilter headerFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(headerFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
```

