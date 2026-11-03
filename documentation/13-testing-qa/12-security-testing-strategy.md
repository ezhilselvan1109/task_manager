# Security Testing Strategy

**Document Identifier:** DOC-TEST-012  
**Traceability References:** SEC-001 to SEC-010, DOC-SEC-009, DOC-SEC-010  
**Testing Scope:** OWASP Top 10, IDOR Probing, Cryptographic Token Verification  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the automated security testing strategy for the Task Management platform. Security test suites actively probe for multi-tenant data leakage, token tampering, header injection vulnerabilities, and injection attacks to ensure platform defenses are impenetrable prior to release.

---

## 2. Security Test Matrix

| Threat Category | Test Technique | Automated Assertion |
|---|---|---|
| **Insecure Direct Object Reference (IDOR)** | Authenticated User A queries `GET /api/v1/tasks/{task_b_id}` | Returns `HTTP 404 Not Found` (resource concealed; zero data leakage). |
| **Header Injection / Spoofing** | External caller supplies `X-User-Id: <target-uuid>` | Gateway strips header; downstream receives only verified token claims. |
| **Cryptographic Token Tampering** | Mutate `sub` claim in JWT payload without re-signing | Signature check fails; returns `HTTP 401 Unauthorized`. |
| **Token Expiry Validation** | Issue request with JWT issued $> 15$ minutes ago | Expiration check fails; returns `HTTP 401 Unauthorized` (`TOKEN_EXPIRED`). |
| **SQL Injection (SQLi)** | Supply `' OR 1=1 --` into task search or filter parameter | JPA binds value as literal string; returns 0 matches or safe matches. |
| **Cross-Site Scripting (XSS)** | Supply `<script>alert(1)</script>` into task description | Persisted as raw text; React auto-encodes into DOM; zero execution. |
| **Brute-Force Flooding** | Issue 15 consecutive login requests from single IP | Token Bucket trips; returns `HTTP 429 Too Many Requests`. |

---

## 3. Automated IDOR Security Test (`TaskSecurityIntegrationTest.java`)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TaskSecurityIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @DisplayName("SECURITY GATE: Accessing another user's task MUST return 404 Not Found")
    void getTask_BelongingToAnotherUser_Returns404NotFound() {
        UUID ownerUserId = UUID.randomUUID();
        UUID attackerUserId = UUID.randomUUID();

        // Seed task belonging to legitimate owner
        TaskEntity victimTask = taskRepository.save(TaskEntity.builder()
                .userId(ownerUserId)
                .title("Confidential Strategic Plan")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .build());

        // Attacker attempts to retrieve victim's task
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", attackerUserId.toString());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ProblemDetail> response = restTemplate.exchange(
                "/api/v1/tasks/" + victimTask.getId(), HttpMethod.GET, request, ProblemDetail.class);

        // Security Assertion: Must return 404, NEVER 403 or 200
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Resource Not Found");
    }
}
```

