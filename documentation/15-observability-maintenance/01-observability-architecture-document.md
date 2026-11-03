# Observability Architecture Document

**Document Identifier:** DOC-OBS-001  
**Traceability References:** NFR-004, NFR-005, Master Project Baseline  
**Pillars:** Metrics (Prometheus), Logs (Slf4j MDC), Traces (Correlation ID), Health (Actuator)  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document defines the comprehensive observability architecture for the Task Management System. Built upon the Three Pillars of Observability (Metrics, Logs, Traces), the architecture provides deep operational visibility into service health, latency profiles, database connection saturation, Kafka consumer lag, and circuit breaker states across the microservices mesh.

---

## 2. Three Pillars Observability Mesh

```mermaid
graph TD
    subgraph "Edge Ingress"
        GW["API Gateway (Port 8080)\n- Generates X-Correlation-Id\n- Exposes Micrometer Metrics"]
    end

    subgraph "Core Microservices Tier"
        US["user-service\n(Actuator :8081)"]
        TS["task-service\n(Actuator :8082)"]
        NS["notification-service\n(Actuator :8083)"]
    end

    subgraph "Pillar 1: Metrics Aggregation"
        PROM["Prometheus Server (Port 9090)\n- Scrapes /actuator/prometheus every 15s\n- Evaluates alert.rules"]
        GRAF["Grafana (Port 3001)\n- Dashboards: JVM, HTTP, Kafka, DB"]
        PROM --> GRAF
    end

    subgraph "Pillar 2 & 3: Logs & MDC Distributed Tracing"
        MDC["Slf4j MDC Context\n[serviceName, correlationId, userId]\nMasked JSON Console Logs"]
    end

    GW -->|Enriches Request with X-Correlation-Id| US
    GW -->|Enriches Request with X-Correlation-Id| TS
    GW -->|Enriches Request with X-Correlation-Id| NS

    GW -.->|Pull Metrics| PROM
    US -.->|Pull Metrics| PROM
    TS -.->|Pull Metrics| PROM
    NS -.->|Pull Metrics| PROM

    GW --- MDC
    US --- MDC
    TS --- MDC
    NS --- MDC
```

---

## 3. Observability Architecture Principles

1. **Correlation Across Boundaries**: Every inbound HTTP request receives or inherits an `X-Correlation-Id` at the API Gateway. This identifier is injected into Slf4j MDC, propagated across downstream HTTP headers, and stamped into Kafka event record headers (`EventEnvelope.correlationId`).
2. **Pull-Based Metrics Scraping**: Prometheus periodically pulls metrics from `/actuator/prometheus` endpoints on a 15-second scrape interval. Microservices do not require proprietary push agents.
3. **Structured JSON Logging**: Production logs output structured JSON strings with fixed schemas, enabling rapid filtering by `correlationId`, `userId`, or `level`.
4. **Zero Performance Impact**: Observability instrumentation operates asynchronously or via in-memory ring buffers (Micrometer counters and gauges), introducing negligible CPU and latency overhead ($< 1$ ms).

