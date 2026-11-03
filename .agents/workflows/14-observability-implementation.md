# Workflow 14: Observability Implementation

## 1. Overview & Purpose
Defines the procedure for instrumenting microservices with custom Micrometer metrics, Actuator health probes, structured logging, and Grafana dashboard integration.

- **Workflow ID**: `WF-014`
- **Trigger**: New service implementation, SLA monitoring requirement, or custom business metric instrumentation.

## 2. Preconditions
- Service running with `spring-boot-starter-actuator` and `micrometer-registry-prometheus`.
- Prometheus and Grafana containers configured in `docker-compose.yml`.

## 3. Required Inputs
- Target microservice.
- Metric name, type (Counter, Gauge, Timer), and dimensional tags.

## 4. Execution Steps
1. **Instrument Code with Micrometer**:
   - Inject `MeterRegistry` into target service.
   - Record business events (e.g. `tasks_created_total`, `tasks_completed_total`, `task_duration_seconds`).
2. **Expose Actuator Endpoint**:
   - In `application.yml`:
     ```yaml
     management:
       endpoints:
         web:
           exposure:
             include: health, info, prometheus, metrics
     ```
3. **Verify Prometheus Scraping**:
   - Add scrape configuration to `infrastructure/prometheus/prometheus.yml`.
   - Query metric in Prometheus Expression Browser (`http://localhost:9090`).
4. **Create Grafana Visualization Panel**:
   - Add panel querying the custom metric in Grafana.
   - Configure alert threshold if applicable.
5. **Verify Correlation ID Propagation**:
   - Confirm MDC `correlationId` appears in all log outputs.

## 5. Skills to Use
- [`prometheus-grafana-observability`](file:///d:/learning/task_manager/.agents/skills/prometheus-grafana-observability/SKILL.md)
- [`spring-boot-microservice`](file:///d:/learning/task_manager/.agents/skills/spring-boot-microservice/SKILL.md)

## 6. Rules to Follow
- [Rule 07: Exception Handling & Logging Standards](file:///d:/learning/task_manager/.agents/rules/07-exception-handling-and-logging.md)
- [Rule 10: Observability & Operational Standards](file:///d:/learning/task_manager/.agents/rules/10-observability-and-ops.md)

## 7. Tests and Validation
- Curl `/actuator/prometheus` and verify custom metric name appears with current counter value.
- Confirm correlation ID propagates end-to-end.

## 8. Failure Handling
- If Prometheus cannot scrape endpoint, verify Docker network connectivity and port mappings.

## 9. Review Checklist
- [ ] No high-cardinality tags used in metrics.
- [ ] Sensitive Actuator endpoints (`env`, `heapdump`) are disabled.
- [ ] Prometheus scrape job targets are valid.

## 10. Expected Outputs
- Instrumented Java code.
- Updated Prometheus configuration.
- Operational Grafana dashboard panel.

