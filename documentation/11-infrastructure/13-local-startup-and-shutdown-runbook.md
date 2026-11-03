# Local Startup and Shutdown Runbook

**Document Identifier:** DOC-INF-013  
**Traceability References:** NFR-007, DOC-INF-001, DOC-INF-005  
**Target Audience:** All Developers, QA Engineers, DevOps on-call  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This runbook provides the definitive operational procedures for launching, validating, troubleshooting, and shutting down the Task Management Microservices platform on a local developer workstation.

---

## 2. Standard Startup Procedure

### Step 1: Pre-Flight Verification
Verify that Docker Desktop is running and healthy:
```powershell
docker info
```
Ensure at least 6 GB of RAM is allocated in Docker Desktop settings.

### Step 2: Initialize Environment File
Ensure `infrastructure/.env` exists:
```powershell
if (!(Test-Path infrastructure/.env)) {
    Copy-Item infrastructure/.env.example infrastructure/.env
}
```

### Step 3: Launch Containers via Docker Compose
Execute from the project root:
```powershell
docker compose -f infrastructure/docker-compose.yml up -d --build
```

### Step 4: Validate Service Health
1. **Check Container Status**:
   ```powershell
   docker compose -f infrastructure/docker-compose.yml ps
   ```
   All 11 containers should display status `Up (healthy)` or `Up`.
2. **Verify Eureka Service Discovery Registry**:
   Open browser to `http://localhost:8761`.  
   Verify that all 4 microservices appear under **Instances currently registered with Eureka**:
   - `API-GATEWAY`
   - `USER-SERVICE`
   - `TASK-SERVICE`
   - `NOTIFICATION-SERVICE`
3. **Verify Edge Gateway Ingress**:
   ```powershell
   curl http://localhost:8080/actuator/health
   ```
   Expected response: `{"status":"UP"}`.
4. **Access Web Applications**:
   - **React Web UI**: `http://localhost:3000`
   - **Grafana Observability**: `http://localhost:3001` (user: `admin`, password: `admin`)
   - **Prometheus Metrics**: `http://localhost:9090`

---

## 3. Standard Shutdown Procedures

### 3.1 Graceful Stop (Preserves Container State & Data)
```powershell
docker compose -f infrastructure/docker-compose.yml stop
```

### 3.2 Full Teardown (Removes Containers, Preserves Database Volumes)
```powershell
docker compose -f infrastructure/docker-compose.yml down
```

### 3.3 Factory Reset (Wipes Database Volumes & Kafka Logs)
```powershell
docker compose -f infrastructure/docker-compose.yml down -v
```
> [!WARNING]
> This command permanently deletes all local PostgreSQL databases, Kafka topic offsets, and Grafana dashboards.

---

## 4. Troubleshooting Guide

| Issue / Symptom | Root Cause | Remediation Command / Action |
|---|---|---|
| **Port Conflict** (`bind: address already in use`) | A local PostgreSQL or Node instance is occupying port 5432, 8080, or 3000. | Check occupying PID: `netstat -ano \| findstr :5432`<br>Terminate process or change port binding in `docker-compose.yml`. |
| **Container OOMKilled** (`exit code 137`) | Total allocated container RAM exceeded Docker Desktop limits. | Increase Docker Desktop memory to 8 GB or reduce `-Xmx` values in `docker-compose.yml`. |
| **Microservices Not in Eureka** | Discovery server booted slower than services. | Eureka client will retry registration automatically within 15 seconds. If stuck, restart service: `docker compose restart task-service`. |
| **PostgreSQL Auth Failure** | Old password cached in existing volume. | Execute clean reset to re-run `init-multi-db.sh`: `docker compose down -v && docker compose up -d`. |

