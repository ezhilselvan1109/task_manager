# Eureka Service Discovery Infrastructure Design

**Document Identifier:** DOC-INF-011  
**Traceability References:** ADR-003, NFR-007, DOC-INF-005  
**Service Engine:** Spring Cloud Netflix Eureka Server (Spring Cloud 2023.x)  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the Eureka Service Discovery infrastructure architecture, heartbeat eviction tuning, client registration parameters, and self-preservation behaviors across the Task Management platform.

---

## 2. Server Configuration (`service-discovery`)

Configured in `service-discovery/src/main/resources/application.yml`:

```yaml
server:
  port: 8761

spring:
  application:
    name: service-discovery

eureka:
  instance:
    hostname: service-discovery
  client:
    register-with-eureka: false       # Discovery server does not register with itself
    fetch-registry: false             # Discovery server does not fetch registry from peers
  server:
    enable-self-preservation: false   # CRITICAL: Disabled for local development
    eviction-interval-timer-in-ms: 5000 # Prune dead instances every 5 seconds
    response-cache-update-interval-ms: 3000 # Propagate fresh registry every 3 seconds
```

> [!TIP]
> **Why Disable Self-Preservation Locally?**  
> In production clusters, Eureka's self-preservation prevents mass de-registration during network partitions. In local developer Docker environments, developers frequently stop and restart containers. Disabling self-preservation guarantees that stopped containers are evicted within 10 seconds, preventing the Gateway from routing traffic to dead IPs.

---

## 3. Client Registration & Heartbeat Tuning

All microservices (`user-service`, `task-service`, `notification-service`, `api-gateway`) apply fast heartbeat intervals:

```yaml
eureka:
  instance:
    prefer-ip-address: true           # Containers communicate via IP on Docker subnet
    lease-renewal-interval-in-seconds: 5 # Send heartbeat every 5 seconds
    lease-expiration-duration-in-seconds: 15 # Evict after 15 seconds without heartbeat
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE:http://service-discovery:8761/eureka/}
    registry-fetch-interval-seconds: 5 # Pull latest instance routing table every 5 seconds
```

---

## 4. Gateway Client-Side Load Balancing
- Spring Cloud Gateway uses `lb://SERVICE-NAME` URI predicates (e.g. `lb://TASK-SERVICE`).
- The reactive `Spring Cloud LoadBalancer` queries Eureka's cached registry and distributes requests across active instances using round-robin scheduling.

