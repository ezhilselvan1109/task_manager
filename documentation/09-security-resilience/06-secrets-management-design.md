# Secrets Management Design

**Document Identifier:** DOC-SEC-006  
**Traceability References:** SEC-005, NFR-003, ADR-006  
**Target Environments:** Local Development, CI/CD Pipeline, Production  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document defines the secrets management architecture, credential storage policies, and key rotation procedures across the Task Management System. The architecture strictly enforces a **Zero-Hardcoded-Secrets** standard across all source code, commit histories, Docker images, and documentation.

---

## 2. Secrets Inventory

| Secret Identifier | Intended Consumers | Sensitivity | Description & Minimum Entropy |
|---|---|---|---|
| `JWT_SECRET` | `user-service`, `api-gateway` | Critical | 256-bit symmetric cryptographic key for HMAC-SHA256 signing (minimum 32 alphanumeric / base64 characters). |
| `DB_USER_PASSWORD` | `user-service` | High | Password for PostgreSQL user `user_app` accessing `user_db`. |
| `DB_TASK_PASSWORD` | `task-service` | High | Password for PostgreSQL user `task_app` accessing `task_db`. |
| `DB_NOTIF_PASSWORD` | `notification-service` | High | Password for PostgreSQL user `notification_app` accessing `notification_db`. |
| `POSTGRES_ROOT_PASSWORD`| Infrastructure (Postgres) | Critical | Master administrative password for initial database provisioning. |

---

## 3. Environment-Specific Secret Provisioning

```mermaid
graph LR
    subgraph "Local / Development"
        EnvFile[".env (Git-Ignored)"] -->|docker compose --env-file| DC["Docker Compose Services"]
    end

    subgraph "CI / CD (Jenkins)"
        JCreds["Jenkins Credentials Store"] -->|withCredentials() wrapper| JPipeline["Jenkinsfile Pipeline Execution"]
    end

    subgraph "Production (Enterprise Roadmap)"
        Vault["HashiCorp Vault / Cloud Secret Store"] -->|Dynamic Secret Injection| ProdContainers["Production Kubernetes Pods"]
    end
```

### 3.1 Local Development Policy
- A `.env.example` template is committed to the repository, containing dummy variable names with empty values.
- Developer-specific `.env` files contain local secrets and are strictly excluded from source control via `.gitignore`.
- Docker Compose injects secrets into container runtimes via standard environment variables:
  ```yaml
  environment:
    - JWT_SECRET=${JWT_SECRET}
    - SPRING_DATASOURCE_PASSWORD=${DB_TASK_PASSWORD}
  ```

### 3.2 CI/CD Jenkins Pipeline Provisioning
- Credentials are stored securely inside the Jenkins Credential Store (encrypted on disk using master AES-256 keys).
- The declarative `Jenkinsfile` accesses secrets using the `withCredentials` binding:
  ```groovy
  withCredentials([string(credentialsId: 'JWT_SECRET', variable: 'JWT_SECRET'),
                   usernamePassword(credentialsId: 'DB_TASK_CREDS', 
                                    usernameVariable: 'DB_USER', 
                                    passwordVariable: 'DB_PASS')]) {
      sh 'docker compose -f infrastructure/docker-compose.yml up -d'
  }
  ```

---

## 4. Secret Leakage Prevention & Scanning

1. **Repository `.gitignore` Safeguards**:
   The repository enforces comprehensive ignore rules preventing credential check-ins:
   ```gitignore
   .env
   .env.*
   *.pem
   *.key
   *.pkcs12
   *.jks
   credentials.json
   secrets.yaml
   ```
2. **Automated Pre-Commit & Pipeline Scanning**:
   - `gitleaks` and `trufflehog` static analysis scanners run during local pre-commit checks and automated Jenkins PR validation.
   - Any commit detected containing high-entropy strings, private keys, or API tokens triggers immediate build failure.

---

## 5. Secret Rotation Procedures

### 5.1 Database Password Rotation
1. Administrator executes SQL command to change role password in PostgreSQL:
   ```sql
   ALTER ROLE task_app WITH PASSWORD 'NewSecurePassw0rd!';
   ```
2. Update Jenkins Credentials or deployment environment variable `DB_TASK_PASSWORD`.
3. Perform zero-downtime rolling restart of `task-service` containers to pick up the updated database credentials.

### 5.2 JWT Secret Rotation
- **Emergency Invalidation**: If the secret is compromised, updating `JWT_SECRET` and restarting `api-gateway` and `user-service` instantly invalidates all active tokens, forcing all users to re-authenticate with valid credentials.
- **Graceful Migration (Dual Key Window)**: The Gateway JWT validator can be configured with a secondary verification secret (`JWT_SECRET_PREVIOUS`) during scheduled key rollover to allow unexpired tokens to age out gracefully.

