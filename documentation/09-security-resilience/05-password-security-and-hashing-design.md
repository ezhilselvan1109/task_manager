# Password Security and Hashing Design

**Document Identifier:** DOC-SEC-005  
**Traceability References:** SEC-003, BRULE-008, NFR-003, FR-001, FR-002  
**Target Services:** `user-service`  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the cryptographic standards, validation policies, and defensive handling mechanisms governing user credentials in the Task Management System. The architecture strictly mandates the use of BCrypt adaptive hashing with a cost factor of 12, constant-time verification, and zero plaintext exposure in logs, datastores, and memory dumps.

---

## 2. Password Complexity Requirements

To defend against dictionary attacks and weak password selection, all user passwords must satisfy the following baseline complexity rules (`BRULE-008`):

| Rule Parameter | Constraint | Error Message |
|---|---|---|
| **Minimum Length** | 8 characters | Password must be at least 8 characters long. |
| **Maximum Length** | 100 characters | Password cannot exceed 100 characters. |
| **Uppercase Character** | At least 1 uppercase letter (`[A-Z]`) | Password must contain at least one uppercase letter. |
| **Lowercase Character** | At least 1 lowercase letter (`[a-z]`) | Password must contain at least one lowercase letter. |
| **Numeric Digit** | At least 1 digit (`[0-9]`) | Password must contain at least one numeric digit. |
| **Special Character** | At least 1 special symbol (`[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]`) | Password must contain at least one special character. |
| **Whitespace** | Zero whitespace allowed | Password cannot contain spaces or tabs. |

### 2.1 Validation Regex Pattern
```regex
^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?])[^\s]{8,100}$
```

---

## 3. Cryptographic Hashing Specification

### 3.1 Algorithm Selection: BCrypt
- **Standard**: OpenBSD `$2a$` / `$2b$` adaptive key derivation function.
- **Underlying Primitive**: Eksblowfish (Exact Key Setup Blowfish cipher).
- **Salt Generation**: 128-bit cryptographically secure pseudorandom salt generated via `java.security.SecureRandom` on every hash invocation.
- **Work Factor (Cost)**: Configured to **12** ($2^{12} = 4,096$ hashing rounds).

### 3.2 Performance and Security Benchmark
- **Computation Time**: Approximately **200–280 ms** on modern x86-64 / ARM server hardware.
- **Brute-Force Resistance**: High CPU/memory intensity renders GPU/FPGA/ASIC parallel cracking economically unviable compared to legacy SHA-256 or MD5 hashes.
- **Throughput Budget**: 250 ms hash time is well within acceptable human interactive login latencies while naturally throttling high-velocity automated brute-force scripts.

### 3.3 Spring Security Implementation
```java
@Configuration
public class SecurityBeanConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Cost factor 12 enforces 4096 iteration rounds with SecureRandom salt
        return new BCryptPasswordEncoder(12);
    }
}
```

---

## 4. Constant-Time Verification & Timing Attack Defense

Password verification uses `passwordEncoder.matches(CharSequence rawPassword, String encodedPassword)`. 

1. BCrypt extracts the salt and cost factor embedded within the 60-character hash string.
2. The candidate password is re-hashed using the identical salt and work factor.
3. Hash comparison is performed using constant-time byte array equality checks (`MessageDigest.isEqual`), eliminating side-channel micro-architectural timing attacks.

---

## 5. Sensitive Data Handling and Logging Defenses

1. **DTO Scrubbing**: Password fields are decorated with `@ToString.Exclude` and Lombok `@JsonIgnore` / record isolation to ensure `System.out` or logging formatters do not output credentials.
2. **Logback Pattern Masking**: Logback configuration regex filters scrub any occurrences of `"password": "..."` or `password=...` from log outputs.
3. **Transient Memory Hygiene**: Character arrays and String allocations for passwords are systematically scoped to local method executions to enable prompt garbage collection.
4. **Zero Outbound Exposure**: The JPA `UserEntity` field `passwordHash` is never mapped to any outbound DTO (`UserResponse`, `TaskResponse`).

