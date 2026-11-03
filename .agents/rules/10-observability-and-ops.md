# Rule 10: Observability & Operational Standards

## Prometheus & Metrics Standards
1. **Endpoint Exposure**:
   - Microservices expose Micrometer metrics at `/actuator/prometheus`.
   - Actuator sensitive endpoints (`env`, `heapdump`, `shutdown`) must be disabled or strictly restricted to internal networks.
2. **Standard Custom Metrics**:
   - `tasks_created_total{priority, status}` (Counter)
   - `tasks_completed_total` (Counter)
   - `outbox_events_published_total{status}` (Counter)
   - `notifications_consumed_total{eventType, status}` (Counter)
   - `task_processing_duration_seconds` (Timer / Summary)

## Health Probes
1. **Liveness & Readiness**:
   - Configure Kubernetes/Docker Actuator probes:
     - `/actuator/health/liveness`: Checks if JVM is running without deadlock.
     - `/actuator/health/readiness`: Checks database connectivity (`db`), Kafka broker (`kafka`), and disk space.
   - Initial delay: 30s, period: 10s, failure threshold: 3 attempts.

## Grafana & Alerting Rules
1. **Critical Alerts**:
   - Service down (`up{job="spring-boot-services"} == 0` for > 1 min).
   - Error rate spike (5xx HTTP responses > 5% for > 5 min).
   - High latency ($p95 \text{ latency} > 500\text{ ms}$ for > 5 min).
   - Outbox lag (> 100 unprocessed events for > 10 min).
   - Kafka consumer lag (> 50 messages for > 5 min).
2. **Disaster Recovery**:
   - Daily `pg_dump` automated backups with RPO $\le 1\text{ hour}$ and RTO $\le 15\text{ minutes}$.

