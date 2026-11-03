# Automated Testing Workflow

**Document Identifier:** DOC-AUTO-007  
**Traceability References:** Master Project Goal, DOC-AUTO-001, DOC-AUTO-004  
**Testing Frameworks:** JUnit 5, Mockito, AssertJ, Testcontainers, JaCoCo  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the automated testing architecture, testing pyramid, execution workflows, and coverage gates enforced across the Task Management Microservices platform. Automated testing is built into the development loop to ensure rapid feedback, regression prevention, and production confidence.

---

## 2. The Microservices Testing Pyramid

```
                ▲
               / \
              /   \
             / E2E \       5% (Full System via Gateway / Postman Newman)
            /-------\
           / Integr. \    10% (Testcontainers: PostgreSQL 16 + Kafka 3.7)
          /-----------\
         / Slice Tests \  15% (@WebMvcTest, @DataJpaTest)
        /---------------\
       /    Unit Tests   \ 70% (JUnit 5 + Mockito + AssertJ: <10ms each)
      /-------------------\
```

---

## 3. Testing Layers and Framework Matrix

| Testing Layer | Scope & Objective | Framework / Tooling | Execution Target | Target Runtime |
|---|---|---|---|---|
| **Unit Tests** | Domain services, business rules, mappers, validators | JUnit 5, Mockito, AssertJ | Local / Pre-commit / CI | $< 10$ seconds total |
| **Web Slice Tests** | Controllers, DTO validation, RFC 7807 exception formatting | `@WebMvcTest`, MockMvc | Local / CI | $< 15$ seconds total |
| **Repository Slice** | Custom JPA queries, specification predicate filters | `@DataJpaTest`, Testcontainers | Local / CI | $< 30$ seconds total |
| **Integration Tests**| Outbox poller, Kafka consumer idempotency, DB transactions | `@SpringBootTest`, Testcontainers | CI Pipeline | $< 90$ seconds total |
| **API Contract Tests**| Gateway routing, headers, status codes, OpenAPI schema | REST-Assured, Spring Cloud Contract | CI Pipeline | $< 60$ seconds total |

---

## 4. Testcontainers Integration Testing Pattern

Integration tests execute against real, ephemeral PostgreSQL and Apache Kafka instances:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TaskEventIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("task_db")
            .withUsername("task_app")
            .withPassword("secret");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void shouldPersistTaskAndOutboxEventAtomically() {
        // Verifies real transactional outbox persistence in PostgreSQL
    }
}
```

---

## 5. JaCoCo Code Coverage Requirements

Configured in service `pom.xml`:
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.75</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

- **Line Coverage Gate**: Minimum **80%**.
- **Branch Coverage Gate**: Minimum **75%**.
- **Security Classes Gate**: **100%** coverage required for `JwtAuthenticationFilter`, `HeaderSanitizationGatewayFilter`, and `BCryptPasswordEncoder` bean definitions.

