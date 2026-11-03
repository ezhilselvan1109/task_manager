# Deployment Runbook

**Document Identifier:** DOC-OPS-013  
**Traceability References:** DOC-OPS-009, DOC-INF-013, DOC-OPS-014  
**Target Operational Scenario:** Production / Staging Deployment Rollout  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This runbook provides the definitive operational checklist and command sequence for deploying a verified release to staging or production environments.

---

## 2. Pre-Deployment Verification Checklist
- [ ] Release branch merged into `main` and tagged with valid SemVer (e.g. `v1.0.0`).
- [ ] Jenkins pipeline completed GREEN with 100% test pass rate and $\ge 80\%$ line coverage.
- [ ] Pre-deployment database snapshot captured:
  ```powershell
  docker exec taskmanager-postgresql pg_dump -U postgres task_db > backup/pre_deploy_task_db.sql
  docker exec taskmanager-postgresql pg_dump -U postgres user_db > backup/pre_deploy_user_db.sql
  docker exec taskmanager-postgresql pg_dump -U postgres notification_db > backup/pre_deploy_notif_db.sql
  ```
- [ ] Production environment variables (`JWT_SECRET`, database passwords) confirmed in `.env`.

---

## 3. Deployment Execution Steps

### Step 1: Pull or Build Target Release Images
```powershell
docker compose -f infrastructure/docker-compose.yml build --no-cache
```

### Step 2: Sequenced Service Startup
1. Ensure infrastructure foundation is healthy:
   ```powershell
   docker compose -f infrastructure/docker-compose.yml up -d postgresql zookeeper kafka service-discovery
   ```
2. Verify discovery server is accepting registrations:
   ```powershell
   curl -f http://localhost:8761/actuator/health
   ```
3. Launch core microservices:
   ```powershell
   docker compose -f infrastructure/docker-compose.yml up -d user-service task-service notification-service
   ```
4. Launch Edge API Gateway and Frontend:
   ```powershell
   docker compose -f infrastructure/docker-compose.yml up -d api-gateway frontend prometheus grafana
   ```

---

## 4. Post-Deployment Smoke Verification

1. **Verify Health Probes**:
   ```powershell
   curl http://localhost:8080/actuator/health
   ```
   Must return `{"status":"UP"}`.
2. **Execute Automated Smoke Tests**:
   ```powershell
   newman run tests/e2e/smoke-tests.json -e tests/e2e/staging.json
   ```
3. **Inspect Prometheus Metrics Stream**:
   Open `http://localhost:3001` (Grafana) and verify HTTP throughput and 0% error rate.
4. **Go/No-Go Decision**:
   - If all probes pass: Sign off on successful deployment.
   - If any probe fails: Initiate `DOC-OPS-011` Rollback Runbook immediately.

