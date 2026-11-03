# ADR-007: Spring Cloud Netflix Eureka for Dynamic Service Discovery

| Metadata Item | Details |
|---|---|
| **ADR ID** | ADR-007 |
| **Status** | Approved |
| **Date** | 2026-11-03 |
| **Deciders** | DevOps Lead, Solution Architect |
| **Related Documents** | `DOC-ARCH-001`, `DOC-ARCH-004` |

---

## 1. Context & Problem Statement
In a containerized microservices environment, service instance IP addresses and ports are ephemeral. Hardcoding IP addresses or DNS names into the API Gateway creates tight deployment coupling, prevents horizontal scaling of downstream microservices, and leads to stale route failures.

## 2. Decision
Deploy **Spring Cloud Netflix Eureka** (`service-discovery` on port `8761`) as the centralized service registry:
1. Microservices (`user-service`, `task-service`, `notification-service`, `api-gateway`) register with Eureka on startup using logical application names (`USER-SERVICE`, `TASK-SERVICE`, `NOTIFICATION-SERVICE`).
2. Instances send periodic heartbeats (every 10 seconds) to maintain active lease status.
3. Spring Cloud Gateway routes incoming traffic using logical service names with client-side load balancing (`lb://TASK-SERVICE`).

## 3. Consequences
- **Positive**:
  - Zero hardcoded IP addresses.
  - Seamless horizontal scaling: additional instances of `task-service` automatically register and participate in gateway round-robin load distribution.
  - Native Spring Cloud integration with zero external daemon dependencies (unlike Consul or ZooKeeper).
- **Negative / Trade-offs**:
  - Heartbeat eviction latency: instances that crash ungracefully may remain in the registry for up to 30 seconds before lease expiration (mitigated by configuring Gateway retry filters with Resilience4j circuit breaking).

