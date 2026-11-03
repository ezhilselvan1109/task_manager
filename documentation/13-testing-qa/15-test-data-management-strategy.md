# Test Data Management Strategy

**Document Identifier:** DOC-TEST-015  
**Traceability References:** DOC-TEST-001, DOC-TEST-002  
**Patterns:** Object Mother Pattern, Test Data Builders, Ephemeral Isolation  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the test data management (TDM) architecture across the Task Management platform. It establishes patterns for data isolation, synthetic data generation, test fixture hygiene, and transactional rollback to eliminate flaky tests and test pollution.

---

## 2. Core Test Data Management Principles

1. **Hermetic Test Isolation**: Tests must never depend on database records seeded by preceding tests. Each test is responsible for creating and tearing down its own test fixtures.
2. **Deterministic UUIDs & Dynamic Data**: All entity primary keys and foreign keys use `UUID.randomUUID()`, preventing unique constraint collisions during parallel test execution.
3. **Zero Production PII in Test Suites**: Production data, actual user emails, or customer databases are strictly prohibited from test suites. Synthetic data factories generate realistic, compliant records.
4. **Automatic Transactional Rollback**: Tests annotated with `@Transactional` automatically roll back all database inserts and updates upon test completion.

---

## 3. Object Mother & Builder Pattern (`TestDataFactory.java`)

```java
public final class TestDataFactory {

    private TestDataFactory() {}

    public static CreateTaskRequest createValidTaskRequest() {
        return new CreateTaskRequest(
                "Automated Test Task " + UUID.randomUUID().toString().substring(0, 8),
                "Comprehensive task description for automated verification.",
                TaskPriority.HIGH,
                Instant.now().plus(7, ChronoUnit.DAYS)
        );
    }

    public static TaskEntity createDefaultTaskEntity(UUID userId) {
        return TaskEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title("Standard Test Task")
                .description("Sample description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .deleted(false)
                .build();
    }

    public static UserEntity createActiveUserEntity() {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        return UserEntity.builder()
                .id(UUID.randomUUID())
                .username("user_" + randomSuffix)
                .email("user_" + randomSuffix + "@example.com")
                .passwordHash("$2a$12$e8Y4Lw4tD8h7v9p0q1r2s3t4u5v6w7x8y9z0a1b2c3d4e5f6g7h8")
                .role(UserRole.ROLE_USER)
                .active(true)
                .deleted(false)
                .build();
    }
}
```

