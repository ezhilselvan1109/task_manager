# Troubleshooting Guide

**Document Identifier:** DOC-OBS-012  
**Traceability References:** NFR-002, NFR-004, DOC-OBS-001  
**Target Audience:** DevOps Engineers, On-Call Developers, SREs  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document provides diagnostic recipes and tactical remediation playbooks for resolving common operational anomalies, performance bottlenecks, and infrastructure faults across the Task Management platform.

---

## 2. Diagnostic Playbooks

### Playbook 1: API Gateway Returns `HTTP 503 Service Unavailable`
- **Symptom**: User receives `503 Service Unavailable` with `errorCode: SERVICE_TEMPORARILY_UNAVAILABLE`.
- **Root Cause**: Downstream service crashed, failed health probes, or Resilience4j circuit breaker tripped `OPEN`.
- **Diagnostic Steps**:
  1. Inspect Eureka Registry at `http://localhost:8761`. Verify target service is listed under registered instances.
  2. Check Prometheus circuit breaker metric:
     ```promql
     resilience4j_circuitbreaker_state{name="taskServiceCircuitBreaker"}
     ```
  3. Inspect downstream container logs:
     ```powershell
     docker logs --tail 100 taskmanager-task-service
     ```
- **Remediation**:
  - If container crashed: `docker restart taskmanager-task-service`.
  - Wait 10 seconds for circuit breaker to enter `HALF_OPEN` state and probe health.

---

### Playbook 2: Database Connection Pool Exhaustion (`Connection is not available`)
- **Symptom**: Application logs show `SQLTransientConnectionException: HikariPool-1 - Connection is not available, request timed out after 3000ms`.
- **Root Cause**: Runaway unindexed query or unclosed transaction holding connections.
- **Diagnostic Steps**:
  1. Inspect active PostgreSQL queries:
     ```sql
     SELECT pid, now() - pg_stat_activity.query_start AS duration, query, state 
     FROM pg_stat_activity 
     WHERE state != 'idle';
     ```
  2. Check HikariCP pending connection count in Grafana.
- **Remediation**:
  - Terminate long-running blocking query: `SELECT pg_terminate_backend(<pid>);`.
  - Review JPA queries for missing `@Transactional(readOnly = true)`.

---

### Playbook 3: Outbox Events Stalling (`processed = false`)
- **Symptom**: Outbox table growing indefinitely; no events arriving in Kafka.
- **Root Cause**: Outbox poller thread encountering Kafka broker timeouts or DB lock.
- **Diagnostic Steps**:
  1. Check count of unprocessed events:
     ```sql
     SELECT count(*), min(created_at) FROM outbox_events WHERE processed = FALSE;
     ```
  2. Inspect Kafka broker connectivity from container:
     ```powershell
     docker exec taskmanager-task-service nc -zv kafka 9092
     ```
- **Remediation**:
  - If Kafka unreachable: `docker restart taskmanager-kafka`.
  - Once Kafka is up, poller automatically clears backlog on next 500ms cycle.

---

### Playbook 4: Kafka Consumer Lag Accumulating
- **Symptom**: `kafka_consumer_records_lag` increasing in `notification-service`.
- **Root Cause**: Consumer thread retrying poison pill or saturated by high throughput.
- **Diagnostic Steps**:
  1. Check if records are routing to DLT:
     ```powershell
     kafka-console-consumer --bootstrap-server kafka:9092 --topic task.events.v1.DLT --from-beginning --max-messages 5
     ```
- **Remediation**:
  - If consumer blocked by poison pill: ensure `DefaultErrorHandler` routes to DLT.
  - Scale consumer concurrency: adjust `factory.setConcurrency(5)` in `KafkaConsumerConfig`.

