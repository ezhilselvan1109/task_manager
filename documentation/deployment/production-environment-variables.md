# Production Environment Variables Reference

| Field | Value |
|---|---|
| **Document ID** | DOC-DEP-004 |
| **Title** | Production Environment Variables Catalogue & Secret Management Guide |
| **Project** | Task Management System – Microservices Architecture |
| **Applicable Target** | Hostinger VPS (`.env` file) |
| **Status** | Approved Production Baseline |
| **Traceability Reference** | ADR-002, ADR-004, DOC-INF-007, DOC-SEC-001, RULE-02 |

---

## 1. Master Environment Variable Catalogue

The following variables are consumed by `docker-compose.prod.yml` and injected into the microservice containers:

| Variable Name | Required | Default / Fallback | Consumed By | Security Classification | Purpose & Description |
|---|---|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Yes | `docker` | All 5 Spring Boot Services | Low / Configuration | Activates the `docker` Spring profile for container-aware property overrides. |
| `POSTGRES_ROOT_PASSWORD` | **CRITICAL** | *None* | `postgresql` | **CONFIDENTIAL (Master)** | Superuser password for PostgreSQL cluster (`postgres` user). Required for initial DB creation. |
| `POSTGRES_HOST_PORT` | No | `5432` | `postgresql` | Low / Network | Host loopback port for PostgreSQL administrative access. Defaults to 5432. |
| `DB_USER_PASSWORD` | **CRITICAL** | *None* | `postgresql`, `user-service` | **CONFIDENTIAL (Credential)** | Password for dedicated PostgreSQL role `user_app` with ownership of `user_db`. |
| `DB_TASK_PASSWORD` | **CRITICAL** | *None* | `postgresql`, `task-service` | **CONFIDENTIAL (Credential)** | Password for dedicated PostgreSQL role `task_app` with ownership of `task_db`. |
| `DB_NOTIF_PASSWORD` | **CRITICAL** | *None* | `postgresql`, `notification-service` | **CONFIDENTIAL (Credential)** | Password for dedicated PostgreSQL role `notification_app` with ownership of `notification_db`. |
| `JWT_SECRET` | **CRITICAL** | *None* | `api-gateway`, `user-service` | **CONFIDENTIAL (Signing Key)** | Base64-encoded HMAC-SHA256 secret key ($\ge 256$ bits) used to sign and verify JWT access tokens. |
| `GRAFANA_ADMIN_PASSWORD` | **CRITICAL** | *None* | `grafana` | **CONFIDENTIAL (Credential)** | Administrator password for Grafana web interface. |
| `DOMAIN_NAME` | Yes | `yourdomain.com` | `nginx` | Low / Configuration | FQDN domain name assigned to the Hostinger VPS for SSL/TLS routing. |
| `FRONTEND_API_BASE_URL` | No | `""` (empty) | `frontend` (Build Arg) | Low / Configuration | Base URL for API requests. Leaving empty enables same-origin relative URLs (`/api/...`). |

---

## 2. Cryptographic Secret Generation Runbook (Ubuntu 26.04)

On the Hostinger VPS, generate high-entropy secrets using OpenSSL prior to container startup:

```bash
# 1. PostgreSQL Master Password (32 chars)
openssl rand -base64 24

# 2. Database Service Passwords (32 chars each)
openssl rand -base64 24 # for DB_USER_PASSWORD
openssl rand -base64 24 # for DB_TASK_PASSWORD
openssl rand -base64 24 # for DB_NOTIF_PASSWORD

# 3. JWT Signing Secret (minimum 256 bits, 64 Base64 chars)
openssl rand -base64 48

# 4. Grafana Admin Password
openssl rand -base64 18
```

---

## 3. Automated `.env` Generation Script

Execute this one-liner on your Ubuntu VPS to automatically populate `.env` with cryptographically secure values:

```bash
cat <<EOF > .env
SPRING_PROFILES_ACTIVE=docker
POSTGRES_HOST_PORT=5432
POSTGRES_ROOT_PASSWORD=$(openssl rand -base64 24)
DB_USER_PASSWORD=$(openssl rand -base64 24)
DB_TASK_PASSWORD=$(openssl rand -base64 24)
DB_NOTIF_PASSWORD=$(openssl rand -base64 24)
JWT_SECRET=$(openssl rand -base64 48)
GRAFANA_ADMIN_PASSWORD=$(openssl rand -base64 18)
DOMAIN_NAME=taskmanager.yourdomain.com
FRONTEND_API_BASE_URL=
EOF

# Lock down file permissions to root/deployer only
chmod 600 .env
```

---

## 4. File Permission & Security Rules

1. **Strict File Mode (`600`)**:
   - `.env` must only be readable and writable by the user executing Docker Compose (`chmod 600 .env`).
2. **Git Exclusion**:
   - `.env` and `.env.*` are explicitly listed in `.gitignore` and `.dockerignore`.
   - Never commit `.env` to GitHub.
3. **Container Injection**:
   - Injected strictly as environment variables at container creation.
   - Values are never baked into image filesystems or Docker layer caches.

