# Docker Security Checklist & Production Hardening Audit

| Field | Value |
|---|---|
| **Document ID** | DOC-DEP-007 |
| **Title** | Production Docker Security Checklist & Hardening Audit |
| **Project** | Task Management System – Microservices Architecture |
| **Target Platform** | Hostinger VPS (Ubuntu 26.04 LTS, Docker Engine 26.x) |
| **Status** | Approved Production Baseline |
| **Traceability Reference** | ADR-002, ADR-004, DOC-SEC-001, DOC-SEC-003, RULE-02, RULE-05 |

---

## 1. Production Security Audit Checklist

| Item # | Security Control | Verification Criterion | Implementation Mechanism | Audit Status |
|:---:|---|---|---|:---:|
| **SEC-01** | **Zero Root Execution** | No application container runs as `root` (UID 0). | Backend Dockerfiles declare `USER appuser:appgroup` (10001). Frontend declares `USER nginx` (101). | **PASS** |
| **SEC-02** | **Concealed Internal Ports** | Internal datastores and microservices must not bind to `0.0.0.0`. | PostgreSQL, Prometheus, Grafana, and Eureka bind strictly to `127.0.0.1` loopback. Kafka, microservices, and frontend have zero host ports. | **PASS** |
| **SEC-03** | **Perimeter Ingress Only** | Only the hardened reverse proxy listens on public network interfaces. | Only `taskmanager-nginx` binds `0.0.0.0:80` and `0.0.0.0:443`. | **PASS** |
| **SEC-04** | **Zero Default Passwords** | No default/hardcoded passwords present in production deployment. | All credentials (`POSTGRES_ROOT_PASSWORD`, `DB_*_PASSWORD`, `JWT_SECRET`, `GRAFANA_ADMIN_PASSWORD`) are injected via `.env`. | **PASS** |
| **SEC-05** | **Strict Secret Permissions** | Production credentials protected from unauthorized local user read. | Host `.env` file is set to `chmod 600` (read/write by owner only). | **PASS** |
| **SEC-06** | **Zero Secrets in Git** | Credentials must never be committed to GitHub repository. | Root `.gitignore` and `.dockerignore` strictly exclude `.env`, `.env.*`, `secrets/`, `*.pem`, `*.key`. | **PASS** |
| **SEC-07** | **Database-per-Service Isolation** | Services must not use shared databases or shared superuser credentials. | `init-multi-db.sh` provisions separate databases (`user_db`, `task_db`, `notification_db`) and separate roles (`user_app`, `task_app`, `notification_app`). | **PASS** |
| **SEC-08** | **High-Entropy JWT Key** | Secret key must prevent HMAC-SHA256 brute-force attacks. | `JWT_SECRET` enforces $\ge 256$ bits (Base64-encoded 48+ chars). | **PASS** |
| **SEC-09** | **Anti-Enumeration Concealment** | Unauthorized cross-tenant queries must return `404 Not Found`. | Microservices enforce tenant-scoped lookups (`RULE-05`). Gateway and Nginx conceal internal network addresses. | **PASS** |
| **SEC-10** | **TLS / HTTPS Hardening** | Modern cryptographic protocols and ciphers. | TLSv1.2 & TLSv1.3 enforced. HSTS (`max-age=63072000`), secure ciphers, SSL session caching. | **PASS** |
| **SEC-11** | **Container Resource Guardrails** | Prevent single container memory leak from crashing the VPS host (OOM). | `deploy.resources.limits` memory and CPU caps configured on all 11 containers ($\le 6.0\text{ GB}$ total). | **PASS** |
| **SEC-12** | **Host Firewall (UFW)** | Operating system blocks all unexpected ingress ports. | Ubuntu UFW blocks all incoming traffic except ports 22 (SSH), 80 (HTTP), 443 (HTTPS). | **PASS** |
| **SEC-13** | **Health Probes & Auto-Restart** | Containers automatically recover from crashes and failure states. | `restart: unless-stopped` + Actuator healthcheck probes on all services. | **PASS** |
| **SEC-14** | **Traceability & Correlation** | Distributed tracing propagated through all network hops without data leaks. | `X-Correlation-ID` header injected by Gateway/Client and preserved in Nginx access logs (`RULE-09`). | **PASS** |

---

## 2. Docker Daemon Hardening Recommendations

On the Hostinger VPS `/etc/docker/daemon.json`, apply the following daemon-level security settings:

```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "50m",
    "max-file": "5"
  },
  "no-new-privileges": true,
  "live-restore": true,
  "userland-proxy": false
}
```

Reload Docker daemon:
```bash
systemctl restart docker
```

---

## 3. Vulnerability Scanning Workflow

Prior to production updates, run Trivy vulnerability scans against container images:

```bash
# Install Trivy on Ubuntu VPS
apt install -y wget apt-transport-https gnupg lsb-release
wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key | gpg --dearmor | tee /usr/share/keyrings/trivy.gpg > /dev/null
echo "deb [signed-by=/usr/share/keyrings/trivy.gpg] https://aquasecurity.github.io/trivy-repo/deb $(lsb_release -sc) main" | tee -a /etc/apt/sources.list.d/trivy.list
apt update && apt install -y trivy

# Scan built images for HIGH and CRITICAL CVEs
trivy image --severity HIGH,CRITICAL taskmanager-api-gateway:production
trivy image --severity HIGH,CRITICAL taskmanager-user-service:production
trivy image --severity HIGH,CRITICAL taskmanager-task-service:production
trivy image --severity HIGH,CRITICAL taskmanager-notification-service:production
trivy image --severity HIGH,CRITICAL taskmanager-frontend:production
```

