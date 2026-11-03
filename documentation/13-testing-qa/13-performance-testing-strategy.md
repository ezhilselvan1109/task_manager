# Performance Testing Strategy

**Document Identifier:** DOC-TEST-013  
**Traceability References:** NFR-001, NFR-002, DOC-TEST-001  
**Load Testing Framework:** Gatling 3.10+ (Java DSL) / Apache JMeter  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the performance and load testing strategy for the Task Management System. Performance tests validate that the microservices architecture satisfies response time SLAs, database connection pool throughput, and memory ceilings under sustained concurrent user traffic.

---

## 2. Performance SLA Targets

| Operation Category | Target Endpoints | p95 Latency SLA | p99 Latency SLA | Error Rate Threshold |
|---|---|---|---|---|
| **Read Operations** | `GET /api/v1/tasks`, `GET /tasks/{id}` | **$< 100$ ms** | $< 200$ ms | $< 0.1\%$ |
| **Write Operations**| `POST /api/v1/tasks`, `PATCH /status` | **$< 200$ ms** | $< 400$ ms | $< 0.1\%$ |
| **Authentication** | `POST /api/v1/auth/login` | **$< 350$ ms** (Includes BCrypt) | $< 500$ ms | $< 0.1\%$ |
| **Dashboard Metrics**| `GET /api/v1/tasks/dashboard` | **$< 150$ ms** | $< 300$ ms | $< 0.1\%$ |

---

## 3. Gatling Load Simulation Profile (`TaskManagerSimulation.java`)

```java
public class TaskManagerSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder userJourney = scenario("Task Management Load Scenario")
            .exec(http("Login Request")
                    .post("/api/v1/auth/login")
                    .body(StringBody("{\"username\":\"testuser\",\"password\":\"Passw0rd123!\"}"))
                    .check(status().is(200))
                    .check(jsonPath("$.accessToken").saveAs("authToken")))
            .pause(1)
            .exec(http("Fetch Tasks List")
                    .get("/api/v1/tasks?page=0&size=20")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200)))
            .pause(1)
            .exec(http("Create New Task")
                    .post("/api/v1/tasks")
                    .header("Authorization", "Bearer #{authToken}")
                    .body(StringBody("{\"title\":\"Performance Task\",\"priority\":\"HIGH\"}"))
                    .check(status().is(201)));

    {
        setUp(
            userJourney.injectOpen(
                rampUsers(100).during(Duration.ofSeconds(60)) // Ramp up to 100 concurrent users
            )
        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().percentile(95).lt(250), // p95 SLA check
             global().successfulRequests().percent().gt(99.9) // 99.9% success check
         );
    }
}
```

