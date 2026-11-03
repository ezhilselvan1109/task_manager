---
name: integration-testing-testcontainers
description: >-
  Use this skill when developing integration tests with Spring Boot, Testcontainers,
  real PostgreSQL databases, and real Apache Kafka brokers.
---

# Integration Testing with Testcontainers Skill

## Purpose
Specifies the pattern for writing integration tests against real containerized PostgreSQL and Apache Kafka instances, ensuring true production parity without relying on in-memory substitutes.

## When to Use
- Testing Flyway migrations and JPA repository query execution against PostgreSQL 16.
- Testing Kafka event production, consumer listeners, and consumer idempotency.
- Writing full end-to-end service slice tests with `@SpringBootTest(webEnvironment = RANDOM_PORT)`.

## Required Inputs
- Docker daemon running locally or on the CI runner.
- Test class in `src/test/java/.../integration/`.

## Step-by-Step Procedure
1. **Define Testcontainers Base Class**:
   Create an abstract reusable base class to share containers across test runs:
   ```java
   @Testcontainers
   public abstract class AbstractIntegrationTest {
       @Container
       static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
           .withDatabaseName("test_db")
           .withUsername("test")
           .withPassword("test");

       @Container
       static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

       @DynamicPropertySource
       static void configureProperties(DynamicPropertyRegistry registry) {
           registry.add("spring.datasource.url", postgres::getJdbcUrl);
           registry.add("spring.datasource.username", postgres::getUsername);
           registry.add("spring.datasource.password", postgres::getPassword);
           registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
       }
   }
   ```
2. **Execute Full Flow Integration Test**:
   ```java
   @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
   class TaskServiceIntegrationTest extends AbstractIntegrationTest {
       @Autowired
       private TestRestTemplate restTemplate;
       @Autowired
       private TaskRepository taskRepository;

       @Test
       void shouldCreateTaskAndPersistOutboxEvent() {
           // Execute HTTP POST and assert database state in real PostgreSQL
       }
   }
   ```
3. **Execute via Maven Failsafe**:
   - Run integration tests: `mvn verify -Pintegration-tests` or `mvn test -Dtest=*IT`.

## Applicable Project Standards
- [Rule 08: Testing & TDD Standards](file:///d:/learning/task_manager/.agents/rules/08-testing-and-tdd.md)
- [Integration Testing Guide](file:///d:/learning/task_manager/documentation/13-testing-qa/05-integration-testing-guide.md)

## Validation Checklist
- [ ] Uses real PostgreSQL container, never H2.
- [ ] Flyway migrations execute automatically at container startup.
- [ ] Tests clean up their test data or use isolated random UUID tenants.
- [ ] DynamicPropertySource overrides datasource properties.

## Common Mistakes
- Hardcoding `localhost:5432` in integration test configuration.
- Spinning up a new PostgreSQL container for every single test class (use shared static container to minimize execution time).

## Completion Criteria
Integration tests pass against containerized PostgreSQL and Kafka instances, validating real SQL constraints and message passing.

