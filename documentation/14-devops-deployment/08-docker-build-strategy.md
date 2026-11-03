# Docker Build and Image Tagging Strategy

**Document Identifier:** DOC-OPS-008  
**Traceability References:** ADR-003, DOC-INF-004, DOC-OPS-004  
**Container Engine:** Docker BuildKit, Trivy Security Scanner  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the Docker image compilation, tagging hierarchy, caching mechanics, and vulnerability scanning procedures executed during the automated delivery pipeline.

---

## 2. Image Tagging Taxonomy

| Build Context | Tag Format | Example | Retention / Lifecycle |
|---|---|---|---|
| **Pull Request** | `pr-{PR_NUMBER}-{COMMIT_SHA}` | `taskmanager/task-service:pr-42-a1b2c3d` | Temporary (purged on PR close). |
| **Integration (`develop`)**| `develop-{COMMIT_SHA}` | `taskmanager/task-service:develop-a1b2c3d` | Retained for 30 days in registry. |
| **Release Tag (`main`)** | `v{MAJOR}.{MINOR}.{PATCH}` | `taskmanager/task-service:v1.0.0` | **Permanent immutable release tag**. |
| **Release Latest** | `latest` | `taskmanager/task-service:latest` | Points to current active release. |

---

## 3. BuildKit Performance Optimization

Docker BuildKit is mandated across all CI build runners:
```bash
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1
```
- **Parallel Stage Execution**: Independent stages in multi-stage builds compile concurrently across available CPU cores.
- **Cache Mounts**: Mounts local `.m2` repository caches into container builds to prevent re-downloading dependencies on every run.

---

## 4. Container Vulnerability Scanning (Trivy)

Every container image built by Jenkins is scanned with Trivy before deployment:

```bash
trivy image \
  --severity HIGH,CRITICAL \
  --exit-code 1 \
  --ignore-unfixed \
  taskmanager/task-service:develop-${GIT_COMMIT_SHORT}
```

- **Exit Code 1**: If any unpatched `CRITICAL` or `HIGH` vulnerability exists in the base image or OS packages, the pipeline immediately aborts and prevents deployment.

