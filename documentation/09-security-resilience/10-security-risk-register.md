# Security Risk Register

**Document Identifier:** DOC-SEC-010  
**Traceability References:** SEC-001 to SEC-010, NFR-003, NFR-004, Initial Risk Register  
**Methodology:** 5x5 Likelihood and Impact Risk Scoring  
**Status:** Approved Baseline  

---

## 1. Overview
This register catalogs all identified security risks, vulnerabilities, and threat scenarios for the Task Management System. It quantifies inherent risk prior to architectural controls, details technical mitigations, and evaluates residual risk post-implementation.

---

## 2. Risk Evaluation Matrix

| Likelihood \ Impact | 1 - Negligible | 2 - Minor | 3 - Moderate | 4 - Major | 5 - Catastrophic |
|---|---|---|---|---|---|
| **5 - Almost Certain** | Low | Medium | High | Critical | Critical |
| **4 - Likely** | Low | Medium | High | High | Critical |
| **3 - Possible** | Low | Low | Medium | High | High |
| **2 - Unlikely** | Low | Low | Low | Medium | Medium |
| **1 - Rare** | Low | Low | Low | Low | Medium |

---

## 3. Security Risk Inventory

| Risk ID | Threat Description & Attack Vector | Inherent (L x I = Score) | Architectural Control & Mitigation | Residual (L x I = Score) | Status |
|---|---|---|---|---|---|
| `SRISK-001` | **Insecure Direct Object Reference (IDOR)**: Attacker guesses task UUIDs belonging to other users to access/modify private tasks. | 4 x 4 = **High** | 1. Strict tenant scoping in JPA queries (`WHERE user_id = :userId`).<br>2. Anti-enumeration: returns `404 Not Found` rather than `403`. | 1 x 4 = **Low** | Mitigated |
| `SRISK-002` | **Header Spoofing**: Malicious client injects arbitrary `X-User-Id` header into inbound request to bypass auth. | 4 x 5 = **Critical** | Gateway `HeaderSanitizationGatewayFilter` strips all inbound `X-User-*` headers before JWT validation and downstream routing. | 1 x 5 = **Low** | Mitigated |
| `SRISK-003` | **Brute-Force & Credential Stuffing**: Automated bots flood `/api/v1/auth/login` to crack accounts and exhaust server CPU. | 4 x 3 = **High** | 1. Token Bucket rate limiting (5 req/min/IP).<br>2. BCrypt cost 12 (~250ms computation time).<br>3. Generic error responses. | 2 x 2 = **Low** | Mitigated |
| `SRISK-004` | **Weak Credentials**: Users choose trivial passwords susceptible to dictionary attacks. | 4 x 3 = **High** | Jakarta Bean Validation regex enforces 8-100 characters, upper, lower, digit, and special symbol (`BRULE-008`). | 2 x 2 = **Low** | Mitigated |
| `SRISK-005` | **Secret Key Exposure**: Cryptographic `JWT_SECRET` or DB passwords accidentally checked into Git repository. | 3 x 5 = **High** | 1. Zero hardcoded secrets policy (`.gitignore` rules).<br>2. Automated `gitleaks` pre-commit and CI scans.<br>3. Environment variable injection. | 1 x 5 = **Low** | Mitigated |
| `SRISK-006` | **Information Leakage via Errors**: Uncaught exceptions expose stack traces, DB table names, or Hibernate SQL syntax. | 3 x 3 = **Medium** | Global `RestControllerAdvice` intercepts all exceptions and outputs sanitized RFC 7807 Problem Details with internal details redacted. | 1 x 2 = **Low** | Mitigated |
| `SRISK-007` | **Stored Cross-Site Scripting (XSS)**: Malicious script embedded in `task.description` executed by victim browser. | 3 x 4 = **High** | 1. React JSX context-aware output encoding.<br>2. Content-Security-Policy headers.<br>3. Plaintext UTF-8 rendering. | 1 x 3 = **Low** | Mitigated |
| `SRISK-008` | **Large Payload / Buffer Flooding**: Massive request body exhausts Gateway memory. | 3 x 3 = **Medium** | Gateway limits request body to 1 MB maximum; oversized requests receive `HTTP 413 Payload Too Large`. | 1 x 2 = **Low** | Mitigated |
| `SRISK-009` | **Cross-Database Data Breach**: Compromised service attempts to query tables of another service. | 3 x 4 = **High** | Database-per-service pattern: dedicated PostgreSQL roles (`user_app`, `task_app`, `notification_app`) granted access only to own database. | 1 x 4 = **Low** | Mitigated |
| `SRISK-010` | **Poison Pill Kafka Ingestion**: Corrupted Kafka message causes consumer deserialization crash loop. | 3 x 3 = **Medium** | Spring Kafka `DefaultErrorHandler` with `DeadLetterPublishingRecoverer` redirects poison pill to `task.events.v1.DLT` after 3 retries. | 1 x 2 = **Low** | Mitigated |

---

## 4. Risk Governance and Review Policy
1. **Quarterly Audit**: Risk register is reviewed at the end of each development quarter or prior to major feature releases.
2. **Incident Trigger**: Any security incident or failed vulnerability scan automatically reopens the affected risk entry for scoring adjustment and corrective action.

